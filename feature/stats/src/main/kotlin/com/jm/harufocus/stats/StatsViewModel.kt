package com.jm.harufocus.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.harufocus.data.util.DummyDataHelper
import com.jm.harufocus.domain.usecase.statistics.GetAchievementMetricsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetDailyStatsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetMonthlyStatsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetWeeklyStatsUseCase
import com.jm.harufocus.stats.model.StatsPeriod
import com.jm.harufocus.stats.model.StatsUiState
import com.jm.harufocus.util.AnalyticsHelper
import com.jm.harufocus.util.CrashReporter
import com.jm.logutil.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 통계 화면의 ViewModel
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val getWeeklyStatsUseCase: GetWeeklyStatsUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase,
    private val getAchievementMetricsUseCase: GetAchievementMetricsUseCase,
    private val dummyDataHelper: DummyDataHelper,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var currentDate = LocalDate.now()
    private var currentYearMonth = YearMonth.now()



    init {
        LogUtil.d("StatsViewModel initialized")
        // 화면 조회 이벤트 로깅
        analyticsHelper.logScreenView("stats_screen", "StatsScreen")

        generateDummyData() // 릴리즈 빌드에서는 더미 데이터 생성하지 않음
        loadAchievementMetrics()
        loadStats()
    }

    // 디버그 전용 함수 - 테스트에서 사용
    @Suppress("unused")
    fun generateDummyData() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch {
                try {
                    dummyDataHelper.generateRandomMonthData()
                } catch (_: Exception) {
                    // 에러 무시 (디버그용)
                }
            }
        }
    }

    /**
     * 통계 기간 변경
     */
    fun selectPeriod(period: StatsPeriod) {
        LogUtil.d("Period selected: $period")

        // Analytics 이벤트 로깅
        val periodStr = when (period) {
            StatsPeriod.DAILY -> AnalyticsHelper.StatsPeriod.DAILY
            StatsPeriod.WEEKLY -> AnalyticsHelper.StatsPeriod.WEEKLY
            StatsPeriod.MONTHLY -> AnalyticsHelper.StatsPeriod.MONTHLY
        }
        analyticsHelper.logStatsPeriodChanged(periodStr)

        _uiState.update { it.copy(selectedPeriod = period) }
        loadStats()
    }

    /**
     * 이전 기간으로 이동
     */
    fun navigateToPreviousPeriod() {
        val periodStr = when (_uiState.value.selectedPeriod) {
            StatsPeriod.DAILY -> AnalyticsHelper.StatsPeriod.DAILY
            StatsPeriod.WEEKLY -> AnalyticsHelper.StatsPeriod.WEEKLY
            StatsPeriod.MONTHLY -> AnalyticsHelper.StatsPeriod.MONTHLY
        }
        analyticsHelper.logStatsNavigation(AnalyticsHelper.Direction.PREVIOUS, periodStr)

        when (_uiState.value.selectedPeriod) {
            StatsPeriod.DAILY -> {
                currentDate = currentDate.minusDays(1)
                LogUtil.d("Navigate to previous day: $currentDate")
            }

            StatsPeriod.WEEKLY -> {
                currentDate = currentDate.minusWeeks(1)
                LogUtil.d("Navigate to previous week: $currentDate")
            }

            StatsPeriod.MONTHLY -> {
                currentYearMonth = currentYearMonth.minusMonths(1)
                LogUtil.d("Navigate to previous month: $currentYearMonth")
            }
        }
        loadStats()
    }

    /**
     * 다음 기간으로 이동
     */
    fun navigateToNextPeriod() {
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()

        val periodStr = when (_uiState.value.selectedPeriod) {
            StatsPeriod.DAILY -> AnalyticsHelper.StatsPeriod.DAILY
            StatsPeriod.WEEKLY -> AnalyticsHelper.StatsPeriod.WEEKLY
            StatsPeriod.MONTHLY -> AnalyticsHelper.StatsPeriod.MONTHLY
        }

        when (_uiState.value.selectedPeriod) {
            StatsPeriod.DAILY -> {
                if (currentDate.isBefore(today)) {
                    currentDate = currentDate.plusDays(1)
                    LogUtil.d("Navigate to next day: $currentDate")
                    analyticsHelper.logStatsNavigation(AnalyticsHelper.Direction.NEXT, periodStr)
                    loadStats()
                }
            }

            StatsPeriod.WEEKLY -> {
                if (currentDate.plusWeeks(1).isBefore(today) || currentDate.plusWeeks(1)
                        .isEqual(today)
                ) {
                    currentDate = currentDate.plusWeeks(1)
                    LogUtil.d("Navigate to next week: $currentDate")
                    analyticsHelper.logStatsNavigation(AnalyticsHelper.Direction.NEXT, periodStr)
                    loadStats()
                }
            }

            StatsPeriod.MONTHLY -> {
                if (currentYearMonth.isBefore(currentMonth)) {
                    currentYearMonth = currentYearMonth.plusMonths(1)
                    LogUtil.d("Navigate to next month: $currentYearMonth")
                    analyticsHelper.logStatsNavigation(AnalyticsHelper.Direction.NEXT, periodStr)
                    loadStats()
                }
            }
        }
    }

    /**
     * 오늘/이번 주/이번 달로 이동
     */
    fun navigateToToday() {
        val periodStr = when (_uiState.value.selectedPeriod) {
            StatsPeriod.DAILY -> AnalyticsHelper.StatsPeriod.DAILY
            StatsPeriod.WEEKLY -> AnalyticsHelper.StatsPeriod.WEEKLY
            StatsPeriod.MONTHLY -> AnalyticsHelper.StatsPeriod.MONTHLY
        }
        analyticsHelper.logStatsNavigation(AnalyticsHelper.Direction.TODAY, periodStr)

        currentDate = LocalDate.now()
        currentYearMonth = YearMonth.now()
        LogUtil.d("Navigate to today")
        loadStats()
    }

    /**
     * 통계 데이터 로드
     */
    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                when (_uiState.value.selectedPeriod) {
                    StatsPeriod.DAILY -> {
                        val dailyStats = getDailyStatsUseCase(currentDate)
                        _uiState.update {
                            it.copy(
                                dailyStats = dailyStats, isLoading = false
                            )
                        }
                        LogUtil.d("Daily stats loaded: ${dailyStats.totalFocusTime}, ${dailyStats.completedSessions} sessions")
                    }

                    StatsPeriod.WEEKLY -> {
                        val weeklyStats = getWeeklyStatsUseCase(currentDate)
                        _uiState.update {
                            it.copy(
                                weeklyStats = weeklyStats, isLoading = false
                            )
                        }
                        LogUtil.d("Weekly stats loaded: ${weeklyStats.totalFocusTime}, ${weeklyStats.totalSessions} sessions")
                    }

                    StatsPeriod.MONTHLY -> {
                        val monthlyStats = getMonthlyStatsUseCase(currentYearMonth)
                        _uiState.update {
                            it.copy(
                                monthlyStats = monthlyStats, isLoading = false
                            )
                        }
                        LogUtil.d("Monthly stats loaded: ${monthlyStats.totalFocusTime}, ${monthlyStats.totalSessions} sessions")
                    }
                }
            } catch (e: Exception) {
                LogUtil.e("Failed to load stats", e)
                CrashReporter.recordException(e, "통계 로드 실패: ${_uiState.value.selectedPeriod}")
                _uiState.update {
                    it.copy(
                        isLoading = false, error = "통계를 불러오는데 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 성취 지표 로드
     */
    private fun loadAchievementMetrics() {
        viewModelScope.launch {
            try {
                val achievementMetrics = getAchievementMetricsUseCase()
                _uiState.update {
                    it.copy(achievementMetrics = achievementMetrics)
                }
                LogUtil.d("Achievement metrics loaded: focusRate=${achievementMetrics.focusRatePercent}%%, consecutiveDays=${achievementMetrics.consecutiveFocusDays}")
            } catch (e: Exception) {
                LogUtil.e("Failed to load achievement metrics", e)
                CrashReporter.recordException(e, "성취 지표 로드 실패")
                // 성취 지표 로드 실패는 에러로 표시하지 않음 (옵션)
            }
        }
    }

    /**
     * 에러 클리어
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
