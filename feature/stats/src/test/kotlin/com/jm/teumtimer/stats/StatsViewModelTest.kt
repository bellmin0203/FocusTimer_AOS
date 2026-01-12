package com.jm.teumtimer.stats

import com.jm.logutil.LogUtil
import com.jm.teumtimer.data.util.DummyDataHelper
import com.jm.teumtimer.domain.model.statistics.AchievementMetrics
import com.jm.teumtimer.domain.model.statistics.DailyStats
import com.jm.teumtimer.domain.model.statistics.MonthlyStats
import com.jm.teumtimer.domain.model.statistics.WeeklyStats
import com.jm.teumtimer.domain.usecase.statistics.GetAchievementMetricsUseCase
import com.jm.teumtimer.domain.usecase.statistics.GetDailyStatsUseCase
import com.jm.teumtimer.domain.usecase.statistics.GetMonthlyStatsUseCase
import com.jm.teumtimer.domain.usecase.statistics.GetWeeklyStatsUseCase
import com.jm.teumtimer.stats.model.StatsPeriod
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest : BehaviorSpec({

    // 테스트용 Dispatcher 설정
    val testDispatcher = StandardTestDispatcher()

    // Mocks
    val getDailyStatsUseCase: GetDailyStatsUseCase = mockk()
    val getWeeklyStatsUseCase: GetWeeklyStatsUseCase = mockk()
    val getMonthlyStatsUseCase: GetMonthlyStatsUseCase = mockk()
    val getAchievementMetricsUseCase: GetAchievementMetricsUseCase = mockk()
    val dummyDataHelper: DummyDataHelper = mockk(relaxed = true)

    // Dummy Response Models
    val mockDailyStats: DailyStats = mockk(relaxed = true)
    val mockWeeklyStats: WeeklyStats = mockk(relaxed = true)
    val mockMonthlyStats: MonthlyStats = mockk(relaxed = true)
    val mockAchievementMetrics: AchievementMetrics = mockk(relaxed = true)

    // Fixed Date for Testing
    val fixedDate = LocalDate.of(2026, 1, 5)

    lateinit var viewModel: StatsViewModel

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        mockkObject(LogUtil)
        mockkStatic(LocalDate::class)
        mockkStatic(YearMonth::class)
        every { LocalDate.now() } returns fixedDate
        every { YearMonth.now() } returns YearMonth.from(fixedDate)
        every { LogUtil.d(any<String>()) } just Runs
        every { LogUtil.d(any<String>(), any()) } just Runs
        every { LogUtil.e(any<String>(), any()) } just Runs
    }

    afterSpec {
        Dispatchers.resetMain()
        unmockkStatic(LocalDate::class)
        unmockkStatic(YearMonth::class)
        unmockkAll()
    }

    beforeContainer {
        clearMocks(
            getDailyStatsUseCase,
            getWeeklyStatsUseCase,
            getMonthlyStatsUseCase,
            getAchievementMetricsUseCase
        )

        coEvery { getDailyStatsUseCase(any()) } returns mockDailyStats
        coEvery { getWeeklyStatsUseCase(any()) } returns mockWeeklyStats
        coEvery { getMonthlyStatsUseCase(any()) } returns mockMonthlyStats
        coEvery { getAchievementMetricsUseCase() } returns mockAchievementMetrics

        viewModel = StatsViewModel(
            getDailyStatsUseCase,
            getWeeklyStatsUseCase,
            getMonthlyStatsUseCase,
            getAchievementMetricsUseCase,
            dummyDataHelper
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    // 각 테스트 케이스 실행 전 기본 Mock 설정
    beforeTest {

    }

    Given("StatsViewModel이 초기화될 때") {
        When("초기 상태를 확인하면") {
            Then("기본 기간은 DAILY여야 한다") {
                viewModel.uiState.value.selectedPeriod shouldBe StatsPeriod.DAILY
            }

            Then("오늘 날짜의 통계 데이터를 불러와야 한다") {
                viewModel.uiState.value.dailyStats shouldBe mockDailyStats
                coVerify(exactly = 1) { getDailyStatsUseCase(fixedDate) }
            }

            Then("성취 지표를 불러와야 한다") {
                viewModel.uiState.value.achievementMetrics shouldBe mockAchievementMetrics
                coVerify(exactly = 1) { getAchievementMetricsUseCase() }
            }
        }
    }

    Given("통계 기간을 변경할 때") {

        When("주간(WEEKLY)으로 변경하면") {
            viewModel.selectPeriod(StatsPeriod.WEEKLY)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("기간 상태가 WEEKLY로 변경되어야 한다") {
                viewModel.uiState.value.selectedPeriod shouldBe StatsPeriod.WEEKLY
            }

            Then("주간 통계 데이터를 불러와야 한다") {
                viewModel.uiState.value.weeklyStats shouldBe mockWeeklyStats
                coVerify { getWeeklyStatsUseCase(any()) }
            }
        }

        When("월간(MONTHLY)으로 변경하면") {
            viewModel.selectPeriod(StatsPeriod.MONTHLY)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("기간 상태가 MONTHLY로 변경되어야 한다") {
                viewModel.uiState.value.selectedPeriod shouldBe StatsPeriod.MONTHLY
            }

            Then("월간 통계 데이터를 불러와야 한다") {
                viewModel.uiState.value.monthlyStats shouldBe mockMonthlyStats
                coVerify { getMonthlyStatsUseCase(any()) }
            }
        }
    }

    Given("이전 기간으로 이동할 때 (Navigate Previous)") {

        When("Daily 모드에서 이전 버튼을 누르면") {
            viewModel.selectPeriod(StatsPeriod.DAILY)
            testDispatcher.scheduler.advanceUntilIdle()

            val initialCallCount = 1 // init에서 1회 호출됨
            viewModel.navigateToPreviousPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("하루 전 데이터가 로드되어야 한다") {
                val slots = mutableListOf<LocalDate>()
                // init(오늘) + navigatePrevious(어제) = 총 2회 호출 확인
                coVerify(atLeast = 2) { getDailyStatsUseCase(capture(slots)) }

                // 마지막으로 호출된 인자가 어제 날짜인지 확인
                slots.last() shouldBe fixedDate.minusDays(1)
            }
        }

        When("Weekly 모드에서 이전 버튼을 누르면") {
            viewModel.navigateToToday() // 상태 초기화 (날짜를 오늘로)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.selectPeriod(StatsPeriod.WEEKLY)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.navigateToPreviousPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("지난주 데이터가 로드되어야 한다") {
                val slots = mutableListOf<LocalDate>()
                coVerify { getWeeklyStatsUseCase(capture(slots)) }
                println("slots: $slots")

                // Weekly 로직상 currentDate.minusWeeks(1)이 호출됨
                slots.last() shouldBe fixedDate.minusWeeks(1)
            }
        }

        When("Monthly 모드에서 이전 버튼을 누르면") {
            viewModel.navigateToToday() // 상태 초기화
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.selectPeriod(StatsPeriod.MONTHLY)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.navigateToPreviousPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("지난달 데이터가 로드되어야 한다") {
                val slots = mutableListOf<YearMonth>()
                coVerify { getMonthlyStatsUseCase(capture(slots)) }

                slots.last() shouldBe YearMonth.now().minusMonths(1)
            }
        }
    }

    Given("다음 기간으로 이동할 때 (Navigate Next)") {

        When("오늘 날짜에서 다음 버튼을 누르면 (미래 이동 시도)") {
            // 초기 상태는 오늘임
            viewModel.navigateToNextPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("날짜가 변경되지 않아야 한다 (UseCase 추가 호출 없음)") {

                // init에서 1번 호출된 것 외에 추가 호출이 없어야 함
                coVerify(exactly = 1) { getDailyStatsUseCase(any()) }
            }
        }

        When("과거 날짜로 이동 후 다음 버튼을 누르면") {
            // 어제로 이동
            viewModel.navigateToPreviousPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            // 다시 내일(오늘)로 이동
            viewModel.navigateToNextPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("오늘 날짜 데이터가 다시 로드되어야 한다") {
                val slots = mutableListOf<LocalDate>()
                coVerify(atLeast = 1) { getDailyStatsUseCase(capture(slots)) }
                slots.last() shouldBe fixedDate
            }
        }
    }

    Given("오늘로 이동할 때 (Navigate To Today)") {

        When("과거 날짜에서 오늘로 이동 버튼을 누르면") {
            // 며칠 전으로 이동
            viewModel.navigateToPreviousPeriod()
            viewModel.navigateToPreviousPeriod()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.navigateToToday()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("오늘 날짜 데이터가 로드되어야 한다") {
                val slots = mutableListOf<LocalDate>()
                coVerify(atLeast = 1) { getDailyStatsUseCase(capture(slots)) }
                slots.last() shouldBe fixedDate
            }
        }
    }

    Given("에러가 발생했을 때") {

        When("통계 로드 중 예외가 발생하면") {
            val errorMessage = "Network Error"
            coEvery { getDailyStatsUseCase(any()) } throws RuntimeException(errorMessage)

            // 로드 트리거 (현재 Daily 상태라고 가정)
            viewModel.selectPeriod(StatsPeriod.DAILY)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("에러 메시지가 상태에 반영되어야 한다") {
                viewModel.uiState.value.error shouldNotBe null
                viewModel.uiState.value.error shouldBe "통계를 불러오는데 실패했습니다: $errorMessage"

                // LogUtil.e가 호출되었는지 확인
                io.mockk.verify { LogUtil.e(any<String>(), any()) }
            }
        }

        When("에러를 클리어하면") {
            // 에러 상태 만들기
            viewModel.selectPeriod(StatsPeriod.DAILY)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.clearError()
            testDispatcher.scheduler.advanceUntilIdle()

            Then("에러 메시지가 사라져야 한다") {
                viewModel.uiState.value.error shouldBe null
            }
        }
    }
})
