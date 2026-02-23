package com.jm.harufocus.stats

import com.jm.harufocus.data.util.DummyDataHelper
import com.jm.harufocus.domain.model.statistics.AchievementMetrics
import com.jm.harufocus.domain.model.statistics.DailyStats
import com.jm.harufocus.domain.model.statistics.MonthlyStats
import com.jm.harufocus.domain.model.statistics.WeeklyStats
import com.jm.harufocus.domain.repository.UserPreferencesRepository
import com.jm.harufocus.domain.usecase.statistics.GetAchievementMetricsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetDailyStatsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetMonthlyStatsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetWeeklyStatsUseCase
import com.jm.harufocus.stats.model.StatsPeriod
import com.jm.harufocus.stats.model.StatsUiState
import com.jm.harufocus.util.AnalyticsHelper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent

/**
 * 📊 StatsTestHelper (Robot Pattern)
 *
 * 통계 ViewModel 테스트를 위한 헬퍼 클래스입니다.
 * ViewModel 생성, 의존성 모킹, 상태 검증을 담당합니다.
 */
@ExperimentalCoroutinesApi
class StatsViewModelRobot(
    private val scope: TestScope,
    val getDailyStatsUseCase: GetDailyStatsUseCase = mockk(relaxed = true),
    val getWeeklyStatsUseCase: GetWeeklyStatsUseCase = mockk(relaxed = true),
    val getMonthlyStatsUseCase: GetMonthlyStatsUseCase = mockk(relaxed = true),
    val getAchievementMetricsUseCase: GetAchievementMetricsUseCase = mockk(relaxed = true),
    val userPreferencesRepository: UserPreferencesRepository = mockk(relaxed = true),
    val dummyDataHelper: DummyDataHelper = mockk(relaxed = true),
    val analyticsHelper: AnalyticsHelper = mockk(relaxed = true)
) {
    lateinit var viewModel: StatsViewModel

    fun createViewModel() {
        every { userPreferencesRepository.isStatsChartTooltipShown } returns flowOf(false)

        viewModel = StatsViewModel(
            getDailyStatsUseCase = getDailyStatsUseCase,
            getWeeklyStatsUseCase = getWeeklyStatsUseCase,
            getMonthlyStatsUseCase = getMonthlyStatsUseCase,
            getAchievementMetricsUseCase = getAchievementMetricsUseCase,
            userPreferencesRepository = userPreferencesRepository,
            dummyDataHelper = dummyDataHelper,
            analyticsHelper = analyticsHelper
        )

        runCurrent()
    }

    // --- 🎬 Actions (Given / When) ---

    fun setupDailyStats(stats: DailyStats) {
        coEvery { getDailyStatsUseCase(any()) } returns stats
    }

    fun setupWeeklyStats(stats: WeeklyStats) {
        coEvery { getWeeklyStatsUseCase(any()) } returns stats
    }

    fun setupMonthlyStats(stats: MonthlyStats) {
        coEvery { getMonthlyStatsUseCase(any()) } returns stats
    }

    fun setupAchievementMetrics(metrics: AchievementMetrics) {
        coEvery { getAchievementMetricsUseCase() } returns metrics
    }

    fun setupChartTooltipShown(isShown: Boolean) {
        every { userPreferencesRepository.isStatsChartTooltipShown } returns flowOf(isShown)
    }

    fun selectPeriod(period: StatsPeriod) {
        viewModel.selectPeriod(period)
        scope.runCurrent()
    }

    fun navigateToPreviousPeriod() {
        viewModel.navigateToPreviousPeriod()
        scope.runCurrent()
    }

    fun navigateToNextPeriod() {
        viewModel.navigateToNextPeriod()
        scope.runCurrent()
    }

    fun navigateToToday() {
        viewModel.navigateToToday()
        scope.runCurrent()
    }

    fun dismissChartTooltip() {
        viewModel.dismissChartTooltip()
        scope.runCurrent()
    }

    fun runCurrent() {
        scope.runCurrent()
    }

    // --- ✅ Assertions (Then) ---

    fun verifyState(block: StatsUiState.() -> Unit) {
        block(viewModel.uiState.value)
    }

    fun verifyChartTooltipStateSaved() {
        coVerify { userPreferencesRepository.updateIsStatsChartTooltipShown(true) }
    }
}
