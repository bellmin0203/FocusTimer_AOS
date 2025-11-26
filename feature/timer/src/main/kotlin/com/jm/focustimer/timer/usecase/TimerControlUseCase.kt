package com.jm.focustimer.timer.usecase

import androidx.compose.runtime.Immutable
import com.jm.focustimer.timer.model.TimerEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration

class TimerControlUseCase @Inject constructor(
    private val countdownTimerUseCase: CountdownTimerUseCase,
) {
    fun startTimer(
        duration: Duration,
        reminderThresholds: List<Duration> = emptyList()
    ): Flow<TimerEvent> {
        return countdownTimerUseCase(
            totalDuration = duration,
            reminderThresholds = reminderThresholds
        )
    }

    fun validateTimerTime(duration: Duration): TimerValidationResult {
        return when {
            duration <= Duration.ZERO -> TimerValidationResult.InvalidTime
            else -> TimerValidationResult.Valid
        }
    }
}

@Immutable
data class TimerState(
    val status: TimerStatus,
    val initialDuration: Duration,
    val remainingTime: Duration,
    val reminderThresholds: List<Duration> = emptyList(),
    val overtime: Duration = Duration.ZERO,
    val isShowReminder: Boolean = false,
) {
    val isRunning: Boolean get() = status is TimerStatus.Running
    val isPaused: Boolean get() = status is TimerStatus.Paused
    val isOvertime: Boolean get() = overtime > Duration.ZERO
}

sealed interface TimerStatus {
    data object Idle : TimerStatus
    data object Running : TimerStatus
    data object Paused : TimerStatus
    data object Completed : TimerStatus
}

enum class TimerValidationResult {
    Valid,
    InvalidTime
}
