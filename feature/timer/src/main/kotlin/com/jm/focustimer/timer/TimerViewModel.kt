package com.jm.focustimer.timer

import TimeFormatter.minutes
import TimeFormatter.seconds
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.timer.model.HapticPattern
import com.jm.focustimer.timer.model.TimerEvent
import com.jm.focustimer.timer.model.TimerIntent
import com.jm.focustimer.timer.model.TimerSideEffect
import com.jm.focustimer.timer.model.TimerUiState
import com.jm.focustimer.timer.usecase.CountdownTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 타이머 화면의 ViewModel
 *
 * MVI 패턴을 사용하여 사용자 Intent를 처리하고 UI 상태를 관리합니다.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val countdownTimerUseCase: CountdownTimerUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Side Effect 전달용 Channel
    private val _sideEffect = Channel<TimerSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    // 타이머 실행 Job
    private var timerJob: Job? = null

    // 초기 설정 시간 (밀리초)
    private var initialTimeInMillis: Long = 0L

    // 타이머 완료 후 초과 시간 추적용 시작 시간
    private var completedAtMillis: Long = 0L

    /**
     * 사용자 Intent 처리
     */
    fun onIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.SetTime -> handleSetTime(intent.timeInMillis)
            is TimerIntent.Start -> handleStart()
            is TimerIntent.Pause -> handlePause()
            is TimerIntent.Resume -> handleResume()
            is TimerIntent.Stop -> handleStop()
            is TimerIntent.DragProgress -> handleDragProgress(intent.progress)
        }
    }

    /**
     * 타이머 시간 설정
     */
    private fun handleSetTime(timeInMillis: Long) {
        // 타이머가 실행 중이 아닐 때만 시간 설정 가능
        if (_uiState.value.isActive) {
            sendSideEffect(TimerSideEffect.ShowError("타이머 실행 중에는 시간을 변경할 수 없습니다."))
            return
        }

        // 유효성 검사
        if (timeInMillis <= 0) {
            sendSideEffect(TimerSideEffect.ShowError("시간은 0보다 커야 합니다."))
            return
        }

        initialTimeInMillis = timeInMillis
        val timeInSeconds = (timeInMillis / 1000).toInt()

        _uiState.update {
            it.copy(
                timeInSeconds = timeInSeconds,
                progress = 1f,
                error = null
            )
        }
    }

    /**
     * 타이머 시작
     */
    private fun handleStart() {
        val currentState = _uiState.value

        // 이미 실행 중이면 무시
        if (currentState.isRunning) return

        // 시간이 설정되지 않았으면 에러
        if (currentState.timeInSeconds <= 0) {
            sendSideEffect(TimerSideEffect.ShowError("시간을 설정해주세요."))
            return
        }

        startTimer()
    }

    /**
     * 타이머 일시정지
     */
    private fun handlePause() {
        val currentState = _uiState.value

        // 실행 중이 아니면 무시
        if (!currentState.isRunning) return

        pauseTimer()
    }

    /**
     * 타이머 재개
     */
    private fun handleResume() {
        val currentState = _uiState.value

        // 일시정지 상태가 아니면 무시
        if (!currentState.isPaused) return

        resumeTimer()
    }

    /**
     * 타이머 정지 및 초기화
     */
    private fun handleStop() {
        stopTimer()
    }

    /**
     * 드래그를 통한 시간 조정
     */
    private fun handleDragProgress(newProgress: Float) {
        // 타이머가 실행 중이 아닐 때만 드래그 가능
        if (_uiState.value.isActive) return

        // progress를 시간으로 변환 (1~60분)
        // progress가 0에 가까울 때는 최소 1분, 1에 가까울 때는 최대 60분
        val minutes = if (newProgress < 0.01f) {
            0
        } else {
            (newProgress * 60).toInt().coerceIn(1, 60)
        }

        val timeInMillis = minutes.minutes()
        initialTimeInMillis = timeInMillis
        val timeInSeconds = (timeInMillis / 1000).toInt()

        val newState = _uiState.value.copy(
            timeInSeconds = timeInSeconds,
            progress = newProgress,
            error = null
        )
        _uiState.value = newState
        saveState(newState)
    }

    /**
     * 타이머 시작 로직
     */
    private fun startTimer() {
        // 이전 타이머 취소
        timerJob?.cancel()

        val currentTimeInMillis = _uiState.value.timeInSeconds.seconds()

        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                isCompleted = false,
                overtimeInSeconds = 0,
                error = null
            )
        }

        timerJob = viewModelScope.launch {
            countdownTimerUseCase(durationMillis = currentTimeInMillis)
                .onEach { event ->
                    when (event) {
                        is TimerEvent.Tick -> {
                            // 타이머 진행 중
                            val remainingSeconds = (event.remainingTimeMillis / 1000).toInt()
                            val progress = if (initialTimeInMillis > 0) {
                                event.remainingTimeMillis.toFloat() / initialTimeInMillis
                            } else {
                                0f
                            }

                            _uiState.update {
                                it.copy(
                                    timeInSeconds = remainingSeconds,
                                    progress = progress
                                )
                            }

                            // 마지막 5초는 햅틱 피드백
                            if (remainingSeconds in 1..5) {
                                sendSideEffect(TimerSideEffect.HapticFeedback(HapticPattern.TICK))
                            }
                        }

                        is TimerEvent.Completed -> {
                            // 타이머 완료
                            completedAtMillis = System.currentTimeMillis()
                            _uiState.update {
                                it.copy(
                                    timeInSeconds = 0,
                                    isCompleted = true,
                                    isRunning = false,
                                    progress = 0f
                                )
                            }

                            sendSideEffect(TimerSideEffect.ShowTimerCompleted)
                            sendSideEffect(TimerSideEffect.HapticFeedback(HapticPattern.COMPLETED))

                            // 완료 후 초과 시간 계산 시작
                            startOvertimeTracking()
                        }

                        is TimerEvent.Reminder -> {
                            // 리마인더 이벤트 처리 (필요 시 구현)
                            // 예: 알림 표시, 햅틱 피드백 등
                            val remainingSeconds = (event.remainingTimeMillis / 1000).toInt()
                            sendSideEffect(TimerSideEffect.ShowReminder(remainingSeconds))
                            sendSideEffect(TimerSideEffect.HapticFeedback(HapticPattern.REMINDER))
                        }
                    }
                }
                .catch { exception ->
                    sendSideEffect(TimerSideEffect.ShowError("타이머 실행 중 오류가 발생했습니다: ${exception.message}"))
                    stopTimer()
                }
                .launchIn(this)
        }
    }

    /**
     * 타이머 완료 후 초과 시간 추적
     */
    private fun startOvertimeTracking() {
        timerJob = viewModelScope.launch {
            flow {
                while (true) {
                    delay(1.seconds())
                    val overtimeSeconds =
                        ((System.currentTimeMillis() - completedAtMillis) / 1000).toInt()
                    emit(overtimeSeconds)
                }
            }
                .onEach { overtimeSeconds ->
                    _uiState.update {
                        it.copy(overtimeInSeconds = overtimeSeconds)
                    }
                }
                .catch { exception ->
                    sendSideEffect(TimerSideEffect.ShowError("초과 시간 추적 중 오류가 발생했습니다: ${exception.message}"))
                }
                .launchIn(this)
        }
    }

    /**
     * 타이머 일시정지 로직
     */
    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isRunning = false,
                isPaused = true
            )
        }
    }

    /**
     * 타이머 재개 로직
     */
    private fun resumeTimer() {
        startTimer()
    }

    /**
     * 타이머 정지 및 초기화 로직
     */
    private fun stopTimer() {
        timerJob?.cancel()
        completedAtMillis = 0L

        val timeInSeconds = (initialTimeInMillis / 1000).toInt()
        _uiState.update {
            TimerUiState(
                timeInSeconds = timeInSeconds,
                progress = 1f
            )
        }
    }

    /**
     * Side Effect 전송
     */
    private fun sendSideEffect(sideEffect: TimerSideEffect) {
        viewModelScope.launch {
            _sideEffect.send(sideEffect)
        }
    }

    /**
     * ViewModel이 제거될 때 타이머 정리
     */
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        _sideEffect.close() // Channel 정리
    }
}