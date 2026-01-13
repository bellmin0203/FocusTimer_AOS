package com.jm.harufocus.domain.usecase.statistics

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.model.statistics.MonthlyStats
import com.jm.harufocus.domain.model.statistics.WeeklyFocusTime
import com.jm.harufocus.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 월간 통계를 조회하는 UseCase
 */
class GetMonthlyStatsUseCase @Inject constructor(
    private val timerSessionRepository: TimerSessionRepository
) {

    companion object {
        // TODO: 사용자가 한 주의 시작/끝 요일을 선택할 수 있도록 개선 필요
        private val WEEK_START_DAY = DayOfWeek.MONDAY
        private val WEEK_END_DAY = DayOfWeek.SUNDAY

        // 트렌드 계산 상수
        private const val TREND_MIN_VALUE = -1.0
        private const val TREND_MAX_VALUE = 1.0
        private const val TREND_FULL_INCREASE = 1.0
        private const val TREND_NO_CHANGE = 0.0
    }

    /**
     * 특정 월의 통계를 조회합니다
     *
     * @param yearMonth 조회할 년월 (기본값: 이번 달)
     * @return 월간 통계
     */
    suspend operator fun invoke(yearMonth: YearMonth = YearMonth.now()): MonthlyStats {
        val zoneId = ZoneId.systemDefault()

        // 현재 월과 이전 월의 데이터 조회
        val currentMonthData = getMonthData(yearMonth, zoneId)
        val previousMonthData = getMonthData(yearMonth.minusMonths(1), zoneId)

        // 트렌드 계산
        val monthOverMonthGrowthRate = calculateMonthOverMonthGrowthRate(
            currentMonthFocusTime = currentMonthData.totalFocusTime,
            previousMonthFocusTime = previousMonthData.totalFocusTime
        )

        return buildMonthlyStats(
            yearMonth = yearMonth,
            monthData = currentMonthData,
            growthRate = monthOverMonthGrowthRate
        )
    }

    /**
     * 특정 월의 집계 데이터를 조회합니다
     */
    private suspend fun getMonthData(yearMonth: YearMonth, zoneId: ZoneId): MonthData {
        val (startMillis, endMillis) = yearMonth.toEpochMillisRange(zoneId)

        // 해당 월의 완료된 세션들 조회
        val completedSessions = timerSessionRepository
            .getCompletedSessionsBetween(startMillis, endMillis)
            .first()

        // 주별 통계 초기화 및 집계
        val monthStartDate = yearMonth.atDay(1)
        val monthEndDate = yearMonth.atEndOfMonth()
        val weeklyStats = initializeWeeklyStats(monthStartDate, monthEndDate)
        val aggregatedWeeklyStats = aggregateSessionsByWeek(
            sessions = completedSessions,
            weeklyStats = weeklyStats,
            monthStartDate = monthStartDate,
            zoneId = zoneId
        )

        val weeklyBreakdown = aggregatedWeeklyStats.values.sortedBy { it.weekOfMonth }
        val totalFocusTime = completedSessions.calculateTotalDuration()

        return MonthData(
            weeklyBreakdown = weeklyBreakdown,
            totalFocusTime = totalFocusTime
        )
    }

    /**
     * 주별 통계를 초기화합니다 (모든 주를 Duration.ZERO로)
     */
    private fun initializeWeeklyStats(
        monthStartDate: LocalDate,
        monthEndDate: LocalDate
    ): MutableMap<Int, WeeklyFocusTime> {
        val weeklyStats = mutableMapOf<Int, WeeklyFocusTime>()
        var weekOfMonth = 1
        var currentDate = monthStartDate.with(TemporalAdjusters.previousOrSame(WEEK_START_DAY))

        // 첫 주가 이전 달에 시작하는 경우 해당 월의 첫날을 시작으로 설정
        if (currentDate.isBefore(monthStartDate)) {
            currentDate = monthStartDate
        }

        while (!currentDate.isAfter(monthEndDate)) {
            val weekEndDate = currentDate.with(TemporalAdjusters.nextOrSame(WEEK_END_DAY))
            val actualWeekEndDate = if (weekEndDate.isAfter(monthEndDate)) {
                monthEndDate
            } else {
                weekEndDate
            }

            weeklyStats[weekOfMonth] = WeeklyFocusTime(
                weekOfMonth = weekOfMonth,
                weekStartDate = currentDate,
                weekEndDate = actualWeekEndDate,
                focusTime = Duration.ZERO,
                sessionCount = 0
            )

            currentDate = actualWeekEndDate.plusDays(1)
            weekOfMonth++
        }

        return weeklyStats
    }

    /**
     * 세션들을 주별로 집계합니다
     */
    private fun aggregateSessionsByWeek(
        sessions: List<TimerSession>,
        weeklyStats: MutableMap<Int, WeeklyFocusTime>,
        monthStartDate: LocalDate,
        zoneId: ZoneId
    ): Map<Int, WeeklyFocusTime> {
        val monthReferenceMonday = monthStartDate.with(TemporalAdjusters.previousOrSame(WEEK_START_DAY))

        sessions.forEach { session ->
            val sessionDate = session.startTime.atZone(zoneId).toLocalDate()
            val weekOfMonth = sessionDate.getWeekOfMonth(monthReferenceMonday)

            weeklyStats[weekOfMonth]?.let { stats ->
                weeklyStats[weekOfMonth] = stats.copy(
                    focusTime = stats.focusTime + session.duration,
                    sessionCount = stats.sessionCount + 1
                )
            }
        }

        return weeklyStats
    }

    /**
     * 이전 달 대비 증감율를 계산합니다
     *
     * @return -1.0(100% 감소) ~ 1.0(100% 증가) 범위의 값
     */
    private fun calculateMonthOverMonthGrowthRate(
        currentMonthFocusTime: Duration,
        previousMonthFocusTime: Duration
    ): Double {
        // 이전 달 데이터가 없는 경우
        if (previousMonthFocusTime == Duration.ZERO) {
            return if (currentMonthFocusTime > Duration.ZERO) {
                TREND_FULL_INCREASE  // 이전 달 0분 -> 이번 달 있음 = 100% 증가
            } else {
                TREND_NO_CHANGE  // 둘 다 0분 = 변화 없음
            }
        }

        // 이전 달 대비 변화율 계산
        val changeMillis = currentMonthFocusTime.inWholeMilliseconds - previousMonthFocusTime.inWholeMilliseconds
        val growthRate = changeMillis.toDouble() / previousMonthFocusTime.inWholeMilliseconds

        // -1.0 ~ 1.0 범위로 제한 (100% 감소 ~ 100% 증가)
        return growthRate
    }

    /**
     * 최종 MonthlyStats 객체를 생성합니다
     */
    private fun buildMonthlyStats(
        yearMonth: YearMonth,
        monthData: MonthData,
        growthRate: Double
    ): MonthlyStats {
        val weeklyBreakdown = monthData.weeklyBreakdown

        // 가장 생산적인 주 계산
        val mostProductiveWeek = weeklyBreakdown
            .filter { it.focusTime > Duration.ZERO }
            .maxByOrNull { it.focusTime }
            ?.weekOfMonth

        // 주평균 세션 수 계산
        val averageSessionsPerWeek = if (weeklyBreakdown.isNotEmpty()) {
            weeklyBreakdown.sumOf { it.sessionCount }.toDouble() / weeklyBreakdown.size
        } else {
            0.0
        }

        return MonthlyStats(
            yearMonth = yearMonth,
            totalFocusTime = monthData.totalFocusTime,
            weeklyBreakdown = weeklyBreakdown,
            averageSessionsPerWeek = averageSessionsPerWeek,
            mostProductiveWeek = mostProductiveWeek,
            growthRate = growthRate
        )
    }

    // === 확장 함수 (Extension Functions) ===

    /**
     * YearMonth를 epochMillis 범위로 변환합니다
     * @return Pair(startMillis, endMillis)
     */
    private fun YearMonth.toEpochMillisRange(zoneId: ZoneId): Pair<Long, Long> {
        val startMillis = this.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = this.atEndOfMonth().plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        return startMillis to endMillis
    }

    /**
     * 특정 날짜가 월 내에서 몇 번째 주인지 계산합니다
     */
    private fun LocalDate.getWeekOfMonth(monthReferenceMonday: LocalDate): Int {
        return ChronoUnit.WEEKS.between(monthReferenceMonday, this).toInt() + 1
    }

    /**
     * TimerSession 리스트의 총 집중 시간을 계산합니다
     */
    private fun List<TimerSession>.calculateTotalDuration(): Duration {
        return this.sumOf { it.duration.inWholeMilliseconds }.milliseconds
    }

    /**
     * 월간 집계 데이터를 담는 내부 데이터 클래스
     */
    private data class MonthData(
        val weeklyBreakdown: List<WeeklyFocusTime>,
        val totalFocusTime: Duration
    )
}
