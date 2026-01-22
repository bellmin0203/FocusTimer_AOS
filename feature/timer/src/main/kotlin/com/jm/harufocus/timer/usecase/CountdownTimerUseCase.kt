package com.jm.harufocus.timer.usecase

import com.jm.harufocus.timer.model.TimerEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 카운트다운 타이머 UseCase
 *
 * 설정된 시간만큼 1초씩 감소하며, 매 틱마다 이벤트를 발생시킵니다.
 * 특정 시간(reminderThresholds)에 도달하면 리마인더 이벤트를 발생시킵니다.
 */
class CountdownTimerUseCase @Inject constructor() {

    operator fun invoke(
        totalDuration: Duration,
        reminderThresholds: List<Duration> = emptyList()
    ): Flow<TimerEvent> = flow {
        validateInput(totalDuration, reminderThresholds)

        // 리마인더 중복 제거 및 정렬
        val pendingReminders = reminderThresholds.distinct().sorted().toMutableSet()
        var remainingTime = totalDuration

        while (remainingTime > 0.seconds) {
            emit(TimerEvent.Tick(remainingTime))

            // 이번 틱에서 발생해야 할 리마인더 확인 및 방출
            val triggered = getTriggeredReminders(remainingTime, pendingReminders)
            triggered.forEach { threshold ->
                emit(TimerEvent.Reminder(threshold))
                pendingReminders.remove(threshold)
            }

            delay(TICK_INTERVAL)
            remainingTime -= TICK_INTERVAL
        }

        emit(TimerEvent.Completed)
    }

    private fun validateInput(totalDuration: Duration, reminderThresholds: List<Duration>) {
        require(totalDuration > Duration.ZERO) { "타이머 시간은 0보다 커야 합니다. 입력된 값: $totalDuration" }
        require(reminderThresholds.all { it > 0.seconds }) { "All reminder thresholds must be positive" }
    }

    /**
     * 현재 남은 시간 기준으로 발생해야 할 리마인더 목록을 반환합니다.
     * 조건: 현재 시간(remainingTime)이 임계값 이하이고, 직전 시간(remainingTime + 1초)은 임계값보다 컸을 때
     */
    private fun getTriggeredReminders(
        remainingTime: Duration,
        pendingReminders: Set<Duration>
    ): List<Duration> {
        return pendingReminders.filter { threshold ->
            remainingTime <= threshold && (remainingTime + TICK_INTERVAL) > threshold
        }
    }

    companion object {
        private val TICK_INTERVAL = 1.seconds
    }
}