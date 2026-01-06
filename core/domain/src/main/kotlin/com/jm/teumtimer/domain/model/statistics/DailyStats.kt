package com.jm.teumtimer.domain.model.statistics

import java.time.LocalDate
import kotlin.time.Duration

/**
 * 일일 통계 데이터
 *
 * @param date 날짜
 * @param totalFocusTime 총 집중 시간
 * @param completedSessions 완료된 세션 수
 * @param hourlyBreakdown 시간대별 분석 (0-23시)
 * @param mostProductiveHour 가장 생산적인 시간대
 */
data class DailyStats(
    val date: LocalDate,
    val totalFocusTime: Duration,
    val completedSessions: Int,
    val hourlyBreakdown: List<HourlyStats>,
    val mostProductiveHour: Int?
) {
    /**
     * 평균 세션 길이를 계산합니다
     */
    val averageSessionLength: Duration
        get() = if (completedSessions > 0) {
            totalFocusTime / completedSessions
        } else {
            Duration.ZERO
        }
}
