package com.jm.harufocus.timer

import com.jm.harufocus.common.di.DefaultDispatcher
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.timer.model.SetTimeError
import com.jm.harufocus.timer.model.TimerError
import com.jm.harufocus.timer.model.TimerEvent
import com.jm.harufocus.timer.usecase.TimerControlUseCase
import com.jm.harufocus.timer.usecase.TimerState
import com.jm.harufocus.timer.usecase.TimerStatus
import com.jm.harufocus.timer.usecase.TimerValidationResult
import com.jm.harufocus.util.CrashReporter
import com.jm.logutil.LogUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// TimerManager 인터페이스
interface TimerManager {
    val timerState: StateFlow<TimerState>
    val timerError: SharedFlow<TimerError>

    fun setTime(duration: Duration)
    fun start(
        initialDuration: Duration,
        duration: Duration,
        reminderThresholds: List<Duration> = emptyList()
    )

    fun pause()
    fun resume()
    fun stop(initialDuration: Duration)
    fun cancelAll()
    fun selectPreset(preset: Preset)

    // 세션 관련 메서드
    fun setSession(sessionId: Long, startTime: Instant)
    fun clearSession()
}

@Singleton
class TimerManagerImpl @Inject constructor(
    private val timerControlUseCase: TimerControlUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : TimerManager {
    private val job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(defaultDispatcher + job)
    private val _timerState = MutableStateFlow(
        TimerState(
            status = TimerStatus.Idle,
            initialDuration = Duration.ZERO,
            remainingTime = Duration.ZERO
        )
    )
    override val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _timerError = MutableSharedFlow<TimerError>(
        replay = 0,
        extraBufferCapacity = 10 // 여러 에러를 버퍼링할 수 있도록 설정
    )
    override val timerError: SharedFlow<TimerError> = _timerError.asSharedFlow()


    private var timerJob: Job? = null

    // 마지막 Tick 시점의 타임스탬프와 남은 시간 (pause 시 정확한 시간 계산용)
    private var lastTickTimestamp: Long = 0L
    private var lastTickRemainingTime: Duration = Duration.ZERO

    override fun setTime(duration: Duration) {
        if (_timerState.value.status !is TimerStatus.Idle) {
            scope.launch { _timerError.emit(TimerError.SetTime(SetTimeError.TimerRunning)) }
            return
        }

        val validationResult = timerControlUseCase.validateTimerTime(duration)
        when (validationResult) {
            TimerValidationResult.InvalidTime -> {
                scope.launch { _timerError.emit(TimerError.SetTime(SetTimeError.InvalidTime)) }
            }

            TimerValidationResult.Valid -> {
                _timerState.update {
                    it.copy(
                        status = TimerStatus.Idle,
                        initialDuration = duration,
                        remainingTime = duration
                    )
                }
            }
        }
    }

    override fun start(
        initialDuration: Duration,
        duration: Duration,
        reminderThresholds: List<Duration>
    ) {
        timerJob?.cancel()

        timerJob = scope.launch {
            try {
                timerControlUseCase.startTimer(duration, reminderThresholds)
                    .onEach { event ->
                        handleTimerEvent(
                            event,
                            initialDuration,
                            reminderThresholds
                        )
                    }
                    .catch { exception -> handleTimerError(exception) }
                    .launchIn(this)
            } catch (e: Exception) {
                handleTimerError(e)
            }
        }
    }

    private fun handleTimerEvent(
        event: TimerEvent,
        initialDuration: Duration,
        reminderThresholds: List<Duration>
    ) {
        when (event) {
            is TimerEvent.Tick -> {
                // pause 시 정확한 시간 계산을 위해 Tick 시점 기록
                lastTickTimestamp = System.currentTimeMillis()
                lastTickRemainingTime = event.remainingTime

                _timerState.update { state ->
                    state.toRunning(
                        initialDuration = initialDuration,
                        remainingTime = event.remainingTime,
                        reminderThresholds = reminderThresholds
                    )
                }
            }

            is TimerEvent.Completed -> handleTimerComplete(initialDuration)
            is TimerEvent.Reminder -> _timerState.update { state ->
                state.toRunning(
                    initialDuration = initialDuration,
                    remainingTime = event.remainingTime,
                    reminderThresholds = reminderThresholds,
                    showReminder = true
                )
            }
        }
    }

    private fun handleTimerComplete(initialDuration: Duration) {
        val currentStatus = _timerState.value.status
        // Completed나 Overtime 상태가 아닐 때만 완료 처리
        if (currentStatus !is TimerStatus.Completed && currentStatus !is TimerStatus.Overtime) {
            _timerState.update { state ->
                state.toCompleted(initialDuration)
            }
            startOvertimeTracking()
        }
    }

    private suspend fun handleTimerError(exception: Throwable) {
        LogUtil.e("타이머 실행 중 오류 발생", exception)
        CrashReporter.recordException(exception, "타이머 실행 중 오류 발생")
        _timerError.emit(TimerError.Run(message = exception.message))
        stop(initialDuration = _timerState.value.initialDuration)
    }

    private fun startOvertimeTracking() {
        timerJob?.cancel()

        timerJob = flow {
            var overtime = Duration.ZERO
            while (true) {
                delay(1.seconds)
                overtime += 1.seconds
                emit(overtime)
            }
        }.onEach { overtime ->
            _timerState.update {
                it.copy(
                    status = TimerStatus.Overtime,
                    remainingTime = Duration.ZERO,
                    overtime = overtime,
                )
            }
        }.launchIn(scope)
    }

    override fun pause() {
        if (_timerState.value.status is TimerStatus.Running) {
            timerJob?.cancel()

            // 마지막 Tick 이후 경과한 시간을 계산하여 정확한 남은 시간 산출
            val elapsedSinceLastTick = (System.currentTimeMillis() - lastTickTimestamp).milliseconds
            val accurateRemainingTime = (lastTickRemainingTime - elapsedSinceLastTick)
                .coerceAtLeast(Duration.ZERO)

            _timerState.update {
                it.copy(
                    status = TimerStatus.Paused,
                    remainingTime = accurateRemainingTime
                )
            }
        }
    }

    override fun resume() {
        val currentState = _timerState.value
        if (currentState.status is TimerStatus.Paused) {
            start(
                initialDuration = currentState.initialDuration,
                duration = currentState.remainingTime,
                reminderThresholds = currentState.reminderThresholds
            )
        }

    }

    override fun stop(initialDuration: Duration) {
        timerJob?.cancel()
        timerJob = null

        _timerState.update { state ->
            state.toIdle(initialDuration)
        }
    }


    override fun cancelAll() {
        job.cancelChildren()
    }

    override fun selectPreset(preset: Preset) {
        _timerState.update { it.copy(selectedPreset = preset) }
    }

    override fun setSession(sessionId: Long, startTime: Instant) {
        _timerState.update {
            it.copy(
                currentSessionId = sessionId,
                sessionStartTime = startTime
            )
        }
    }

    override fun clearSession() {
        _timerState.update {
            it.copy(
                currentSessionId = null,
                sessionStartTime = null
            )
        }
    }

    private fun TimerState.toRunning(
        initialDuration: Duration,
        remainingTime: Duration,
        reminderThresholds: List<Duration>,
        showReminder: Boolean = false
    ): TimerState = copy(
        status = TimerStatus.Running,
        initialDuration = initialDuration,
        remainingTime = remainingTime,
        reminderThresholds = reminderThresholds,
        isShowReminder = showReminder
    )

    private fun TimerState.toCompleted(
        initialDuration: Duration
    ): TimerState = copy(
        status = TimerStatus.Completed,
        initialDuration = initialDuration,
        remainingTime = Duration.ZERO,
        overtime = Duration.ZERO
    )

    private fun TimerState.toPaused(): TimerState = copy(
        status = TimerStatus.Paused
    )

    private fun TimerState.toIdle(
        initialDuration: Duration
    ): TimerState = copy(
        status = TimerStatus.Idle,
        initialDuration = initialDuration,
        remainingTime = initialDuration,
        overtime = Duration.ZERO,
        reminderThresholds = emptyList(),
        isShowReminder = false,
        currentSessionId = null,
        sessionStartTime = null,
    )
}
