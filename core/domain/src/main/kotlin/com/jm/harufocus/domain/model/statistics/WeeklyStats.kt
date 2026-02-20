package com.jm.harufocus.domain.model.statistics

import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration

/**
 * 일별 집중 시간 데이터
 *
 * @param date 날짜
 * @param dayOfWeek 요일
 * @param focusTime 집중 시간
 * @param fullCompletedTime 완전 완료된 세션의 총 시간
 * @param partialTime 부분 완료된 세션의 총 시간
 * @param sessionCount 완료된 세션 수
 * @param fullCompletedSessionCount 완전 완료된 세션 수
 * @param partialSessionCount 부분 완료된 세션 수
 */
data class DailyFocusTime(
    val date: LocalDate,
    val dayOfWeek: DayOfWeek,
    val focusTime: Duration,
    val fullCompletedTime: Duration = Duration.ZERO,
    val partialTime: Duration = Duration.ZERO,
    val sessionCount: Int,
    val fullCompletedSessionCount: Int = 0,
    val partialSessionCount: Int = 0
)

/**
 * 주간 통계 데이터
 *
 * @param weekStartDate 주의 시작 날짜 (월요일)
 * @param weekEndDate 주의 종료 날짜 (일요일)
 * @param totalFocusTime 주간 총 집중 시간
 * @param dailyBreakdown 일별 분석 (월~일)
 * @param averageSessionsPerDay 하루 평균 세션 수
 * @param mostProductiveDay 가장 생산적인 요일
 * @param growthRate 이전 주 대비 증감률 (-1.0 ~ 1.0, 없으면 0.0)
 */
data class WeeklyStats(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val totalFocusTime: Duration,
    val dailyBreakdown: List<DailyFocusTime>,
    val averageSessionsPerDay: Double,
    val mostProductiveDay: DayOfWeek?,
    val growthRate: Double
) {
    /**
     * 총 완료 세션 수를 계산합니다 (완전 완료 + 부분 완료)
     */
    val totalSessions: Int
        get() = dailyBreakdown.sumOf { it.sessionCount }

    /**
     * 총 완전 완료 세션 수를 계산합니다
     */
    val totalFullCompletedSessions: Int
        get() = dailyBreakdown.sumOf { it.fullCompletedSessionCount }

    /**
     * 총 부분 완료 세션 수를 계산합니다
     */
    val totalPartialSessions: Int
        get() = dailyBreakdown.sumOf { it.partialSessionCount }

    /**
     * 총 완전 완료 시간을 계산합니다.
     */
    val totalFullCompletedTime: Duration
        get() = dailyBreakdown.fold(Duration.ZERO) { acc, day -> acc + day.fullCompletedTime }

    /**
     * 총 부분 완료 시간을 계산합니다.
     */
    val totalPartialTime: Duration
        get() = dailyBreakdown.fold(Duration.ZERO) { acc, day -> acc + day.partialTime }

    /**
     * 일평균 집중 시간을 계산합니다
     */
    val averageDailyFocusTime: Duration
        get() = if (dailyBreakdown.isNotEmpty()) {
            totalFocusTime / dailyBreakdown.size
        } else {
            Duration.ZERO
        }
}
