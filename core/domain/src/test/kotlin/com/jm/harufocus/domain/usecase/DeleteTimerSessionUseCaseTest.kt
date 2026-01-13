package com.jm.harufocus.domain.usecase

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.usecase.session.DeleteTimerSessionUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * DeleteTimerSessionUseCase 단위 테스트
 *
 * TDD Red 단계 - 실패하는 테스트를 먼저 작성
 */
class DeleteTimerSessionUseCaseTest : BehaviorSpec({

    val repository = mockk<TimerSessionRepository>()
    val useCase = DeleteTimerSessionUseCase(repository)

    Given("존재하는 세션이 주어질 때") {
        val session = TimerSession(
            id = 1,
            presetId = 1,
            startTime = Instant.now(),
            endTime = Instant.now().plusSeconds(1500),
            duration = 25.minutes,
            completed = true,
            overrunTime = null
        )

        coEvery { repository.getSessionById(1) } returns session
        coEvery { repository.deleteSession(session) } just runs

        When("세션을 삭제하면") {
            Then("성공적으로 삭제되어야 한다") {
                runTest {
                    val result = useCase(1)

                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) { repository.deleteSession(session) }
                }
            }
        }
    }

    Given("존재하지 않는 세션 ID가 주어질 때") {
        coEvery { repository.getSessionById(999) } returns null

        When("세션을 삭제하려고 하면") {
            Then("예외가 발생해야 한다") {
                runTest {
                    val result = useCase(999)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "세션을 찾을 수 없습니다. (ID: 999)"
                }
            }
        }
    }

    Given("유효하지 않은 세션 ID가 주어질 때") {
        When("음수 ID로 삭제하려고 하면") {
            Then("예외가 발생해야 한다") {
                runTest {
                    val result = useCase(-1)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "유효하지 않은 세션 ID입니다."
                }
            }
        }
    }
})
