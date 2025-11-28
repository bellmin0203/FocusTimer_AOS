package com.jm.focustimer.stats.model

import com.jm.focustimer.domain.model.DailyStats
import com.jm.focustimer.domain.model.MonthlyStats
import com.jm.focustimer.domain.model.WeeklyStats

/**
 * 통계 화면의 UI 상태
 *
 * @param selectedPeriod 선택된 기간 (일간/주간/월간)
 * @param dailyStats 일간 통계
 * @param weeklyStats 주간 통계
 * @param monthlyStats 월간 통계
 * @param isLoading 로딩 중 여부
 * @param error 에러 메시지
 */
data class StatsUiState(
    val selectedPeriod: StatsPeriod = StatsPeriod.DAILY,
    val dailyStats: DailyStats? = null,
    val weeklyStats: WeeklyStats? = null,
    val monthlyStats: MonthlyStats? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 통계 기간 타입
 */
enum class StatsPeriod {
    DAILY,   // 일간
    WEEKLY,  // 주간
    MONTHLY  // 월간
}
