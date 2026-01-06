package com.jm.teumtimer.domain.model.statistics

import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration

/**
 * 주별 집중 시간 데이터
 *
 * @param weekOfMonth 주 번호 (1-5)
 * @param weekStartDate 주의 시작 날짜
 * @param weekEndDate 주의 종료 날짜
 * @param focusTime 집중 시간
 * @param sessionCount 완료된 세션 수
 */
data class WeeklyFocusTime(
    val weekOfMonth: Int,
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val focusTime: Duration,
    val sessionCount: Int
)

/**
 * 월간 통계 데이터
 *
 * @param yearMonth 년월
 * @param totalFocusTime 월간 총 집중 시간
 * @param weeklyBreakdown 주별 분석
 * @param averageSessionsPerWeek 주평균 세션 수
 * @param mostProductiveWeek 가장 생산적인 주
 * @param growthRate 이전 월 대비 증감률 (-1.0 ~ 1.0)
 */
data class MonthlyStats(
    val yearMonth: YearMonth,
    val totalFocusTime: Duration,
    val weeklyBreakdown: List<WeeklyFocusTime>,
    val averageSessionsPerWeek: Double,
    val mostProductiveWeek: Int?,
    val growthRate: Double
) {
    /**
     * 총 완료 세션 수를 계산합니다
     */
    val totalSessions: Int
        get() = weeklyBreakdown.sumOf { it.sessionCount }

    /**
     * 주평균 집중 시간을 계산합니다
     */
    val averageWeeklyFocusTime: Duration
        get() = if (weeklyBreakdown.isNotEmpty()) {
            totalFocusTime / weeklyBreakdown.size
        } else {
            Duration.ZERO
        }
}
