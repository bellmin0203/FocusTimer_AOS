package com.jm.harufocus.domain.model.statistics

import kotlin.time.Duration

/**
 * 성취 지표 데이터
 *
 * @param focusRate 집중 달성률 (0.0 ~ 1.0, 완료된 세션 / 시작된 세션)
 * @param consecutiveFocusDays 연속 집중 일수
 * @param longestFocusTime 최장 집중 시간 (하루 중 가장 긴 집중 시간)
 * @param averageSessionLength 평균 세션 길이
 * @param totalSessions 전체 세션 수 (시작된 세션)
 * @param completedSessions 완료된 세션 수
 * @param totalFocusTime 전체 집중 시간
 */
data class AchievementMetrics(
    val focusRate: Double,
    val consecutiveFocusDays: Int,
    val longestFocusTime: Duration,
    val averageSessionLength: Duration,
    val totalSessions: Int,
    val completedSessions: Int,
    val totalFocusTime: Duration
) {
    /**
     * 집중률을 퍼센트로 변환 (0-100)
     */
    val focusRatePercent: Int
        get() = (focusRate * 100).toInt()

    /**
     * 미완료 세션 수
     */
    val incompleteSessions: Int
        get() = totalSessions - completedSessions
}
