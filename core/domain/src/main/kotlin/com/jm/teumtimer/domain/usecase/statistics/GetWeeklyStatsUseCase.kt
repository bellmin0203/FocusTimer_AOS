package com.jm.teumtimer.domain.usecase.statistics

import com.jm.teumtimer.domain.model.statistics.DailyFocusTime
import com.jm.teumtimer.domain.model.statistics.WeeklyStats
import com.jm.teumtimer.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 주간 통계를 조회하는 UseCase
 */
class GetWeeklyStatsUseCase @Inject constructor(
    private val timerSessionRepository: TimerSessionRepository
) {
    /**
     * 특정 날짜가 속한 주의 통계를 조회합니다
     *
     * @param date 조회할 날짜 (기본값: 오늘, 해당 날짜가 속한 주의 통계 반환)
     * @return 주간 통계
     */
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): WeeklyStats {
        // 주의 시작(월요일)과 끝(일요일) 계산
        val weekStartDate = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEndDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val zoneId = ZoneId.systemDefault()
        val startOfWeek = weekStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfWeek = weekEndDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // 해당 주의 완료된 세션들 조회
        val sessions = timerSessionRepository.getCompletedSessionsBetween(startOfWeek, endOfWeek).first()

        // 일별 집계 (월~일)
        val dailyStatsMap = mutableMapOf<LocalDate, DailyFocusTime>()
        var currentDate = weekStartDate
        while (!currentDate.isAfter(weekEndDate)) {
            dailyStatsMap[currentDate] = DailyFocusTime(
                date = currentDate,
                dayOfWeek = currentDate.dayOfWeek,
                focusTime = 0.milliseconds,
                sessionCount = 0
            )
            currentDate = currentDate.plusDays(1)
        }

        // 세션들을 날짜별로 집계
        sessions.forEach { session ->
            val sessionDate = session.startTime.atZone(zoneId).toLocalDate()
            val currentStats = dailyStatsMap[sessionDate]
            if (currentStats != null) {
                dailyStatsMap[sessionDate] = currentStats.copy(
                    focusTime = currentStats.focusTime + session.duration,
                    sessionCount = currentStats.sessionCount + 1
                )
            }
        }

        val dailyBreakdown = dailyStatsMap.values
            .sortedBy { it.date }

        // 가장 생산적인 요일 계산
        val mostProductiveDay = dailyBreakdown
            .filter { it.focusTime > Duration.ZERO }
            .maxByOrNull { it.focusTime }
            ?.dayOfWeek

        // 총 집중 시간 계산
        val totalFocusTime = dailyBreakdown
            .sumOf { it.focusTime.inWholeMilliseconds }
            .milliseconds

        // 하루 평균 세션 수 계산
        val averageSessionsPerDay = if (dailyBreakdown.isNotEmpty()) {
            dailyBreakdown.sumOf { it.sessionCount }.toDouble() / 7.0
        } else {
            0.0
        }

        // 지난주 데이터 조회 및 증감률 계산
        val prevWeekStartDate = weekStartDate.minusWeeks(1)
        val prevWeekEndDate = weekEndDate.minusWeeks(1)
        val startOfPrevWeek = prevWeekStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfPrevWeek = prevWeekEndDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        
        val prevSessions = timerSessionRepository.getCompletedSessionsBetween(startOfPrevWeek, endOfPrevWeek).first()
        val prevTotalFocusTime = prevSessions.sumOf { it.duration.inWholeMilliseconds }.milliseconds

        val growthRate = if (prevTotalFocusTime.inWholeMinutes > 0) {
            (totalFocusTime.inWholeMinutes - prevTotalFocusTime.inWholeMinutes).toDouble() / prevTotalFocusTime.inWholeMinutes.toDouble()
        } else if (totalFocusTime.inWholeMinutes > 0) {
            1.0 // 지난주는 0인데 이번주는 있음 -> 100% 증가로 취급 (혹은 무한대?) 일반적으로 1.0 (100%) 표시
        } else {
            0.0 // 둘 다 0
        }

        return WeeklyStats(
            weekStartDate = weekStartDate,
            weekEndDate = weekEndDate,
            totalFocusTime = totalFocusTime,
            dailyBreakdown = dailyBreakdown,
            averageSessionsPerDay = averageSessionsPerDay,
            mostProductiveDay = mostProductiveDay,
            growthRate = growthRate
        )
    }
}
