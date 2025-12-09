package com.jm.focustimer.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource
import com.jm.focustimer.domain.model.preset.Preset
import com.jm.focustimer.domain.model.session.TimerSession
import com.jm.focustimer.domain.usecase.preset.AddPresetUseCase
import com.jm.focustimer.domain.usecase.preset.DeletePresetUseCase
import com.jm.focustimer.domain.usecase.preset.GetAllPresetsUseCase
import com.jm.focustimer.domain.usecase.preset.UpdatePresetUseCase
import com.jm.focustimer.domain.usecase.session.SaveTimerSessionUseCase
import com.jm.focustimer.domain.usecase.session.UpdateTimerSessionUseCase
import com.jm.focustimer.timer.model.HapticPattern
import com.jm.focustimer.timer.model.PresetError
import com.jm.focustimer.timer.model.SetTimeError
import com.jm.focustimer.timer.model.TimerError
import com.jm.focustimer.timer.model.TimerIntent
import com.jm.focustimer.timer.model.TimerSideEffect
import com.jm.focustimer.timer.model.TimerSideEffect.HapticFeedback
import com.jm.focustimer.timer.model.TimerSideEffect.ShowError
import com.jm.focustimer.timer.model.TimerSideEffect.ShowReminder
import com.jm.focustimer.timer.model.TimerUiState
import com.jm.focustimer.timer.usecase.TimerStatus
import com.jm.focustimer.util.NotificationSoundPlayer
import com.jm.logutil.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getAllPresetsUseCase: GetAllPresetsUseCase,
    private val addPresetUseCase: AddPresetUseCase,
    private val updatePresetUseCase: UpdatePresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase,
    private val saveTimerSessionUseCase: SaveTimerSessionUseCase,
    private val updateTimerSessionUseCase: UpdateTimerSessionUseCase,
    private val timerManager: TimerManager,
    private val settingsDataSource: SettingsPreferencesDataSource,
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
        observeTimer()
        // Intent 요청 관찰
        observeIntent()
        // 화면 켜짐 설정 관찰
        observeScreenOnSetting()
    }

    /**
     * 프리셋 목록을 관찰하여 UI 상태에 반영
     */
    private fun observePresets() {
        LogUtil.d("프리셋 목록 관찰 시작")
        getAllPresetsUseCase()
            .onEach { presets ->
                LogUtil.d("프리셋 목록 업데이트, count=${presets.size}")
                _uiState.update { it.copy(presets = presets) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * 화면 켜짐 유지 설정을 관찰하여 UI 상태에 반영
     */
    private fun observeScreenOnSetting() {
        LogUtil.d("화면 켜짐 설정 관찰 시작")
        settingsDataSource.isScreenOnFlow
            .onEach { isScreenOnEnabled ->
                LogUtil.d("화면 켜짐 설정 업데이트, isScreenOnEnabled=$isScreenOnEnabled")
                _uiState.update { it.copy(isScreenOnEnabled = isScreenOnEnabled) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTimer() {
        viewModelScope.launch {
            timerManager.timerState.collect { timerState ->
                val initialTime = timerState.initialDuration
                val remainingTime = timerState.remainingTime
                val overtime = timerState.overtime
                val isRunning = timerState.isRunning
                val isPaused = timerState.isPaused

                when (timerState.status) {
                    is TimerStatus.Idle -> {
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
                        if (remainingTime.inWholeSeconds % 10L == 0L) {
                            LogUtil.d("startTimer: Tick event, remainingTime=$remainingTime, progress=$progress")
                        }

                        _uiState.update {
                            it.copy(
                                initialTime = initialTime,
                                remainingTime = remainingTime,
                                isRunning = true,
                                isPaused = false,
                                overtime = overtime,
                                progress = progress
                            )
                        }
                        // 마지막 5초는 햅틱 피드백
                        if (remainingTime in 1.seconds..5.seconds) {
                            LogUtil.d("startTimer: 마지막 5초, 햅틱 피드백 전송")
                            sendSideEffect(HapticFeedback(HapticPattern.TICK))
                        }

                        if (timerState.isShowReminder) {
                            sendSideEffect(ShowReminder(remainingTime))
                            sendSideEffect(HapticFeedback(HapticPattern.REMINDER))
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
                            )
                        }
                        if (!timerState.isOvertime) {
                            // 타이머가 정상 완료된 시점 (초과 시간 진입 전)
                            sendSideEffect(TimerSideEffect.ShowTimerCompleted)
                            sendSideEffect(HapticFeedback(HapticPattern.COMPLETED))
                            // 알림 소리 및 진동 재생
                            playNotificationSound()

                            // 타이머가 자동 완료되었으므로 세션을 완료 상태로 업데이트
                            val currentState = _uiState.value
                            currentState.currentSessionId?.let { sessionId ->
                                viewModelScope.launch {
                                    val updateResult = updateTimerSession(
                                        state = currentState,
                                        sessionId = sessionId.toInt(),
                                        endTime = Instant.now(),
                                        completed = true,
                                        overrunTime = Duration.ZERO
                                    )

                                    updateResult.fold(
                                        onSuccess = {
                                            LogUtil.d("세션 자동 완료 업데이트 성공, sessionId=$sessionId")
                                        },
                                        onFailure = { error ->
                                            LogUtil.e("세션 자동 완료 업데이트 실패", error)
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
                sendSideEffect(ShowError(it))
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
                processIntent(intent = intent)
            }
        }
    }

    private fun processIntent(intent: TimerIntent) {
        when (intent) {
            is TimerIntent.SetTime -> handleSetTime(intent.totalTime)
            is TimerIntent.Start -> handleStart(intent.reminderThresholds)
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
    private fun handleStart(reminderThresholds: List<Duration>) {
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
            sendSideEffect(
                ShowError(
                    TimerError.SetTime(
                        SetTimeError.InvalidTime
                    )
                )
            )
            return
        }

        LogUtil.d("타이머 시작")

        // 타이머 시작 전에 세션을 DB에 저장
        val startTime = Instant.now()
        viewModelScope.launch {
            val sessionResult = saveTimerSession(
                presetId = currentState.selectedPresetId ?: 0, // 프리셋 미선택 시 0
                duration = currentState.initialTime,
                startTime = startTime
            )

            sessionResult.fold(
                onSuccess = { sessionId ->
                    LogUtil.d("세션 저장 성공, sessionId=$sessionId")
                    _uiState.update {
                        it.copy(
                            currentSessionId = sessionId,
                            sessionStartTime = startTime
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
                    // 세션 저장 실패 시에도 타이머는 시작 (데이터 저장은 부가 기능)
                    sendSideEffect(
                        TimerSideEffect.ShowSnackbar(
                            R.string.snackbar_session_save_failed
                        )
                    )
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
     * 타이머 세션을 DB에 저장합니다.
     *
     * @param presetId 사용된 프리셋 ID (선택되지 않은 경우 0)
     * @param duration 세션 지속 시간
     * @param startTime 세션 시작 시간
     * @return 저장된 세션 ID를 담은 Result
     */
    private suspend fun saveTimerSession(
        presetId: Int,
        duration: Duration,
        startTime: Instant
    ): Result<Long> {
        val session = TimerSession(
            presetId = presetId,
            startTime = startTime,
            endTime = null, // 시작 시점에는 종료 시간 없음
            duration = duration,
            completed = false, // 아직 완료되지 않음
            overrunTime = null // 초과 시간 없음
        )

        return saveTimerSessionUseCase(session)
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
                val updateResult = updateTimerSession(
                    state = currentState,
                    sessionId = sessionId.toInt(),
                    endTime = Instant.now(),
                    completed = false,
                    overrunTime = Duration.ZERO
                )

                updateResult.fold(
                    onSuccess = {
                        LogUtil.d("세션 업데이트 성공 (미완료), sessionId=$sessionId")
                    },
                    onFailure = { error ->
                        LogUtil.e("세션 업데이트 실패", error)
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
                    val updateResult = updateTimerSession(
                        state = currentState,
                        sessionId = sessionId.toInt(),
                        endTime = Instant.now(),
                        completed = true,
                        overrunTime = currentState.overtime
                    )

                    updateResult.fold(
                        onSuccess = {
                            LogUtil.d("세션 초과 시간 업데이트 성공, sessionId=$sessionId, overtime=${currentState.overtime}")
                        },
                        onFailure = { error ->
                            LogUtil.e("세션 초과 시간 업데이트 실패", error)
                            sendSideEffect(
                                TimerSideEffect.ShowSnackbar(
                                    R.string.snackbar_session_update_failed
                                )
                            )
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
        sendSideEffect(
            TimerSideEffect.ShowSnackbar(
                R.string.snackbar_timer_completed
            )
        )
    }

    /**
     * 타이머 세션을 업데이트합니다.
     *
     * @param sessionId 업데이트할 세션 ID
     * @param endTime 세션 종료 시간
     * @param completed 세션 완료 여부
     * @param overrunTime 초과 시간
     * @return 업데이트 결과를 담은 Result
     */
    private suspend fun updateTimerSession(
        state: TimerUiState,
        sessionId: Int,
        endTime: Instant,
        completed: Boolean,
        overrunTime: Duration,
    ): Result<Unit> {
        // 세션 시작 시간이 없으면 에러
        val startTime = state.sessionStartTime
            ?: return Result.failure(IllegalStateException("세션 시작 시간을 찾을 수 없습니다"))

        val updatedSession = TimerSession(
            id = sessionId,
            presetId = state.selectedPresetId ?: 0,
            startTime = startTime,
            endTime = endTime,
            duration = state.initialTime,
            completed = completed,
            overrunTime = if (overrunTime > Duration.ZERO) overrunTime else null
        )

        return updateTimerSessionUseCase(updatedSession)
    }

    /**
     * 드래그를 통한 시간 조정
     */
    private fun handleDragProgress(newProgress: Float) {
        LogUtil.d("newProgress=$newProgress, isActive=${_uiState.value.isActive}")

        // 타이머가 실행 중이 아닐 때만 드래그 가능
        if (_uiState.value.isActive) {
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
            selectedPresetId = null, // 수동 조정 시 프리셋 선택 해제
        )
        _uiState.update { newState }
    }

    /**
     * 프리셋 선택 처리
     */
    private fun handleSelectPreset(presetId: Int) {
        LogUtil.d("presetId=$presetId, isActive=${_uiState.value.isActive}")

        // 타이머가 실행 중이면 무시
        if (_uiState.value.isActive) {
            LogUtil.w("타이머 실행 중이므로 프리셋 변경 불가")
            sendSideEffect(
                TimerSideEffect.ShowError(
                    TimerError.Preset(
                        PresetError.TimerRunning,
                    ),
                ),
            )
            return
        }

        viewModelScope.launch {
            val preset = _uiState.value.presets.find { it.id == presetId }
            if (preset != null) {
                LogUtil.d("프리셋 선택됨, name=${preset.name}, duration=${preset.duration}")
                handleSetTime(preset.duration)
                _uiState.update { it.copy(selectedPresetId = presetId) }
                sendSideEffect(
                    TimerSideEffect.ShowSnackbar(
                        R.string.snackbar_preset_selected,
                        listOf(preset.name)
                    )
                )
            } else {
                LogUtil.w("프리셋을 찾을 수 없음, presetId=$presetId")
                sendSideEffect(
                    TimerSideEffect.ShowError(
                        TimerError.Preset(
                            PresetError.NotFound,
                        ),
                    )
                )
            }
        }
    }

    /**
     * 현재 시간을 프리셋으로 저장
     */
    private fun handleSaveAsPreset(name: String, duration: Duration, colorIndex: Int) {
        LogUtil.d("name=$name, duration=$duration, colorIndex=$colorIndex")

        if (duration <= 0.seconds) {
            LogUtil.w("시간이 설정되지 않음")
            sendSideEffect(
                TimerSideEffect.ShowError(
                    TimerError.Preset(
                        PresetError.NoTime,
                    ),
                ),
            )
            return
        }

        // 프리셋 최대 개수 체크
        if (_uiState.value.presets.size >= AddPresetUseCase.MAX_PRESET_COUNT) {
            LogUtil.w("프리셋 최대 개수 초과")
            sendSideEffect(
                TimerSideEffect.ShowError(
                    TimerError.Preset(
                        PresetError.MaxCount,
                    ),
                ),
            )
            return
        }

        viewModelScope.launch {
            val result = addPresetUseCase(name, duration, colorIndex)
            result.fold(
                onSuccess = { id ->
                    LogUtil.d("프리셋 저장 성공, id=$id")
                    sendSideEffect(
                        TimerSideEffect.ShowSnackbar(
                            R.string.snackbar_preset_saved,
                            listOf(name)
                        )
                    )
                },
                onFailure = { error ->
                    LogUtil.e("프리셋 저장 실패", error)
                    sendSideEffect(
                        TimerSideEffect.ShowError(
                            TimerError.Preset(
                                PresetError.FailSave,
                            ),
                        ),
                    )
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
                val result = deletePresetUseCase(preset)
                result.fold(
                    onSuccess = {
                        LogUtil.d("프리셋 삭제 성공")
                        sendSideEffect(
                            TimerSideEffect.ShowSnackbar(
                                R.string.snackbar_preset_deleted,
                                listOf(preset.name)
                            )
                        )
                        // 삭제된 프리셋이 선택되어 있었다면 선택 해제
                        if (_uiState.value.selectedPresetId == presetId) {
                            LogUtil.d("선택된 프리셋 해제")
                            _uiState.update { it.copy(selectedPresetId = null) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("프리셋 삭제 실패", error)
                        sendSideEffect(
                            TimerSideEffect.ShowError(
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
                val result = updatePresetUseCase(updatedPreset)
                result.fold(
                    onSuccess = {
                        LogUtil.d("프리셋 수정 성공")
                        sendSideEffect(
                            TimerSideEffect.ShowSnackbar(
                                R.string.snackbar_preset_updated,
                                listOf(name)
                            )
                        )

                        // 수정된 프리셋이 현재 선택되어 있다면 타이머도 동기화
                        if (_uiState.value.selectedPresetId == presetId && !_uiState.value.isActive) {
                            LogUtil.d("선택된 프리셋이므로 타이머 동기화")
                            handleSetTime(duration)
                            _uiState.update { it.copy(selectedPresetId = presetId) }
                        }
                    },
                    onFailure = { error ->
                        LogUtil.e("프리셋 수정 실패", error)
                        sendSideEffect(
                            TimerSideEffect.ShowError(
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
                val soundType = settingsDataSource.notificationSoundTypeFlow.first()
                val isVibrate = settingsDataSource.isNotificationVibrateFlow.first()

                LogUtil.d("알림 재생: soundType=$soundType, isVibrate=$isVibrate")

                // 알림 소리 및 진동 재생
                notificationSoundPlayer.play(soundType, isVibrate)
            } catch (e: Exception) {
                LogUtil.e("알림 재생 실패", e)
            }
        }
    }

    /**
     * Side Effect 전송
     */
    private fun sendSideEffect(sideEffect: TimerSideEffect) {
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
     * ViewModel이 제거될 때 타이머 정리
     */
    override fun onCleared() {
        super.onCleared()
        LogUtil.d("ViewModel 정리 중")
        timerManager.cancelAll()
        notificationSoundPlayer.stop() // 알림 소리 정지
        _sideEffect.close() // Channel 정리
    }

    companion object {
        val MAX_TIME = 60.minutes
        const val MAX_DRAG_MINUTES = 60
        const val MIN_DRAG_MINUTES = 1
    }
}