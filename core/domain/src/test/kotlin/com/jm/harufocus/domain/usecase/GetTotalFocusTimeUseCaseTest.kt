package com.jm.harufocus.domain.usecase

import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.usecase.session.GetTotalFocusTimeUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant

/**
 * GetTotalFocusTimeUseCase 단위 테스트
 *
 * TDD Red 단계 - 실패하는 테스트를 먼저 작성
 */
class GetTotalFocusTimeUseCaseTest : BehaviorSpec({

    val repository = mockk<TimerSessionRepository>()
    val useCase = GetTotalFocusTimeUseCase(repository)

    Given("특정 기간에 완료된 세션들이 있을 때") {
        val startTime = Instant.parse("2024-01-01T00:00:00Z")
        val endTime = Instant.parse("2024-01-31T23:59:59Z")
        val totalFocusTimeMillis = 3600000L // 1시간 = 3,600,000ms

        coEvery {
            repository.getTotalFocusTimeBetween(
                startTime.toEpochMilli(),
                endTime.toEpochMilli()
            )
        } returns totalFocusTimeMillis

        When("해당 기간의 총 집중 시간을 조회하면") {
            Then("총 집중 시간을 밀리초로 반환해야 한다") {
                runTest {
                    val result = useCase(startTime, endTime)

                    result shouldBe totalFocusTimeMillis
                }
            }
        }
    }

    Given("특정 기간에 완료된 세션이 없을 때") {
        val startTime = Instant.parse("2024-02-01T00:00:00Z")
        val endTime = Instant.parse("2024-02-29T23:59:59Z")

        coEvery {
            repository.getTotalFocusTimeBetween(
                startTime.toEpochMilli(),
                endTime.toEpochMilli()
            )
        } returns 0L

        When("해당 기간의 총 집중 시간을 조회하면") {
            Then("0을 반환해야 한다") {
                runTest {
                    val result = useCase(startTime, endTime)

                    result shouldBe 0L
                }
            }
        }
    }

    Given("시작 시간이 종료 시간보다 늦을 때") {
        val startTime = Instant.parse("2024-02-01T00:00:00Z")
        val endTime = Instant.parse("2024-01-01T00:00:00Z")

        When("총 집중 시간을 조회하려고 하면") {
            Then("예외가 발생해야 한다") {
                runTest {
                    val result = runCatching {
                        useCase(startTime, endTime)
                    }

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "시작 시간은 종료 시간보다 빨라야 합니다."
                }
            }
        }
    }
})
