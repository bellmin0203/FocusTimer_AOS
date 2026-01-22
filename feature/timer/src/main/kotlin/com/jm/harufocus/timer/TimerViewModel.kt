package com.jm.harufocus.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.domain.usecase.preset.ManagePresetUseCase
import com.jm.harufocus.domain.usecase.preset.PresetException
import com.jm.harufocus.domain.usecase.review.CanRequestReviewUseCase
import com.jm.harufocus.timer.model.HapticPattern
import com.jm.harufocus.timer.model.PresetError
import com.jm.harufocus.timer.model.SetTimeError
import com.jm.harufocus.timer.model.TimerError
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.model.TimerSideEffect.HapticFeedback
import com.jm.harufocus.timer.model.TimerSideEffect.ShowError
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.usecase.TimerStatus
import com.jm.harufocus.util.AnalyticsHelper
import com.jm.harufocus.util.CrashReporter
import com.jm.harufocus.util.NotificationSoundPlayer
import com.jm.harufocus.widget.HaruFocusWidgetUpdater
import com.jm.logutil.LogUtil
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
    private val timerManager: TimerManager,
    private val settingsRepository: SettingsRepository,
    private val notificationSoundPlayer: NotificationSoundPlayer,
    private val widgetUpdater: HaruFocusWidgetUpdater,
    private val canRequestReviewUseCase: CanRequestReviewUseCase,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    // Side Effect 전달용 Channel
    private val _sideEffect = Channel<TimerSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    private val intentChannel = Channel<TimerIntent>(Channel.UNLIMITED)


    init {
        LogUtil.d("TimerViewModel initialized")
        // 화면 조회 이벤트 로깅
        analyticsHelper.logScreenView("timer_screen", "TimerScreen")
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
            combine(
                settingsRepository.isScreenOn,
                settingsRepository.isHapticFeedback,
                settingsRepository.isTickSound
            ) { screenOn, haptic, tick -> Triple(screenOn, haptic, tick) },
            combine(
                settingsRepository.isMinimizedControls,
                settingsRepository.isPulseAnimationEnabled,
                settingsRepository.isScreenRotationEnabled
            ) { minimized, pulse, screenRotation -> Triple(minimized, pulse, screenRotation) }
        ) { (screenOn, haptic, tick), (minimized, pulse, screenRotation) ->
            LogUtil.d("설정 업데이트: screenOn=$screenOn, haptic=$haptic, tick=$tick, minimized=$minimized, pulse=$pulse, screenRotation=$screenRotation")
            _uiState.update {
                it.copy(
                    isScreenOnEnabled = screenOn,
                    isHapticFeedbackEnabled = haptic,
                    isTickSoundEnabled = tick,
                    isMinimizedControlsEnabled = minimized,
                    isPulseAnimationEnabled = pulse,
                    isScreenRotationEnabled = screenRotation
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

                LogUtil.d("timerState=$timerState")

                when (timerState.status) {
                    is TimerStatus.Idle -> {
                        notificationSoundPlayer.stop() // 알림 소리 정지

                        val progress = calculateProgress(remainingTime = initialTime)
                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                status = TimerStatus.Idle,
                                overtime = Duration.ZERO,
                                progress = progress,
                                error = null,
                                selectedPreset = selectedPreset,
                                currentSessionId = null,
                                sessionStartTime = null,
                            )
                        }

                        // In-App Review 요청 가능 여부 확인
                        checkAndRequestInAppReview()
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
                                status = TimerStatus.Running,
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
                                status = TimerStatus.Paused,
                                overtime = overtime,
                                selectedPreset = selectedPreset,
                            )
                        }
                    }

                    is TimerStatus.Completed -> {
                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                status = TimerStatus.Completed,
                                overtime = overtime,
                                selectedPreset = selectedPreset,
                            )
                        }
                        // 타이머가 정상 완료된 시점 (초과 시간 진입 전)
                        emitSideEffect(TimerSideEffect.ShowTimerCompleted)

                        // 햅틱 피드백 (설정이 활성화된 경우)
                        if (_uiState.value.isHapticFeedbackEnabled) {
                            emitSideEffect(HapticFeedback(HapticPattern.COMPLETED))
                        }

                        // 알림 소리 및 진동 재생
                        playNotificationSound()

                        // 세션 완료 처리는 TimerService에서 처리됩니다
                    }

                    is TimerStatus.Overtime -> {
                        // 초과 시간 진행 중
                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                status = TimerStatus.Overtime,
                                overtime = overtime,
                                selectedPreset = selectedPreset,
                            )
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
            is TimerIntent.Start -> startTimer(intent.reminderThresholds)
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

        // 위젯 상태도 동기화
        widgetUpdater.onTimeSet(
            duration = totalTime,
            preset = _uiState.value.selectedPreset
        )
    }

    /**
     * 타이머 시작
     *
     * 유효성 검사만 수행하고, 실제 타이머 제어는 TimerService에서 처리됩니다.
     */
    private fun startTimer(reminderThresholds: List<Duration>) {
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

        // Analytics 이벤트 로깅
        val inputMethod = when {
            currentState.selectedPreset != null -> AnalyticsHelper.InputMethod.PRESET
            else -> AnalyticsHelper.InputMethod.DRAG
        }
        analyticsHelper.logTimerStarted(
            durationMinutes = currentState.initialTime.inWholeMinutes,
            presetId = currentState.selectedPreset?.id,
            presetName = currentState.selectedPreset?.name,
            inputMethod = inputMethod
        )

        LogUtil.d("타이머 시작 요청")
        // 서비스 시작 요청
        emitSideEffect(TimerSideEffect.StartTimerService(currentState.remainingTime.inWholeMilliseconds))
    }

    /**
     * 타이머 일시정지
     *
     * 유효성 검사만 수행하고, 실제 타이머 제어는 TimerService에서 처리됩니다.
     */
    private fun handlePause() {
        val currentState = _uiState.value
        LogUtil.d("isRunning=${currentState.isRunning}")

        // 실행 중이 아니면 무시
        if (!currentState.isRunning) {
            LogUtil.w("타이머가 실행 중이 아님")
            return
        }

        // Analytics 이벤트 로깅
        val elapsedMinutes = (currentState.initialTime - currentState.remainingTime).inWholeMinutes
        analyticsHelper.logTimerPaused(
            remainingMinutes = currentState.remainingTime.inWholeMinutes,
            elapsedMinutes = elapsedMinutes
        )

        LogUtil.d("타이머 일시정지 요청")
        // 서비스 일시정지 요청
        emitSideEffect(TimerSideEffect.PauseTimerService)
    }

    /**
     * 타이머 재개
     *
     * 유효성 검사만 수행하고, 실제 타이머 제어는 TimerService에서 처리됩니다.
     */
    private fun handleResume() {
        val currentState = _uiState.value
        LogUtil.d("isPaused=${currentState.isPaused}")

        // 일시정지 상태가 아니면 무시
        if (!currentState.isPaused) {
            LogUtil.w("일시정지 상태가 아님")
            return
        }

        // Analytics 이벤트 로깅
        analyticsHelper.logTimerResumed(
            remainingMinutes = currentState.remainingTime.inWholeMinutes
        )

        LogUtil.d("타이머 재개 요청")
        // 서비스 재개 요청
        emitSideEffect(TimerSideEffect.ResumeTimerService)
    }

    /**
     * 타이머 정지 및 초기화
     *
     * 실제 타이머 정지 및 세션 미완료 처리는 TimerService에서 처리됩니다.
     */
    private fun handleStop() {
        val currentState = _uiState.value

        // Analytics 이벤트 로깅 (타이머가 활성 상태일 때만)
        if (currentState.isTimerActive) {
            val elapsedMinutes = (currentState.initialTime - currentState.remainingTime).inWholeMinutes
            val completionRate = if (currentState.initialTime > Duration.ZERO) {
                ((currentState.initialTime - currentState.remainingTime) / currentState.initialTime * 100).toInt()
            } else 0
            analyticsHelper.logTimerStopped(
                remainingMinutes = currentState.remainingTime.inWholeMinutes,
                elapsedMinutes = elapsedMinutes,
                completionRate = completionRate
            )
        }

        LogUtil.d("타이머 정지 요청")
        // 서비스 정지 요청
        emitSideEffect(TimerSideEffect.StopTimerService)
    }

    /**
     * 타이머 완료 확인 처리
     *
     * 사용자가 완료 버튼을 클릭했을 때 호출됩니다.
     * 실제 세션 업데이트 및 타이머 리셋은 TimerService에서 처리됩니다.
     */
    private fun handleComplete() {
        LogUtil.d("타이머 완료 확인 요청")

        if (!_uiState.value.isCompleted) {
            LogUtil.w("완료 상태가 아님")
            return
        }

        // Analytics 이벤트 로깅
        val currentState = _uiState.value
        analyticsHelper.logTimerCompleted(
            durationMinutes = currentState.initialTime.inWholeMinutes,
            presetId = currentState.selectedPreset?.id,
            overtimeSeconds = currentState.overtime.inWholeSeconds
        )

        // 실제 완료 처리는 TimerService에서 처리
        emitSideEffect(
            TimerSideEffect.ShowSnackbar(
                R.string.snackbar_timer_completed
            )
        )
        emitSideEffect(TimerSideEffect.CompleteTimerService)
    }

    /**
     * 드래그를 통한 시간 조정
     */
    private fun handleDragProgress(newProgress: Float) {
        LogUtil.d("newProgress=$newProgress, isActive=${_uiState.value.isTimerActive}")

        // 타이머가 실행 중이 아닐 때만 드래그 가능
        if (_uiState.value.isTimerActive) {
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
            status = TimerStatus.Idle,
            overtime = Duration.ZERO,
            progress = newProgress,
            error = null,
        )
        _uiState.update { newState }

        // 위젯 상태 동기화
        widgetUpdater.onTimeSet(
            duration = updatedMinutes,
            preset = _uiState.value.selectedPreset
        )
    }

    /**
     * 프리셋 선택 처리
     */
    private fun handleSelectPreset(presetId: Int) {
        LogUtil.d("presetId=$presetId, isActive=${_uiState.value.isTimerActive}")

        viewModelScope.launch {
            val result = managePresetUseCase.selectPreset(
                presetId = presetId,
                presets = _uiState.value.presets,
                isTimerActive = _uiState.value.isTimerActive
            )

            result.fold(
                onSuccess = { preset ->
                    LogUtil.d("프리셋 선택됨, name=${preset.name}, duration=${preset.duration}")
                    val initialTime = preset.duration
                    val remainingTime = preset.duration
                    val progress = calculateProgress(remainingTime)

                    timerManager.setTime(duration = preset.duration)

                    // 위젯 상태 동기화 (preset 정보 포함)
                    widgetUpdater.onTimeSet(
                        duration = preset.duration,
                        preset = preset
                    )

                    _uiState.update {
                        it.copy(
                            initialTime = initialTime,
                            remainingTime = remainingTime,
                            progress = progress,
                            selectedPreset = preset
                        )
                    }

                    timerManager.selectPreset(preset)

                    // Crashlytics에 프리셋 정보 기록
                    CrashReporter.setSelectedPreset(preset.id.toLong(), preset.name)

                    // Analytics 이벤트 로깅
                    analyticsHelper.logPresetSelected(
                        presetId = preset.id,
                        presetName = preset.name,
                        durationMinutes = preset.duration.inWholeMinutes
                    )

                    emitSideEffect(
                        TimerSideEffect.ShowSnackbar(
                            R.string.snackbar_preset_selected,
                            listOf(preset.name)
                        )
                    )
                },
                onFailure = { error ->
                    LogUtil.e("프리셋 선택 실패", error)
                    CrashReporter.recordException(error, "프리셋 선택 실패")
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

                    // Analytics 이벤트 로깅
                    analyticsHelper.logPresetCreated(
                        presetName = name,
                        durationMinutes = duration.inWholeMinutes,
                        colorIndex = colorIndex,
                        presetCount = _uiState.value.presets.size + 1
                    )

                    emitSideEffect(
                        TimerSideEffect.ShowSnackbar(
                            R.string.snackbar_preset_saved,
                            listOf(name)
                        )
                    )
                },
                onFailure = { error ->
                    LogUtil.e("프리셋 저장 실패", error)
                    CrashReporter.recordException(error, "프리셋 저장 실패")
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

                        // Analytics 이벤트 로깅
                        analyticsHelper.logPresetDeleted(
                            presetId = preset.id,
                            durationMinutes = preset.duration.inWholeMinutes
                        )

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
                        CrashReporter.recordException(error, "프리셋 삭제 실패")
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

                        // Analytics 이벤트 로깅
                        analyticsHelper.logPresetUpdated(
                            presetId = presetId,
                            oldDurationMinutes = existingPreset.duration.inWholeMinutes,
                            newDurationMinutes = duration.inWholeMinutes
                        )

                        emitSideEffect(
                            TimerSideEffect.ShowSnackbar(
                                R.string.snackbar_preset_updated,
                                listOf(name)
                            )
                        )

                        // 수정된 프리셋이 현재 선택되어 있다면 타이머도 동기화
                        if (_uiState.value.selectedPreset?.id == presetId && !_uiState.value.isTimerActive) {
                            LogUtil.d("선택된 프리셋이므로 타이머 동기화")
                            handleSetTime(duration)
                            _uiState.update { it.copy(selectedPreset = preset) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("프리셋 수정 실패", error)
                        CrashReporter.recordException(error, "프리셋 수정 실패")
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
                CrashReporter.recordException(e, "알림 재생 실패")
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

    /**
     * In-App Review 요청 가능 여부를 확인하고, 가능하면 SideEffect를 발생시킵니다.
     */
    private fun checkAndRequestInAppReview() {
        viewModelScope.launch {
            try {
                if (canRequestReviewUseCase()) {
                    LogUtil.d("In-App Review 요청 조건 충족")
                    emitSideEffect(TimerSideEffect.RequestInAppReview)
                }
            } catch (e: Exception) {
                LogUtil.e("In-App Review 요청 가능 여부 확인 실패", e)
            }
        }
    }

    private fun calculateProgress(remainingTime: Duration): Float {
        return if (remainingTime > Duration.ZERO) {
            (remainingTime / MAX_TIME).coerceIn(0.0, 1.0).toFloat()
        } else 0f
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
