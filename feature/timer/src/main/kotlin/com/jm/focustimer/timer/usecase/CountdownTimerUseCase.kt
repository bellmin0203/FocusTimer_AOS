package com.jm.focustimer.timer.usecase

import com.jm.focustimer.timer.model.TimerEvent
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
 *
 * @param durationMillis 타이머 총 시간 (밀리초)
 * @param reminderThresholds 리마인더를 발생시킬 시간들 (밀리초), 예: [5분(300000ms), 30초(30000ms)]
 * @return 타이머 이벤트를 방출하는 Flow
 */
class CountdownTimerUseCase @Inject constructor() {

    operator fun invoke(
        totalDuration: Duration,
        reminderThresholds: List<Duration> = emptyList()
    ): Flow<TimerEvent> = flow {
        require(totalDuration > 0.seconds) { "Duration must be positive" }
        require(reminderThresholds.all { it > 0.seconds }) { "All reminder thresholds must be positive" }

        // 리마인더 임계값을 정렬하여 중복 제거
        val sortedThresholds = reminderThresholds.distinct().sorted()
        val reminderSet = sortedThresholds.toMutableSet()

        var remainingTime = totalDuration

        while (remainingTime > 0.seconds) {
            // 현재 남은 시간에 대한 Tick 이벤트 발생
            emit(TimerEvent.Tick(remainingTime))

            // 리마인더 체크 (정확한 시간 또는 그 이하로 떨어졌을 때)
            val triggeredReminders = reminderSet.filter { threshold ->
                remainingTime <= threshold && remainingTime + TICK_INTERVAL > threshold
            }
            triggeredReminders.forEach { threshold ->
                emit(TimerEvent.Reminder(threshold))
                reminderSet.remove(threshold)
            }

            // 1초 대기
            delay(TICK_INTERVAL)
            remainingTime -= TICK_INTERVAL
        }

        // 타이머 완료
        emit(TimerEvent.Completed)
    }

    companion object {
        private val TICK_INTERVAL = 1.seconds // 1초
    }
}
