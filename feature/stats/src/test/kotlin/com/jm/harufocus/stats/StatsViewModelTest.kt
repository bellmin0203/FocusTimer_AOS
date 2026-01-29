package com.jm.harufocus.stats

import com.jm.harufocus.domain.model.statistics.AchievementMetrics
import com.jm.harufocus.domain.model.statistics.DailyStats
import com.jm.harufocus.domain.model.statistics.MonthlyStats
import com.jm.harufocus.domain.model.statistics.WeeklyStats
import com.jm.harufocus.domain.usecase.statistics.GetAchievementMetricsUseCase
import com.jm.harufocus.domain.usecase.statistics.GetDailyStatsUseCase
import com.jm.harufocus.stats.model.StatsPeriod
import com.jm.harufocus.util.CrashReporter
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class StatsViewModelTest : BehaviorSpec({

    val testDispatcher: TestDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)

        mockkObject(CrashReporter)
        every { CrashReporter.recordException(any(), any()) } just Runs
    }

    afterSpec {
        Dispatchers.resetMain()

        unmockkObject(CrashReporter)
    }

    Given("ViewModel이 초기화될 때") {
        When("정상적으로 통계 데이터와 성취 지표를 불러오면") {
            Then("초기 상태와 통계 데이터가 올바르게 업데이트되어야 한다") {
                runTest {
                    val testDailyStats = DailyStats(
                        date = LocalDate.now(),
                        totalFocusTime = Duration.ZERO,
                        completedSessions = 0,
                        hourlyBreakdown = emptyList(),
                        mostProductiveHour = null
                    )
                    val testAchievementMetrics = AchievementMetrics(
                        focusRate = 0.8,
                        consecutiveFocusDays = 5,
                        longestFocusTime = 30.minutes,
                        averageSessionLength = 25.minutes,
                        totalSessions = 10,
                        completedSessions = 8,
                        totalFocusTime = 200.minutes
                    )

                    val robot = StatsViewModelRobot(this)
                    robot.setupDailyStats(testDailyStats)
                    robot.setupAchievementMetrics(testAchievementMetrics)

                    robot.createViewModel()

                    robot.verifyState {
                        selectedPeriod shouldBe StatsPeriod.DAILY
                        dailyStats shouldBe testDailyStats
                        achievementMetrics shouldBe testAchievementMetrics
                        isLoading shouldBe false
                        error shouldBe null
                    }
                }
            }
        }

        When("성취 지표 로드에 실패하면") {
            Then("에러를 발생시키지 않고 성취 지표만 비어있어야 한다") {
                runTest {
                    val getAchievementMetricsUseCase = mockk<GetAchievementMetricsUseCase>()
                    coEvery { getAchievementMetricsUseCase() } throws Exception("Test error")

                    val robot = StatsViewModelRobot(
                        scope = this,
                        getAchievementMetricsUseCase = getAchievementMetricsUseCase
                    )
                    robot.createViewModel()

                    verify { CrashReporter.recordException(any(), "성취 지표 로드 실패") }
                    robot.verifyState {
                        achievementMetrics shouldBe null
                    }
                }
            }
        }
    }

    Given("기간 선택 기능에서") {
        When("사용자가 주간 기간을 선택하면") {
            Then("주간 통계를 로드하고 UI 상태를 업데이트해야 한다") {
                runTest {
                    val testWeeklyStats = WeeklyStats(
                        weekStartDate = LocalDate.now(),
                        weekEndDate = LocalDate.now(),
                        totalFocusTime = Duration.ZERO,
                        dailyBreakdown = emptyList(),
                        averageSessionsPerDay = 0.0,
                        mostProductiveDay = null,
                        growthRate = 0.0
                    )
                    val robot = StatsViewModelRobot(this)
                    robot.setupWeeklyStats(testWeeklyStats)
                    robot.createViewModel()

                    robot.selectPeriod(StatsPeriod.WEEKLY)

                    robot.verifyState {
                        selectedPeriod shouldBe StatsPeriod.WEEKLY
                        weeklyStats shouldBe testWeeklyStats
                    }
                    coVerify { robot.getWeeklyStatsUseCase() }
                }
            }
        }

        When("사용자가 월간 기간을 선택하면") {
            Then("월간 통계를 로드하고 UI 상태를 업데이트해야 한다") {
                runTest {
                    val testMonthlyStats = MonthlyStats(
                        yearMonth = YearMonth.now(),
                        totalFocusTime = Duration.ZERO,
                        weeklyBreakdown = emptyList(),
                        averageSessionsPerWeek = 0.0,
                        mostProductiveWeek = null,
                        growthRate = 0.0
                    )
                    val robot = StatsViewModelRobot(this)
                    robot.setupMonthlyStats(testMonthlyStats)
                    robot.createViewModel()

                    robot.selectPeriod(StatsPeriod.MONTHLY)

                    robot.verifyState {
                        selectedPeriod shouldBe StatsPeriod.MONTHLY
                        monthlyStats shouldBe testMonthlyStats
                    }
                    coVerify { robot.getMonthlyStatsUseCase() }
                }
            }
        }
    }

    Given("기간 이동 기능에서") {
        When("이전 기간으로 이동하면 - Daily") {
            Then("선택된 기간의 이전 날짜로 변경하고 통계를 다시 로드해야 한다") {
                runTest {
                    val robot = StatsViewModelRobot(this)
                    robot.createViewModel()

                    robot.navigateToPreviousPeriod()

                    val yesterday = LocalDate.now().minusDays(1)
                    coVerify { robot.getDailyStatsUseCase(yesterday) }
                }
            }
        }

        When("이전 기간으로 이동하면 - Weekly") {
            Then("선택된 기간의 이전 날짜로 변경하고 통계를 다시 로드해야 한다") {
                runTest {
                    val robot = StatsViewModelRobot(this)
                    robot.createViewModel()
                    robot.selectPeriod(StatsPeriod.WEEKLY)

                    robot.navigateToPreviousPeriod()

                    val previousDate = LocalDate.now().minusWeeks(1)
                    coVerify { robot.getWeeklyStatsUseCase(previousDate) }
                }
            }
        }

        When("이전 기간으로 이동하면 - Monthly") {
            Then("선택된 기간의 이전 날짜로 변경하고 통계를 다시 로드해야 한다") {
                runTest {
                    val robot = StatsViewModelRobot(this)
                    robot.createViewModel()
                    robot.selectPeriod(StatsPeriod.MONTHLY)

                    robot.navigateToPreviousPeriod()

                    val previousDate = YearMonth.now().minusMonths(1)
                    coVerify { robot.getMonthlyStatsUseCase(previousDate) }
                }
            }
        }

        When("다음 기간으로 이동하면 - 미래가 아닌 경우") {
            Then("선택된 기간의 다음 날짜로 변경하고 통계를 다시 로드해야 한다") {
                runTest {
                    val robot = StatsViewModelRobot(this)
                    robot.createViewModel()

                    // 오늘 -> 어제
                    robot.navigateToPreviousPeriod()
                    clearMocks(robot.getDailyStatsUseCase, answers = false, recordedCalls = true)

                    // 어제 -> 오늘
                    robot.navigateToNextPeriod()

                    coVerify(exactly = 1) { robot.getDailyStatsUseCase(LocalDate.now()) }
                }
            }
        }

        When("다음 기간으로 이동하면 - 미래로 이동 시도") {
            Then("날짜가 변경되지 않고 통계를 다시 로드하지 않아야 한다") {
                runTest {
                    val robot = StatsViewModelRobot(this)
                    robot.createViewModel()
                    clearMocks(robot.getDailyStatsUseCase, answers = false, recordedCalls = true)

                    robot.navigateToNextPeriod()

                    coVerify(exactly = 0) { robot.getDailyStatsUseCase(LocalDate.now()) }
                }
            }
        }

        When("오늘로 이동 기능을 사용하면") {
            Then("오늘 날짜로 변경하고 통계를 다시 로드해야 한다") {
                runTest {
                    val robot = StatsViewModelRobot(this)
                    robot.createViewModel()

                    // 오늘 -> 어제
                    robot.navigateToPreviousPeriod()
                    clearMocks(robot.getDailyStatsUseCase, answers = false, recordedCalls = true)

                    // 어제 -> 오늘
                    robot.navigateToToday()

                    coVerify(exactly = 1) { robot.getDailyStatsUseCase(LocalDate.now()) }
                }
            }
        }
    }

    Given("데이터 로드 실패 시") {
        When("통계 데이터 로드 중 에러가 발생하면") {
            Then("에러 메시지를 UI 상태에 표시해야 한다") {
                runTest {
                    val getDailyStatsUseCase = mockk<GetDailyStatsUseCase>()
                    coEvery { getDailyStatsUseCase(any()) } throws RuntimeException("Data load failed")

                    val robot = StatsViewModelRobot(
                        scope = this,
                        getDailyStatsUseCase = getDailyStatsUseCase
                    )
                    robot.createViewModel()

                    robot.verifyState {
                        isLoading shouldBe false
                        error shouldBe "통계를 불러오는데 실패했습니다: Data load failed"
                    }

                    verify { CrashReporter.recordException(any(), any()) }
                }
            }
        }

        When("에러 메시지가 표시된 상태에서 에러를 확인하면") {
            Then("에러 메시지가 초기화되어야 한다") {
                runTest {
                    val getDailyStatsUseCase = mockk<GetDailyStatsUseCase>()
                    coEvery { getDailyStatsUseCase(any()) } throws RuntimeException("Data load failed")

                    val robot = StatsViewModelRobot(
                        scope = this,
                        getDailyStatsUseCase = getDailyStatsUseCase
                    )
                    robot.createViewModel()

                    // 에러 상태 확인
                    robot.verifyState { error shouldBe "통계를 불러오는데 실패했습니다: Data load failed" }

                    // 에러 클리어
                    robot.viewModel.clearError()

                    robot.verifyState { error shouldBe null }
                }
            }
        }
    }
})
