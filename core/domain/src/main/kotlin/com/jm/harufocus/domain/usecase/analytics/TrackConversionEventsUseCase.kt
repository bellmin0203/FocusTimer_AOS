package com.jm.harufocus.domain.usecase.analytics

import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.usecase.statistics.GetAchievementMetricsUseCase
import javax.inject.Inject
import kotlin.time.Duration

/**
 * 전환 이벤트 추적 UseCase
 *
 * 세션 완료 후 첫 번째 세션 완료, 연속 집중 일수 등의 전환 이벤트를 확인합니다.
 */
class TrackConversionEventsUseCase @Inject constructor(
    private val timerSessionRepository: TimerSessionRepository,
    private val getAchievementMetricsUseCase: GetAchievementMetricsUseCase
) {
    /**
     * 세션 완료 후 전환 이벤트를 확인합니다
     *
     * @param sessionDuration 완료된 세션의 시간
     * @return 발생한 전환 이벤트 정보
     */
    suspend operator fun invoke(sessionDuration: Duration): ConversionEvents {
        val completedSessionCount = timerSessionRepository.getCompletedSessionCount()
        val metrics = getAchievementMetricsUseCase()

        return ConversionEvents(
            isFirstSessionCompleted = completedSessionCount == 1,
            sessionDuration = sessionDuration,
            consecutiveFocusDays = metrics.consecutiveFocusDays,
            isConsecutiveDaysMilestone = isConsecutiveDaysMilestone(metrics.consecutiveFocusDays),
            totalCompletedSessions = completedSessionCount
        )
    }

    /**
     * 연속 집중 일수가 특정 마일스톤에 도달했는지 확인합니다.
     * 마일스톤: 3일, 7일, 14일, 30일, 50일, 100일
     */
    private fun isConsecutiveDaysMilestone(days: Int): Boolean {
        return days in CONSECUTIVE_DAYS_MILESTONES
    }

    companion object {
        val CONSECUTIVE_DAYS_MILESTONES = setOf(3, 7, 14, 30, 50, 100)
    }
}

/**
 * 전환 이벤트 정보
 */
data class ConversionEvents(
    /** 첫 번째 세션 완료 여부 */
    val isFirstSessionCompleted: Boolean,
    /** 완료된 세션의 시간 */
    val sessionDuration: Duration,
    /** 현재 연속 집중 일수 */
    val consecutiveFocusDays: Int,
    /** 연속 집중 일수가 마일스톤에 도달했는지 여부 */
    val isConsecutiveDaysMilestone: Boolean,
    /** 총 완료된 세션 수 */
    val totalCompletedSessions: Int
)
