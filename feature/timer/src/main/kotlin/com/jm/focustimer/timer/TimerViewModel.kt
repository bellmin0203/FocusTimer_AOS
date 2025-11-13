package com.jm.focustimer.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.domain.model.Preset
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
import com.jm.logutil.LogUtil
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
        LogUtil.d("TimerViewModel initialized")
        // 프리셋 목록 관찰
        observePresets()
    }

    /**
     * 프리셋 목록을 관찰하여 UI 상태에 반영
     */
    private fun observePresets() {
        LogUtil.d("observePresets: 프리셋 목록 관찰 시작")
        getAllPresetsUseCase()
            .onEach { presets ->
                LogUtil.d("observePresets: 프리셋 목록 업데이트, count=${presets.size}")
                _uiState.update { it.copy(presets = presets) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 사용자 Intent 처리
     */
    fun onIntent(intent: TimerIntent) {
        LogUtil.d("onIntent: ${intent::class.simpleName}")
        when (intent) {
            is TimerIntent.SetTime -> handleSetTime(intent.totalTime)
            is TimerIntent.Start -> handleStart()
            is TimerIntent.Pause -> handlePause()
            is TimerIntent.Resume -> handleResume()
            is TimerIntent.Stop -> handleStop()
            is TimerIntent.Complete -> handleComplete()
            is TimerIntent.DragProgress -> handleDragProgress(intent.progress)
            is TimerIntent.SelectPreset -> handleSelectPreset(intent.presetId)
            is TimerIntent.SaveAsPreset -> handleSaveAsPreset(
                intent.name,
                intent.duration,
                intent.colorIndex
            )
            is TimerIntent.DeletePreset -> handleDeletePreset(intent.presetId)
            is TimerIntent.UpdatePreset -> handleUpdatePreset(intent.preset)
        }
    }

    /**
     * 타이머 시간 설정
     */
    private fun handleSetTime(totalTime: Duration) {
        LogUtil.d("handleSetTime: totalTime=$totalTime, isActive=${_uiState.value.isActive}")

        // 타이머가 실행 중이 아닐 때만 시간 설정 가능
        if (_uiState.value.isActive) {
            LogUtil.w("handleSetTime: 타이머 실행 중이므로 시간 설정 불가")
            sendSideEffect(TimerSideEffect.ShowError("타이머 실행 중에는 시간을 변경할 수 없습니다."))
            return
        }

        val timeInMillis = totalTime.inWholeMilliseconds
        // 유효성 검사
        if (totalTime <= 0.seconds) {
            LogUtil.w("handleSetTime: 유효하지 않은 시간 값 (0 이하)")
            sendSideEffect(TimerSideEffect.ShowError("시간은 0보다 커야 합니다."))
            return
        }

        initialTimeInMillis = timeInMillis

        // 최대 60분을 기준으로 progress 계산
        val progress = (timeInMillis.toFloat() / MAX_TIME.inWholeMilliseconds).coerceIn(0f, 1f)

        LogUtil.d("handleSetTime: 시간 설정 완료, initialTime=$totalTime, progress=$progress")

        _uiState.update {
            it.copy(
                remainingTime = totalTime,
                progress = progress,
                error = null,
                initialTime = totalTime
            )
        }
    }

    /**
     * 타이머 시작
     */
    private fun handleStart() {
        val currentState = _uiState.value
        LogUtil.d("handleStart: isRunning=${currentState.isRunning}, remainingTime=${currentState.remainingTime}")

        // 이미 실행 중이면 무시
        if (currentState.isRunning) {
            LogUtil.w("handleStart: 이미 타이머가 실행 중")
            return
        }

        // 시간이 설정되지 않았으면 에러
        if (currentState.remainingTime <= 0.seconds) {
            LogUtil.w("handleStart: 시간이 설정되지 않음")
            sendSideEffect(TimerSideEffect.ShowError("시간을 설정해주세요."))
            return
        }

        LogUtil.d("handleStart: 타이머 시작")
        startTimer()
    }

    /**
     * 타이머 일시정지
     */
    private fun handlePause() {
        val currentState = _uiState.value
        LogUtil.d("handlePause: isRunning=${currentState.isRunning}")

        // 실행 중이 아니면 무시
        if (!currentState.isRunning) {
            LogUtil.w("handlePause: 타이머가 실행 중이 아님")
            return
        }

        LogUtil.d("handlePause: 타이머 일시정지")
        pauseTimer()
    }

    /**
     * 타이머 재개
     */
    private fun handleResume() {
        val currentState = _uiState.value
        LogUtil.d("handleResume: isPaused=${currentState.isPaused}")

        // 일시정지 상태가 아니면 무시
        if (!currentState.isPaused) {
            LogUtil.w("handleResume: 일시정지 상태가 아님")
            return
        }

        LogUtil.d("handleResume: 타이머 재개")
        resumeTimer()
    }

    /**
     * 타이머 정지 및 초기화
     */
    private fun handleStop() {
        LogUtil.d("handleStop: 타이머 정지 및 초기화")
        stopTimer()
    }

    /**
     * 타이머 완료 확인 처리
     */
    private fun handleComplete() {
        LogUtil.d("handleComplete: 타이머 완료 확인")

        if (!_uiState.value.isCompleted) {
            LogUtil.w("handleComplete: 완료 상태가 아님")
            return
        }

        // 타이머를 초기 상태로 리셋
        stopTimer()
        sendSideEffect(TimerSideEffect.ShowSnackbar("타이머를 완료했습니다!"))
    }

    /**
     * 드래그를 통한 시간 조정
     */
    private fun handleDragProgress(newProgress: Float) {
        LogUtil.d("handleDragProgress: newProgress=$newProgress, isActive=${_uiState.value.isActive}")

        // 타이머가 실행 중이 아닐 때만 드래그 가능
        if (_uiState.value.isActive) {
            LogUtil.w("handleDragProgress: 타이머 실행 중이므로 드래그 불가")
            return
        }

        // progress를 시간으로 변환 (1~60분)
        // progress가 0에 가까울 때는 최소 1분, 1에 가까울 때는 최대 60분
        val updatedMinutes = if (newProgress < 0.01f) {
            0
        } else {
            (newProgress * 60).toInt().coerceIn(1, 60)
        }

        initialTimeInMillis = updatedMinutes.minutes.inWholeMilliseconds

        LogUtil.d("handleDragProgress: updatedMinutes=$updatedMinutes")

        val newState = _uiState.value.copy(
            remainingTime = updatedMinutes.minutes,
            progress = newProgress,
            error = null,
            selectedPresetId = null, // 수동 조정 시 프리셋 선택 해제
            initialTime = updatedMinutes.minutes
        )
        _uiState.value = newState
    }

    /**
     * 프리셋 선택 처리
     */
    private fun handleSelectPreset(presetId: Int) {
        LogUtil.d("handleSelectPreset: presetId=$presetId, isActive=${_uiState.value.isActive}")

        // 타이머가 실행 중이면 무시
        if (_uiState.value.isActive) {
            LogUtil.w("handleSelectPreset: 타이머 실행 중이므로 프리셋 변경 불가")
            sendSideEffect(TimerSideEffect.ShowError("타이머 실행 중에는 프리셋을 변경할 수 없습니다."))
            return
        }

        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                LogUtil.d("handleSelectPreset: 프리셋 선택됨, name=${preset.name}, duration=${preset.duration}")
                handleSetTime(preset.duration)
                _uiState.update { it.copy(selectedPresetId = presetId) }
                sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${preset.name}' 선택됨"))
            } else {
                LogUtil.w("handleSelectPreset: 프리셋을 찾을 수 없음, presetId=$presetId")
            }
        }
    }

    /**
     * 현재 시간을 프리셋으로 저장
     */
    private fun handleSaveAsPreset(name: String, duration: Duration, colorIndex: Int) {
        LogUtil.d("handleSaveAsPreset: name=$name, duration=$duration, colorIndex=$colorIndex")

        if (duration <= 0.seconds) {
            LogUtil.w("handleSaveAsPreset: 시간이 설정되지 않음")
            sendSideEffect(TimerSideEffect.ShowError("시간을 설정한 후 프리셋으로 저장할 수 있습니다."))
            return
        }

        // 프리셋 최대 개수 체크
        if (_uiState.value.presets.size >= AddPresetUseCase.MAX_PRESET_COUNT) {
            LogUtil.w("handleSaveAsPreset: 프리셋 최대 개수 초과")
            sendSideEffect(TimerSideEffect.ShowError("프리셋은 최대 ${AddPresetUseCase.MAX_PRESET_COUNT}개까지 저장할 수 있습니다."))
            return
        }

        viewModelScope.launch {
            val result = addPresetUseCase(name, duration, colorIndex)
            result.fold(
                onSuccess = { id ->
                    LogUtil.d("handleSaveAsPreset: 프리셋 저장 성공, id=$id")
                    sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${name}' 저장 완료"))
                    _uiState.update { it.copy(selectedPresetId = id.toInt()) }
                },
                onFailure = { error ->
                    LogUtil.e("handleSaveAsPreset: 프리셋 저장 실패", error)
                    sendSideEffect(TimerSideEffect.ShowError(error.message ?: "프리셋 저장 실패"))
                }
            )
        }
    }

    /**
     * 프리셋 삭제 처리
     */
    private fun handleDeletePreset(presetId: Int) {
        LogUtil.d("handleDeletePreset: presetId=$presetId")

        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                LogUtil.d("handleDeletePreset: 프리셋 찾음, name=${preset.name}")
                val result = deletePresetUseCase(preset)
                result.fold(
                    onSuccess = {
                        LogUtil.d("handleDeletePreset: 프리셋 삭제 성공")
                        sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${preset.name}' 삭제됨"))
                        // 삭제된 프리셋이 선택되어 있었다면 선택 해제
                        if (_uiState.value.selectedPresetId == presetId) {
                            LogUtil.d("handleDeletePreset: 선택된 프리셋 해제")
                            _uiState.update { it.copy(selectedPresetId = null) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("handleDeletePreset: 프리셋 삭제 실패", error)
                        sendSideEffect(TimerSideEffect.ShowError(error.message ?: "프리셋 삭제 실패"))
                    }
                )
            } else {
                LogUtil.w("handleDeletePreset: 프리셋을 찾을 수 없음, presetId=$presetId")
            }
        }
    }

    /**
     * 프리셋 수정 처리
     */
    private fun handleUpdatePreset(preset: Preset) {
        val presetId = preset.id
        val name = preset.name
        val duration = preset.duration
        val colorIndex = preset.colorIndex
        LogUtil.d("handleUpdatePreset: presetId=$presetId, name=$name, duration=$duration, colorIndex=$colorIndex")

        viewModelScope.launch {
            val existingPreset = _uiState.value.presets.find { it.id == presetId }
            if (existingPreset != null) {
                LogUtil.d("handleUpdatePreset: 프리셋 찾음, oldName=${existingPreset.name}")
                val updatedPreset =
                    existingPreset.copy(name = name, duration = duration, colorIndex = colorIndex)
                val result = updatePresetUseCase(updatedPreset)
                result.fold(
                    onSuccess = {
                        LogUtil.d("handleUpdatePreset: 프리셋 수정 성공")
                        sendSideEffect(TimerSideEffect.ShowSnackbar("프리셋 '${name}' 수정 완료"))

                        // 수정된 프리셋이 현재 선택되어 있다면 타이머도 동기화
                        if (_uiState.value.selectedPresetId == presetId && !_uiState.value.isActive) {
                            LogUtil.d("handleUpdatePreset: 선택된 프리셋이므로 타이머 동기화")
                            handleSetTime(duration)
                            _uiState.update { it.copy(selectedPresetId = presetId) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("handleUpdatePreset: 프리셋 수정 실패", error)
                        sendSideEffect(TimerSideEffect.ShowError(error.message ?: "프리셋 수정 실패"))
                    }
                )
            } else {
                LogUtil.w("handleUpdatePreset: 프리셋을 찾을 수 없음, presetId=$presetId")
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
        LogUtil.d("startTimer: remainingTime=$remainingTime")

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

                            // 매 10초마다 로그 출력 (너무 빈번한 로그 방지)
                            if (remainingTime.inWholeSeconds % 10L == 0L) {
                                LogUtil.d("startTimer: Tick event, remainingTime=$remainingTime, progress=$progress")
                            }

                            _uiState.update {
                                it.copy(
                                    remainingTime = remainingTime,
                                    progress = progress
                                )
                            }

                            // 마지막 5초는 햅틱 피드백
                            if (remainingTime in 1.seconds..5.seconds) {
                                LogUtil.d("startTimer: 마지막 5초, 햅틱 피드백 전송")
                                sendSideEffect(TimerSideEffect.HapticFeedback(HapticPattern.TICK))
                            }
                        }

                        is TimerEvent.Completed -> {
                            // 이미 완료 상태면 중복 처리 방지
                            if (_uiState.value.isCompleted) {
                                LogUtil.w("startTimer: 이미 완료 상태, 중복 처리 방지")
                                return@onEach
                            }

                            LogUtil.d("startTimer: 타이머 완료")

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
                            LogUtil.d("startTimer: 초과 시간 추적 시작")
                            startOvertimeTracking()
                        }

                        is TimerEvent.Reminder -> {
                            // 리마인더 이벤트 처리 (필요 시 구현)
                            // 예: 알림 표시, 햅틱 피드백 등
                            val remainingTime = event.remainingTime
                            LogUtil.d("startTimer: Reminder event, remainingTime=$remainingTime")
                            sendSideEffect(TimerSideEffect.ShowReminder(remainingTime))
                            sendSideEffect(TimerSideEffect.HapticFeedback(HapticPattern.REMINDER))
                        }
                    }
                }
                .catch { exception ->
                    LogUtil.e("startTimer: 타이머 실행 중 오류 발생", exception)
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
            LogUtil.d("startOvertimeTracking: 초과 시간 추적 시작")

            flow {
                while (true) {
                    delay(1.seconds)
                    emit(_uiState.value.overtime + 1.seconds)
                }
            }
                .onEach { newOvertime ->
                    // 매 10초마다 로그 출력
                    if (newOvertime.inWholeSeconds % 10L == 0L) {
                        LogUtil.d("startOvertimeTracking: overtime=$newOvertime")
                    }

                    // 초과 시간을 기준으로 progress 계산 (최대 60분까지 표시)
                    val overtimeProgress =
                        (newOvertime.inWholeMilliseconds.toFloat() / MAX_TIME.inWholeMilliseconds.toFloat()).coerceIn(
                            0f,
                            1f
                        )

                    _uiState.update {
                        it.copy(
                            overtime = newOvertime,
                            progress = overtimeProgress
                        )
                    }
                }
                .catch { exception ->
                    LogUtil.e("startOvertimeTracking: 초과 시간 추적 중 오류 발생", exception)
                    sendSideEffect(TimerSideEffect.ShowError("초과 시간 추적 중 오류가 발생했습니다: ${exception.message}"))
                }
                .launchIn(this)
        }
    }

    /**
     * 타이머 일시정지 로직
     */
    private fun pauseTimer() {
        LogUtil.d("pauseTimer: 타이머 일시정지, remainingTime=${_uiState.value.remainingTime}")
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
        LogUtil.d("resumeTimer: 타이머 재개, remainingTime=${_uiState.value.remainingTime}")
        startTimer()
    }

    /**
     * 타이머 정지 및 초기화 로직
     */
    private fun stopTimer() {
        LogUtil.d("stopTimer: 타이머 정지 및 초기화, initialTimeInMillis=$initialTimeInMillis")
        timerJob?.cancel()

        val progress = (initialTimeInMillis.toFloat() / MAX_TIME.inWholeMilliseconds.toFloat()).coerceIn(0f, 1f)

        _uiState.update {
            TimerUiState(
                remainingTime = initialTimeInMillis.milliseconds,
                progress = progress,
                presets = it.presets, // 프리셋 목록 유지
                selectedPresetId = it.selectedPresetId, // 선택된 프리셋 유지
                initialTime = initialTimeInMillis.milliseconds
            )
        }

        LogUtil.d("stopTimer: 초기화 완료, remainingTime=${initialTimeInMillis.milliseconds}, progress=$progress")
    }

    /**
     * Side Effect 전송
     */
    private fun sendSideEffect(sideEffect: TimerSideEffect) {
        LogUtil.d("sendSideEffect: ${sideEffect::class.simpleName}")
        viewModelScope.launch {
            _sideEffect.send(sideEffect)
        }
    }

    /**
     * ViewModel이 제거될 때 타이머 정리
     */
    override fun onCleared() {
        super.onCleared()
        LogUtil.d("onCleared: ViewModel 정리 중")
        timerJob?.cancel()
        _sideEffect.close() // Channel 정리
    }

    companion object {
        val MAX_TIME = 60.minutes
    }
}