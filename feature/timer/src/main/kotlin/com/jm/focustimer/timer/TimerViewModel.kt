package com.jm.focustimer.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.domain.usecase.AddPresetUseCase
import com.jm.focustimer.domain.usecase.DeletePresetUseCase
import com.jm.focustimer.domain.usecase.GetAllPresetsUseCase
import com.jm.focustimer.domain.usecase.UpdatePresetUseCase
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 타이머 화면의 ViewModel
 *
 * MVI 패턴을 사용하여 사용자 Intent를 처리하고 UI 상태를 관리합니다.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val countdownTimerUseCase: CountdownTimerUseCase,
    private val getAllPresetsUseCase: GetAllPresetsUseCase,
    private val addPresetUseCase: AddPresetUseCase,
    private val updatePresetUseCase: UpdatePresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase,
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

    init {
        // 프리셋 목록 관찰
        observePresets()
    }

    /**
     * 프리셋 목록을 관찰하여 UI 상태에 반영
     */
    private fun observePresets() {
        getAllPresetsUseCase()
            .onEach { presets ->
                _uiState.update { it.copy(presets = presets) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 사용자 Intent 처리
     */
    fun onIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.SetTime -> handleSetTime(intent.totalTime)
            is TimerIntent.Start -> handleStart()
            is TimerIntent.Pause -> handlePause()
            is TimerIntent.Resume -> handleResume()
            is TimerIntent.Stop -> handleStop()
            is TimerIntent.DragProgress -> handleDragProgress(intent.progress)
            is TimerIntent.SelectPreset -> handleSelectPreset(intent.presetId)
            is TimerIntent.SaveAsPreset -> handleSaveAsPreset(intent.name)
            is TimerIntent.DeletePreset -> handleDeletePreset(intent.presetId)
            is TimerIntent.UpdatePreset -> handleUpdatePreset(
                intent.presetId,
                intent.name,
                intent.duration
            )
        }
    }

    /**
     * 타이머 시간 설정
     */
    private fun handleSetTime(totalTime: Duration) {
        // 타이머가 실행 중이 아닐 때만 시간 설정 가능
        if (_uiState.value.isActive) {
            sendSideEffect(TimerSideEffect.ShowError("타이머 실행 중에는 시간을 변경할 수 없습니다."))
            return
        }

        val timeInMillis = totalTime.inWholeMilliseconds
        // 유효성 검사
        if (totalTime <= 0.seconds) {
            sendSideEffect(TimerSideEffect.ShowError("시간은 0보다 커야 합니다."))
            return
        }

        initialTimeInMillis = timeInMillis

        // 최대 60분을 기준으로 progress 계산
        val progress = (timeInMillis.toFloat() / MAX_TIME.inWholeMilliseconds).coerceIn(0f, 1f)

        _uiState.update {
            it.copy(
                remainingTime = totalTime,
                progress = progress,
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
        if (currentState.remainingTime <= 0.seconds) {
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
        val updatedMinutes = if (newProgress < 0.01f) {
            0
        } else {
            (newProgress * 60).toInt().coerceIn(1, 60)
        }

        initialTimeInMillis = updatedMinutes.minutes.inWholeMilliseconds

        val newState = _uiState.value.copy(
            remainingTime = updatedMinutes.minutes,
            progress = newProgress,
            error = null,
            selectedPresetId = null // 수동 조정 시 프리셋 선택 해제
        )
        _uiState.value = newState
    }

    /**
     * 프리셋 선택 처리
     */
    private fun handleSelectPreset(presetId: Int) {
        // 타이머가 실행 중이면 무시
        if (_uiState.value.isActive) {
            sendSideEffect(TimerSideEffect.ShowError("타이머 실행 중에는 프리셋을 변경할 수 없습니다."))
            return
        }

        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                handleSetTime(preset.duration)
                _uiState.update { it.copy(selectedPresetId = presetId) }
                sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${preset.name}' 선택됨"))
            }
        }
    }

    /**
     * 현재 시간을 프리셋으로 저장
     */
    private fun handleSaveAsPreset(name: String) {
        val currentTime = _uiState.value.remainingTime

        if (currentTime <= 0.seconds) {
            sendSideEffect(TimerSideEffect.ShowError("시간을 설정한 후 프리셋으로 저장할 수 있습니다."))
            return
        }

        viewModelScope.launch {
            val result = addPresetUseCase(name, currentTime)
            result.fold(
                onSuccess = { id ->
                    sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${name}' 저장 완료"))
                    _uiState.update { it.copy(selectedPresetId = id.toInt()) }
                },
                onFailure = { error ->
                    sendSideEffect(TimerSideEffect.ShowError(error.message ?: "프리셋 저장 실패"))
                }
            )
        }
    }

    /**
     * 프리셋 삭제 처리
     */
    private fun handleDeletePreset(presetId: Int) {
        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                val result = deletePresetUseCase(preset)
                result.fold(
                    onSuccess = {
                        sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${preset.name}' 삭제됨"))
                        // 삭제된 프리셋이 선택되어 있었다면 선택 해제
                        if (_uiState.value.selectedPresetId == presetId) {
                            _uiState.update { it.copy(selectedPresetId = null) }
                        }
                    },
                    onFailure = { error ->
                        sendSideEffect(TimerSideEffect.ShowError(error.message ?: "프리셋 삭제 실패"))
                    }
                )
            }
        }
    }

    /**
     * 프리셋 수정 처리
     */
    private fun handleUpdatePreset(presetId: Int, name: String, duration: Duration) {
        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                val updatedPreset = preset.copy(name = name, duration = duration)
                val result = updatePresetUseCase(updatedPreset)
                result.fold(
                    onSuccess = {
                        sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${name}' 수정 완료"))
                    },
                    onFailure = { error ->
                        sendSideEffect(TimerSideEffect.ShowError(error.message ?: "프리셋 수정 실패"))
                    }
                )
            }
        }
    }

    /**
     * 타이머 시작 로직
     */
    private fun startTimer() {
        // 이전 타이머 취소
        timerJob?.cancel()

        val remainingTime = _uiState.value.remainingTime

        _uiState.update {
            it.copy(
                isRunning = true,
                isPaused = false,
                isCompleted = false,
                overtime = 0.seconds,
                error = null
            )
        }

        timerJob = viewModelScope.launch {
            countdownTimerUseCase(totalDuration = remainingTime)
                .onEach { event ->
                    when (event) {
                        is TimerEvent.Tick -> {
                            // 타이머 진행 중
                            val remainingTime = event.remainingTime
                            val progress = if (initialTimeInMillis > 0) {
                                remainingTime.inWholeMilliseconds.toFloat() / MAX_TIME.inWholeMilliseconds.toFloat()
                            } else {
                                0f
                            }

                            _uiState.update {
                                it.copy(
                                    remainingTime = remainingTime,
                                    progress = progress
                                )
                            }

                            // 마지막 5초는 햅틱 피드백
                            if (remainingTime in 1.seconds..5.seconds) {
                                sendSideEffect(TimerSideEffect.HapticFeedback(HapticPattern.TICK))
                            }
                        }

                        is TimerEvent.Completed -> {
                            // 이미 완료 상태면 중복 처리 방지
                            if (_uiState.value.isCompleted) return@onEach

                            // 타이머 완료
                            _uiState.update {
                                it.copy(
                                    remainingTime = 0.seconds,
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
                            val remainingTime = event.remainingTime
                            sendSideEffect(TimerSideEffect.ShowReminder(remainingTime))
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
        // 기존 Job 취소 (중복 실행 방지)
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            flow {
                while (true) {
                    delay(1.seconds)
                    emit(_uiState.value.overtime + 1.seconds)
                }
            }
                .onEach { newOvertime ->
                    _uiState.update {
                        it.copy(overtime = newOvertime)
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

        val progress = (initialTimeInMillis.toFloat() / MAX_TIME.inWholeMilliseconds.toFloat()).coerceIn(0f, 1f)

        _uiState.update {
            TimerUiState(
                remainingTime = initialTimeInMillis.milliseconds,
                progress = progress,
                presets = it.presets, // 프리셋 목록 유지
                selectedPresetId = it.selectedPresetId // 선택된 프리셋 유지
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

    companion object {
        val MAX_TIME = 60.minutes
    }
}