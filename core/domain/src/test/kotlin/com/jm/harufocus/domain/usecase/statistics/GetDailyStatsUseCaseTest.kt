package com.jm.harufocus.domain.usecase.statistics

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.repository.TimerSessionRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class GetDailyStatsUseCaseTest : BehaviorSpec({

    val timerSessionRepository: TimerSessionRepository = mockk()
    val getDailyStatsUseCase = GetDailyStatsUseCase(timerSessionRepository)

    Given("특정 날짜에 완료된 세션이 하나도 없는 경우") {
        val date = LocalDate.now()
        coEvery { timerSessionRepository.getCompletedSessionsBetween(any(), any()) } returns flowOf(
            emptyList()
        )

        When("해당 날짜의 일일 통계를 조회하면") {
            val result = getDailyStatsUseCase(date)

            Then("총 집중 시간은 0이어야 한다") {
                result.totalFocusTime shouldBe Duration.ZERO
            }

            Then("완료된 세션 수는 0이어야 한다") {
                result.completedSessions shouldBe 0
            }

            Then("시간대별 통계는 모두 0이어야 한다") {
                result.hourlyBreakdown.size shouldBe 24
                result.hourlyBreakdown.all {
                    it.focusTime == Duration.ZERO && it.sessionCount == 0
                } shouldBe true
            }

            Then("가장 생산적인 시간은 null이어야 한다") {
                result.mostProductiveHour shouldBe null
            }
        }
    }

    Given("오전 10시에 30분간 진행된 세션이 하나 있는 경우") {
        val testDate = LocalDate.now()

        val startInstant = testDate.atTime(10, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
        val endInstant = startInstant.plus(30, ChronoUnit.MINUTES)

        coEvery {
            timerSessionRepository.getCompletedSessionsBetween(any(), any())
        } returns flowOf(
            listOf(
                TimerSession(
                    id = 1,
                    startTime = startInstant,
                    endTime = endInstant,
                    duration = 30.minutes,
                    completed = true,
                    overrunTime = Duration.ZERO
                )
            )
        )

        When("일일 통계를 조회하면") {
            val result = getDailyStatsUseCase(testDate)

            Then("총 집중 시간은 30분이어야 한다") {
                result.totalFocusTime shouldBe 30.minutes
            }

            Then("가장 생산적인 시간은 10시여야 한다") {
                result.mostProductiveHour shouldBe 10
            }

            Then("10시의 통계가 정확해야 한다") {
                val hourStats = result.hourlyBreakdown[10]
                hourStats.hour shouldBe 10
                hourStats.focusTime shouldBe 30.minutes
                hourStats.sessionCount shouldBe 1
            }
        }
    }

    Given("여러 시간대에 걸쳐 복수의 세션이 존재하는 경우") {
        val testDate = LocalDate.now()

        // 10:00 ~ 10:30 (30분)
        val startInstant1 = testDate.atTime(10, 0).atZone(ZoneId.systemDefault()).toInstant()
        val endInstant1 = startInstant1.plus(30, ChronoUnit.MINUTES)

        // 10:40 ~ 10:50 (10분)
        val startInstant2 = testDate.atTime(10, 40).atZone(ZoneId.systemDefault()).toInstant()
        val endInstant2 = startInstant2.plus(10, ChronoUnit.MINUTES)

        // 14:00 ~ 15:00 (60분)
        val startInstant3 = testDate.atTime(14, 0).atZone(ZoneId.systemDefault()).toInstant()
        val endInstant3 = startInstant3.plus(60, ChronoUnit.MINUTES)

        coEvery {
            timerSessionRepository.getCompletedSessionsBetween(any(), any())
        } returns flowOf(
            listOf(
                TimerSession(id = 1, startTime = startInstant1, endTime = endInstant1, duration = 30.minutes, completed = true, overrunTime = Duration.ZERO),
                TimerSession(id = 2, startTime = startInstant2, endTime = endInstant2, duration = 10.minutes, completed = true, overrunTime = Duration.ZERO),
                TimerSession(id = 3, startTime = startInstant3, endTime = endInstant3, duration = 60.minutes, completed = true, overrunTime = Duration.ZERO)
            )
        )

        When("일일 통계를 조회하면") {
            val result = getDailyStatsUseCase(testDate)

            Then("총 집중 시간이 올바르게 합산되어야 한다") {
                result.totalFocusTime shouldBe 100.minutes // 30 + 10 + 60
                result.completedSessions shouldBe 3
            }

            Then("시간대별(Hourly) 통계가 정확히 집계되어야 한다") {
                result.hourlyBreakdown.size shouldBe 24

                // 10시 통계 (30분 + 10분 = 40분, 2회)
                val stats10 = result.hourlyBreakdown[10]
                stats10.hour shouldBe 10
                stats10.focusTime shouldBe 40.minutes
                stats10.sessionCount shouldBe 2

                // 14시 통계 (60분, 1회)
                val stats14 = result.hourlyBreakdown[14]
                stats14.hour shouldBe 14
                stats14.focusTime shouldBe 60.minutes
                stats14.sessionCount shouldBe 1

                // 그 외 시간대는 0이어야 함
                result.hourlyBreakdown.filter { it.hour != 10 && it.hour != 14 }.all {
                    it.focusTime == Duration.ZERO && it.sessionCount == 0
                } shouldBe true
            }

            Then("가장 집중 시간이 긴 시간대가 생산적인 시간으로 선정되어야 한다") {
                result.mostProductiveHour shouldBe 14 // 60분 > 40분
            }
        }
    }

    Given("특정 날짜의 통계를 요청했을 때") {
        val targetDate = LocalDate.of(2025, 1, 1)
        val zoneId = ZoneId.systemDefault()
        
        // 해당 날짜의 시작(00:00:00)과 다음 날의 시작(00:00:00)을 밀리초로 계산
        val startOfDay = targetDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = targetDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // Mock 설정: 호출 시 빈 리스트 반환 (호출 파라미터 검증이 목적이므로 반환값은 중요하지 않음)
        coEvery { timerSessionRepository.getCompletedSessionsBetween(any(), any()) } returns flowOf(emptyList())

        When("UseCase를 실행하면") {
            getDailyStatsUseCase(targetDate)

            Then("해당 날짜의 시작과 끝 시간(ms)으로 저장소를 조회해야 한다") {
                coVerify(exactly = 1) {
                    timerSessionRepository.getCompletedSessionsBetween(startOfDay, endOfDay)
                }
            }
        }
    }
})