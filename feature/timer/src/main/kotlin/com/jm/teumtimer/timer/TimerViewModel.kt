package com.jm.teumtimer.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.logutil.LogUtil
import com.jm.teumtimer.domain.model.preset.Preset
import com.jm.teumtimer.domain.repository.SettingsRepository
import com.jm.teumtimer.domain.usecase.preset.ManagePresetUseCase
import com.jm.teumtimer.domain.usecase.preset.PresetException
import com.jm.teumtimer.domain.usecase.session.ManageTimerSessionUseCase
import com.jm.teumtimer.domain.usecase.session.SessionException
import com.jm.teumtimer.timer.model.HapticPattern
import com.jm.teumtimer.timer.model.PresetError
import com.jm.teumtimer.timer.model.SessionError
import com.jm.teumtimer.timer.model.SetTimeError
import com.jm.teumtimer.timer.model.TimerError
import com.jm.teumtimer.timer.model.TimerIntent
import com.jm.teumtimer.timer.model.TimerSideEffect
import com.jm.teumtimer.timer.model.TimerSideEffect.HapticFeedback
import com.jm.teumtimer.timer.model.TimerSideEffect.ShowError
import com.jm.teumtimer.timer.model.TimerUiState
import com.jm.teumtimer.timer.usecase.TimerStatus
import com.jm.teumtimer.util.NotificationSoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 타이머 화면의 ViewModel
 *
 * MVI 패턴을 사용하여 사용자 Intent를 처리하고 UI 상태를 관리합니다.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val managePresetUseCase: ManagePresetUseCase,
    private val manageTimerSessionUseCase: ManageTimerSessionUseCase,
    private val timerManager: TimerManager,
    private val settingsRepository: SettingsRepository,
    private val notificationSoundPlayer: NotificationSoundPlayer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Side Effect 전달용 Channel
    private val _sideEffect = Channel<TimerSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private val intentChannel = Channel<TimerIntent>(Channel.UNLIMITED)


    init {
        LogUtil.d("TimerViewModel initialized")
        // 프리셋 목록 관찰
        observePresets()
        // 타이머 매니저 관찰
        observeTimerStateChanges()
        // Intent 요청 관찰
        observeIntent()
        // 설정 관찰
        observeSettings()
        // 마지막 세션 기억 설정에 따라 초기 시간 설정
        initializeTimerDuration()
    }

    /**
     * 프리셋 목록을 관찰하여 UI 상태에 반영
     */
    private fun observePresets() {
        LogUtil.d("프리셋 목록 관찰 시작")
        managePresetUseCase.getAllPresets()
            .onEach { presets ->
                LogUtil.d("프리셋 목록 업데이트, count=${presets.size}")
                _uiState.update { it.copy(presets = presets) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 모든 설정을 관찰하여 UI 상태에 반영
     */
    private fun observeSettings() {
        LogUtil.d("설정 관찰 시작")

        combine(
            settingsRepository.isScreenOn,
            settingsRepository.isHapticFeedback,
            settingsRepository.isTickSound,
            settingsRepository.isMinimizedControls,
            settingsRepository.isPulseAnimationEnabled
        ) { screenOn, haptic, tick, minimized, pulse ->
            LogUtil.d("설정 업데이트: screenOn=$screenOn, haptic=$haptic, tick=$tick, minimized=$minimized, pulse=$pulse")
            _uiState.update {
                it.copy(
                    isScreenOnEnabled = screenOn,
                    isHapticFeedbackEnabled = haptic,
                    isTickSoundEnabled = tick,
                    isMinimizedControlsEnabled = minimized,
                    isPulseAnimationEnabled = pulse
                )
            }
        }.launchIn(viewModelScope)
    }

    /**
     * 마지막 세션 기억 설정에 따라 초기 타이머 시간을 설정하고
     * 기본 프리셋이 있으면 자동으로 선택합니다
     */
    private fun initializeTimerDuration() {
        viewModelScope.launch {
            val isRememberLastSession = settingsRepository.isRememberLastSession.first()
            LogUtil.d("마지막 세션 기억 설정: $isRememberLastSession")

            if (!isRememberLastSession) {
                // 기본 프리셋이 있는지 확인
                val defaultPresetId = settingsRepository.defaultPresetId.first()

                if (defaultPresetId != null) {
                    // 기본 프리셋이 설정되어 있으면 프리셋 선택
                    LogUtil.d("기본 프리셋 선택: $defaultPresetId")
                    handleSelectPreset(defaultPresetId)
                } else {
                    // 기본 프리셋이 없으면 기본 세션 시간으로 설정
                    val defaultDuration = settingsRepository.defaultSessionDuration.first()
                    LogUtil.d("기본 세션 시간으로 설정: $defaultDuration")
                    handleSetTime(defaultDuration)
                }
            }
        }
    }

    private fun observeTimerStateChanges() {
        viewModelScope.launch {
            timerManager.timerState.collect { timerState ->
                val initialTime = timerState.initialDuration
                val remainingTime = timerState.remainingTime
                val overtime = timerState.overtime
                val isRunning = timerState.isRunning
                val isPaused = timerState.isPaused
                val selectedPreset = timerState.selectedPreset

                when (timerState.status) {
                    is TimerStatus.Idle -> {
                        notificationSoundPlayer.stop() // 알림 소리 정지

                        val progress = calculateProgress(remainingTime = initialTime)
                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                isRunning = false,
                                isPaused = false,
                                isCompleted = false,
                                overtime = Duration.ZERO,
                                progress = progress,
                                error = null,
                                currentSessionId = null,
                                sessionStartTime = null,
                            )
                        }
                    }

                    is TimerStatus.Running -> {
                        val progress = calculateProgress(remainingTime = remainingTime)

                        // 매 10초마다 로그 출력 (너무 빈번한 로그 방지)
                        if (remainingTime.inWholeSeconds % LOG_INTERVAL_SECONDS == 0L) {
                            LogUtil.d("startTimer: Tick event, remainingTime=$remainingTime, progress=$progress")
                        }

                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                isRunning = true,
                                isPaused = false,
                                overtime = overtime,
                                progress = progress,
                                selectedPreset = selectedPreset,
                            )
                        }
                    }

                    is TimerStatus.Paused -> {
                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                overtime = overtime,
                                isPaused = true,
                                isRunning = false,
                                selectedPreset = selectedPreset,
                            )
                        }
                    }

                    is TimerStatus.Completed -> {
                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                overtime = overtime,
                                isRunning = isRunning,
                                isPaused = isPaused,
                                isCompleted = true,
                                selectedPreset = selectedPreset,
                            )
                        }
                        if (!timerState.isOvertime) {
                            // 타이머가 정상 완료된 시점 (초과 시간 진입 전)
                            emitSideEffect(TimerSideEffect.ShowTimerCompleted)

                            // 햅틱 피드백 (설정이 활성화된 경우)
                            if (_uiState.value.isHapticFeedbackEnabled) {
                                emitSideEffect(HapticFeedback(HapticPattern.COMPLETED))
                            }

                            // 알림 소리 및 진동 재생
                            playNotificationSound()

                            // 타이머가 자동 완료되었으므로 세션을 완료 상태로 업데이트
                            val currentState = _uiState.value
                            currentState.currentSessionId?.let { sessionId ->
                                viewModelScope.launch {
                                    val updateResult = manageTimerSessionUseCase.completeSession(
                                        sessionId = sessionId,
                                        presetId = currentState.selectedPreset?.id,
                                        startTime = currentState.sessionStartTime,
                                        initialDuration = currentState.initialTime,
                                        overtime = Duration.ZERO
                                    )

                                    updateResult.fold(
                                        onSuccess = {
                                            LogUtil.d("세션 자동 완료 업데이트 성공, sessionId=$sessionId")
                                        },
                                        onFailure = { error ->
                                            LogUtil.e("세션 자동 완료 업데이트 실패", error)
                                            emitSideEffect(
                                                ShowError(
                                                    mapSessionExceptionToTimerError(
                                                        error,
                                                        true
                                                    )
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            timerManager.timerError.collect {
                emitSideEffect(ShowError(it))
            }
        }
    }

    /**
     * 사용자 Intent 처리
     */
    fun onIntent(intent: TimerIntent) {
        LogUtil.d("${intent::class.simpleName}")
        viewModelScope.launch { intentChannel.send(intent) }
    }

    private fun observeIntent() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                handleUserIntent(intent = intent)
            }
        }
    }

    private fun handleUserIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.SetTime -> handleSetTime(intent.totalTime)
            is TimerIntent.Start -> startTimerWithSession(intent.reminderThresholds)
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
     * 타이머 시간 설정 (SetTimeUseCase 활용)
     */
    private fun handleSetTime(totalTime: Duration) {
        timerManager.setTime(duration = totalTime)
    }

    /**
     * 타이머 시작
     */
    private fun startTimerWithSession(reminderThresholds: List<Duration>) {
        val currentState = _uiState.value
        LogUtil.d("isRunning=${currentState.isRunning}, remainingTime=${currentState.remainingTime}")

        // 이미 실행 중이면 무시
        if (currentState.isRunning) {
            LogUtil.w("이미 타이머가 실행 중")
            return
        }

        // 시간이 설정되지 않았으면 에러
        if (currentState.remainingTime <= 0.seconds) {
            LogUtil.w("시간이 설정되지 않음")
            emitSideEffect(
                ShowError(
                    TimerError.SetTime(SetTimeError.InvalidTime)
                )
            )
            return
        }

        LogUtil.d("타이머 시작")

        // 타이머 시작 전에 세션을 DB에 저장
        viewModelScope.launch {
            val sessionResult = manageTimerSessionUseCase.startSession(
                presetId = currentState.selectedPreset?.id ?: 0,
                duration = currentState.initialTime
            )

            sessionResult.fold(
                onSuccess = { sessionId ->
                    LogUtil.d("세션 저장 성공, sessionId=$sessionId")
                    _uiState.update {
                        it.copy(
                            currentSessionId = sessionId,
                            sessionStartTime = Instant.now()
                        )
                    }

                    // 세션 저장 성공 후 타이머 시작
                    timerManager.start(
                        initialDuration = _uiState.value.initialTime,
                        duration = _uiState.value.remainingTime,
                        reminderThresholds = reminderThresholds,
                    )
                },
                onFailure = { error ->
                    LogUtil.e("세션 저장 실패", error)
                    // 세션 저장 실패 시 알림 표시
                    emitSideEffect(ShowError(mapSessionExceptionToTimerError(error, false)))

                    // 세션 저장 실패 시에도 타이머는 시작 (사용자 경험 우선)
                    timerManager.start(
                        initialDuration = _uiState.value.initialTime,
                        duration = _uiState.value.remainingTime,
                        reminderThresholds = reminderThresholds,
                    )
                }
            )
        }
    }

    /**
     * 타이머 일시정지
     */
    private fun handlePause() {
        val currentState = _uiState.value
        LogUtil.d("isRunning=${currentState.isRunning}")

        // 실행 중이 아니면 무시
        if (!currentState.isRunning) {
            LogUtil.w("타이머가 실행 중이 아님")
            return
        }

        LogUtil.d("타이머 일시정지")
        timerManager.pause()
    }

    /**
     * 타이머 재개
     */
    private fun handleResume() {
        val currentState = _uiState.value
        LogUtil.d("isPaused=${currentState.isPaused}")

        // 일시정지 상태가 아니면 무시
        if (!currentState.isPaused) {
            LogUtil.w("일시정지 상태가 아님")
            return
        }

        LogUtil.d("타이머 재개")
        timerManager.resume()
    }

    /**
     * 타이머 정지 및 초기화
     */
    private fun handleStop() {
        LogUtil.d("타이머 정지 및 초기화")

        // 진행 중인 세션이 있으면 미완료 상태로 업데이트
        val currentState = _uiState.value
        currentState.currentSessionId?.let { sessionId ->
            viewModelScope.launch {
                val updateResult = manageTimerSessionUseCase.stopSession(
                    sessionId = sessionId,
                    presetId = currentState.selectedPreset?.id,
                    startTime = currentState.sessionStartTime,
                    initialDuration = currentState.initialTime
                )

                updateResult.fold(
                    onSuccess = {
                        LogUtil.d("세션 업데이트 성공 (미완료), sessionId=$sessionId")
                    },
                    onFailure = { error ->
                        LogUtil.e("세션 업데이트 실패", error)
                        // 에러 발생 시 UI에 알림
                        emitSideEffect(ShowError(mapSessionExceptionToTimerError(error, true)))
                    }
                )
            }
        }

        timerManager.stop(initialDuration = _uiState.value.initialTime)
        _uiState.update {
            it.copy(
                currentSessionId = null,
                sessionStartTime = null
            )
        }
    }

    /**
     * 타이머 완료 확인 처리
     *
     * 사용자가 완료 버튼을 클릭했을 때 호출됩니다.
     * 초과 시간이 있는 경우 세션에 초과 시간을 업데이트합니다.
     */
    private fun handleComplete() {
        LogUtil.d("타이머 완료 확인")

        if (!_uiState.value.isCompleted) {
            LogUtil.w("완료 상태가 아님")
            return
        }

        // 초과 시간이 있는 경우에만 세션 업데이트 (초과 시간 추가)
        val currentState = _uiState.value
        if (currentState.overtime > Duration.ZERO) {
            currentState.currentSessionId?.let { sessionId ->
                viewModelScope.launch {
                    val updateResult = manageTimerSessionUseCase.completeSession(
                        sessionId = sessionId,
                        presetId = currentState.selectedPreset?.id,
                        startTime = currentState.sessionStartTime,
                        initialDuration = currentState.initialTime,
                        overtime = Duration.ZERO
                    )

                    updateResult.fold(
                        onSuccess = {
                            LogUtil.d("세션 초과 시간 업데이트 성공, sessionId=$sessionId, overtime=${currentState.overtime}")
                        },
                        onFailure = { error ->
                            LogUtil.e("세션 초과 시간 업데이트 실패", error)
                            emitSideEffect(ShowError(mapSessionExceptionToTimerError(error, true)))
                        }
                    )
                }
            }
        } else {
            // 초과 시간 없이 완료 버튼을 누른 경우 (이미 자동 완료 시 업데이트됨)
            LogUtil.d("초과 시간 없음, 세션 업데이트 건너뜀")
        }

        // 타이머를 초기 상태로 리셋
        timerManager.stop(initialDuration = _uiState.value.initialTime)
        _uiState.update {
            it.copy(
                currentSessionId = null,
                sessionStartTime = null
            )
        }

        emitSideEffect(
            TimerSideEffect.ShowSnackbar(
                R.string.snackbar_timer_completed
            )
        )
    }

    /**
     * 드래그를 통한 시간 조정
     */
    private fun handleDragProgress(newProgress: Float) {
        LogUtil.d("newProgress=$newProgress, isActive=${_uiState.value.isTimerActiveOrPaused}")

        // 타이머가 실행 중이 아닐 때만 드래그 가능
        if (_uiState.value.isTimerActiveOrPaused) {
            LogUtil.w("타이머 실행 중이므로 드래그 불가")
            return
        }

        // progress를 시간으로 변환 (1~60분)
        // progress가 0에 가까울 때는 최소 1분, 1에 가까울 때는 최대 60분
        val updatedMinutes = if (newProgress < 0.01f) {
            0.minutes
        } else {
            (newProgress * MAX_DRAG_MINUTES).toInt()
                .coerceIn(MIN_DRAG_MINUTES, MAX_DRAG_MINUTES).minutes
        }

        LogUtil.d("updatedMinutes=$updatedMinutes")

        val newState = _uiState.value.copy(
            initialTime = updatedMinutes,
            remainingTime = updatedMinutes,
            isRunning = false,
            isPaused = false,
            overtime = Duration.ZERO,
            progress = newProgress,
            error = null,
            selectedPreset = null, // 수동 조정 시 프리셋 선택 해제
        )
        _uiState.update { newState }
    }

    /**
     * 프리셋 선택 처리
     */
    private fun handleSelectPreset(presetId: Int) {
        LogUtil.d("presetId=$presetId, isActive=${_uiState.value.isTimerActiveOrPaused}")

        viewModelScope.launch {
            val result = managePresetUseCase.selectPreset(
                presetId = presetId,
                presets = _uiState.value.presets,
                isTimerActive = _uiState.value.isTimerActiveOrPaused
            )

            result.fold(
                onSuccess = { preset ->
                    LogUtil.d("프리셋 선택됨, name=${preset.name}, duration=${preset.duration}")
                    handleSetTime(preset.duration)
                    _uiState.update { it.copy(selectedPreset = preset) }
                    timerManager.selectPreset(preset)
                    emitSideEffect(
                        TimerSideEffect.ShowSnackbar(
                            R.string.snackbar_preset_selected,
                            listOf(preset.name)
                        )
                    )
                },
                onFailure = { error ->
                    LogUtil.e("프리셋 선택 실패", error)
                    val timerError = when (error) {
                        is PresetException.TimerRunning ->
                            TimerError.Preset(PresetError.TimerRunning)

                        else ->
                            TimerError.Preset(PresetError.NotFound)
                    }
                    emitSideEffect(ShowError(timerError))
                }
            )
        }
    }

    /**
     * 현재 시간을 프리셋으로 저장
     */
    private fun handleSaveAsPreset(name: String, duration: Duration, colorIndex: Int) {
        LogUtil.d("name=$name, duration=$duration, colorIndex=$colorIndex")

        viewModelScope.launch {
            val result = managePresetUseCase.addPreset(
                name = name,
                duration = duration,
                colorIndex = colorIndex
            )

            result.fold(
                onSuccess = {
                    LogUtil.d("프리셋 저장 성공")
                    emitSideEffect(
                        TimerSideEffect.ShowSnackbar(
                            R.string.snackbar_preset_saved,
                            listOf(name)
                        )
                    )
                },
                onFailure = { error ->
                    LogUtil.e("프리셋 저장 실패", error)
                    val timerError = when (error) {
                        is PresetException.InvalidDuration -> TimerError.Preset(PresetError.NoTime)
                        is PresetException.MaxCountExceeded -> TimerError.Preset(
                            PresetError.MaxCount,
                            listOf(error.maxCount.toString())
                        )

                        is PresetException.InvalidName -> TimerError.Preset(PresetError.InvalidName)
                        is PresetException.NameTooLong -> TimerError.Preset(
                            PresetError.NameTooLong,
                            listOf(error.maxLength.toString())
                        )

                        else -> TimerError.Preset(PresetError.FailSave)
                    }
                    emitSideEffect(ShowError(timerError))
                }
            )
        }
    }

    /**
     * 프리셋 삭제 처리
     */
    private fun handleDeletePreset(presetId: Int) {
        LogUtil.d("presetId=$presetId")

        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                LogUtil.d("프리셋 찾음, name=${preset.name}")
                val result = managePresetUseCase.deletePreset(preset)
                result.fold(
                    onSuccess = {
                        LogUtil.d("프리셋 삭제 성공")
                        emitSideEffect(
                            TimerSideEffect.ShowSnackbar(
                                R.string.snackbar_preset_deleted,
                                listOf(preset.name)
                            )
                        )
                        // 삭제된 프리셋이 선택되어 있었다면 선택 해제
                        if (_uiState.value.selectedPreset?.id == presetId) {
                            LogUtil.d("선택된 프리셋 해제")
                            _uiState.update { it.copy(selectedPreset = null) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("프리셋 삭제 실패", error)
                        emitSideEffect(
                            ShowError(
                                TimerError.Preset(
                                    PresetError.FailDelete,
                                )
                            ),
                        )
                    }
                )
            } else {
                LogUtil.w("프리셋을 찾을 수 없음, presetId=$presetId")
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
        LogUtil.d("presetId=$presetId, name=$name, duration=$duration, colorIndex=$colorIndex")

        viewModelScope.launch {
            val existingPreset = _uiState.value.presets.find { it.id == presetId }
            if (existingPreset != null) {
                LogUtil.d("프리셋 찾음, oldName=${existingPreset.name}")
                val updatedPreset =
                    existingPreset.copy(name = name, duration = duration, colorIndex = colorIndex)
                val result = managePresetUseCase.updatePreset(updatedPreset)
                result.fold(
                    onSuccess = {
                        LogUtil.d("프리셋 수정 성공")
                        emitSideEffect(
                            TimerSideEffect.ShowSnackbar(
                                R.string.snackbar_preset_updated,
                                listOf(name)
                            )
                        )

                        // 수정된 프리셋이 현재 선택되어 있다면 타이머도 동기화
                        if (_uiState.value.selectedPreset?.id == presetId && !_uiState.value.isTimerActiveOrPaused) {
                            LogUtil.d("선택된 프리셋이므로 타이머 동기화")
                            handleSetTime(duration)
                            _uiState.update { it.copy(selectedPreset = preset) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("프리셋 수정 실패", error)
                        emitSideEffect(
                            ShowError(
                                TimerError.Preset(
                                    PresetError.FailUpdate,
                                ),
                            ),
                        )
                    }
                )
            } else {
                LogUtil.w("프리셋을 찾을 수 없음, presetId=$presetId")
            }
        }
    }

    /**
     * 알림 소리 및 진동 재생
     */
    private fun playNotificationSound() {
        viewModelScope.launch {
            try {
                // 설정에서 알림 소리 타입과 진동 설정 가져오기
                val soundType = settingsRepository.notificationSoundType.first()
                val isVibrate = settingsRepository.isNotificationVibrate.first()

                LogUtil.d("알림 재생: soundType=$soundType, isVibrate=$isVibrate")

                // 알림 소리 및 진동 재생
                notificationSoundPlayer.playTimerComplete(vibrate = isVibrate)
            } catch (e: Exception) {
                LogUtil.e("알림 재생 실패", e)
            }
        }
    }

    /**
     * Side Effect 전송
     */
    private fun emitSideEffect(sideEffect: TimerSideEffect) {
        LogUtil.d("${sideEffect::class.simpleName}")
        viewModelScope.launch {
            _sideEffect.send(sideEffect)
        }
    }

    private fun calculateProgress(remainingTime: Duration): Float {
        return if (remainingTime > Duration.ZERO) {
            (remainingTime / MAX_TIME).coerceIn(0.0, 1.0).toFloat()
        } else 0f
    }

    /**
     * SessionException을 TimerError로 변환
     */
    private fun mapSessionExceptionToTimerError(
        error: Throwable,
        isUpdate: Boolean
    ): TimerError.Session {
        val code = when (error) {
            is SessionException.SessionNotFound -> SessionError.NotFound
            is SessionException.InvalidDuration -> SessionError.InvalidDuration
            is SessionException.InvalidTimeRange -> SessionError.InvalidTimeRange
            is SessionException.MissingStartTime -> SessionError.MissingStartTime
            else -> if (isUpdate) SessionError.FailUpdate else SessionError.FailSave
        }
        return TimerError.Session(code)
    }

    /**
     * ViewModel이 제거될 때 타이머 정리
     */
    override fun onCleared() {
        super.onCleared()
        LogUtil.d("ViewModel 정리 중")
//        timerManager.cancelAll()
        notificationSoundPlayer.stop() // 알림 소리 정지
        _sideEffect.close() // Channel 정리
    }

    companion object {
        // 타이머 설정
        val MAX_TIME = 60.minutes
        const val MAX_DRAG_MINUTES = 60
        const val MIN_DRAG_MINUTES = 1

        // 햅틱 피드백 설정
        private val HAPTIC_FEEDBACK_START_TIME = 5.seconds
        private val HAPTIC_FEEDBACK_RANGE = 1.seconds..HAPTIC_FEEDBACK_START_TIME

        // 로그 설정
        private const val LOG_INTERVAL_SECONDS = 10L
    }
}
