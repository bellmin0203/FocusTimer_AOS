package com.jm.focustimer.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jm.focustimer.data.util.DummyDataHelper
import com.jm.focustimer.domain.usecase.statistics.GetAchievementMetricsUseCase
import com.jm.focustimer.domain.usecase.statistics.GetDailyStatsUseCase
import com.jm.focustimer.domain.usecase.statistics.GetMonthlyStatsUseCase
import com.jm.focustimer.domain.usecase.statistics.GetWeeklyStatsUseCase
import com.jm.focustimer.stats.model.StatsPeriod
import com.jm.focustimer.stats.model.StatsUiState
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var currentDate = LocalDate.now()
    private var currentYearMonth = YearMonth.now()



    init {
        LogUtil.d("StatsViewModel initialized")

        generateDummyData()
        loadAchievementMetrics()
        loadStats()
    }

    // 디버그 전용 함수들
    fun generateDummyData() {
        if (BuildConfig.DEBUG) {
            viewModelScope.launch {
                try {
                    val count = dummyDataHelper.generateRandomMonthData()
                    // 성공 처리
                } catch (e: Exception) {
                    // 에러 처리
                }
            }
        }
    }

    /**
     * 통계 기간 변경
     */
    fun selectPeriod(period: StatsPeriod) {
        LogUtil.d("Period selected: $period")
        _uiState.update { it.copy(selectedPeriod = period) }
        loadStats()
    }

    /**
     * 이전 기간으로 이동
     */
    fun navigateToPreviousPeriod() {
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

        when (_uiState.value.selectedPeriod) {
            StatsPeriod.DAILY -> {
                if (currentDate.isBefore(today)) {
                    currentDate = currentDate.plusDays(1)
                    LogUtil.d("Navigate to next day: $currentDate")
                    loadStats()
                }
            }

            StatsPeriod.WEEKLY -> {
                if (currentDate.plusWeeks(1).isBefore(today) || currentDate.plusWeeks(1)
                        .isEqual(today)
                ) {
                    currentDate = currentDate.plusWeeks(1)
                    LogUtil.d("Navigate to next week: $currentDate")
                    loadStats()
                }
            }

            StatsPeriod.MONTHLY -> {
                if (currentYearMonth.isBefore(currentMonth)) {
                    currentYearMonth = currentYearMonth.plusMonths(1)
                    LogUtil.d("Navigate to next month: $currentYearMonth")
                    loadStats()
                }
            }
        }
    }

    /**
     * 오늘/이번 주/이번 달로 이동
     */
    fun navigateToToday() {
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
                LogUtil.d("Achievement metrics loaded: focusRate=${achievementMetrics.focusRatePercent} %, consecutiveDays=${achievementMetrics.consecutiveFocusDays}")
            } catch (e: Exception) {
                LogUtil.e("Failed to load achievement metrics", e)
                e.printStackTrace()
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
