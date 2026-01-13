package com.jm.harufocus.domain.usecase

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.usecase.session.SaveTimerSessionUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * SaveTimerSessionUseCase 단위 테스트
 *
 * TDD 방식으로 작성 (Red-Green-Refactor)
 */
class SaveTimerSessionUseCaseTest : BehaviorSpec({

    // Mock 객체와 SUT(System Under Test) 선언
    val repository = mockk<TimerSessionRepository>()
    val useCase = SaveTimerSessionUseCase(repository)

    Given("완료된 타이머 세션이 주어질 때") {
        val session = TimerSession(
            id = 0,
            presetId = 1,
            startTime = Instant.now(),
            endTime = Instant.now().plusSeconds(1500),
            duration = 25.minutes,
            completed = true,
            overrunTime = null
        )

        coEvery { repository.insertSession(any()) } returns 1L

        When("세션을 저장하면") {
            Then("성공적으로 세션 ID를 반환해야 한다") {
                runTest {
                    val result = useCase(session)

                    result.isSuccess shouldBe true
                    result.getOrThrow() shouldBe 1L
                    coVerify(exactly = 1) { repository.insertSession(session) }
                }
            }
        }
    }

    Given("진행 중인 타이머 세션이 주어질 때") {
        val session = TimerSession(
            id = 0,
            presetId = 1,
            startTime = Instant.now(),
            endTime = null,
            duration = 10.minutes,
            completed = false,
            overrunTime = null
        )

        coEvery { repository.insertSession(any()) } returns 2L

        When("세션을 저장하면") {
            Then("성공적으로 세션 ID를 반환해야 한다") {
                runTest {
                    val result = useCase(session)

                    result.isSuccess shouldBe true
                    result.getOrThrow() shouldBe 2L
                }
            }
        }
    }

    Given("초과 시간이 있는 세션이 주어질 때") {
        val session = TimerSession(
            id = 0,
            presetId = 1,
            startTime = Instant.now(),
            endTime = Instant.now().plusSeconds(1800),
            duration = 25.minutes,
            completed = true,
            overrunTime = 5.minutes
        )

        coEvery { repository.insertSession(any()) } returns 3L

        When("세션을 저장하면") {
            Then("성공적으로 초과 시간과 함께 저장되어야 한다") {
                runTest {
                    val result = useCase(session)

                    result.isSuccess shouldBe true
                    result.getOrThrow() shouldBe 3L
                }
            }
        }
    }

    Given("지속 시간이 0 이하인 잘못된 세션이 주어질 때") {
        val invalidSession = TimerSession(
            id = 0,
            presetId = 1,
            startTime = Instant.now(),
            endTime = Instant.now(),
            duration = 0.seconds,
            completed = true,
            overrunTime = null
        )

        When("세션을 저장하려고 하면") {
            Then("유효성 검증 실패로 예외가 발생해야 한다") {
                runTest {
                    val result = useCase(invalidSession)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "세션 지속 시간은 0보다 커야 합니다."
                }
            }
        }
    }

    Given("시작 시간이 종료 시간보다 늦은 경우") {
        val startTime = Instant.now()
        val invalidSession = TimerSession(
            id = 0,
            presetId = 1,
            startTime = startTime,
            endTime = startTime.minusSeconds(100),
            duration = 10.minutes,
            completed = true,
            overrunTime = null
        )

        When("세션을 저장하려고 하면") {
            Then("시간 유효성 검증 실패로 예외가 발생해야 한다") {
                runTest {
                    val result = useCase(invalidSession)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "세션 종료 시간은 시작 시간보다 늦어야 합니다."
                }
            }
        }
    }
})
