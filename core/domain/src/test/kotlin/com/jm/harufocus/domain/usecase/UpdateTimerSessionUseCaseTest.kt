package com.jm.harufocus.domain.usecase

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.usecase.session.UpdateTimerSessionUseCase
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
 * UpdateTimerSessionUseCase 단위 테스트
 *
 * TDD Red 단계 - 실패하는 테스트를 먼저 작성
 */
class UpdateTimerSessionUseCaseTest : BehaviorSpec({

    val repository = mockk<TimerSessionRepository>()
    val useCase = UpdateTimerSessionUseCase(repository)

    Given("유효한 세션이 주어질 때") {
        val existingSession = TimerSession(
            id = 1,
            presetId = 1,
            startTime = Instant.now(),
            endTime = null,
            duration = 25.minutes,
            completed = false,
            overrunTime = null
        )

        val updatedSession = existingSession.copy(
            endTime = Instant.now().plusSeconds(1500),
            completed = true
        )

        coEvery { repository.getSessionById(1) } returns existingSession
        coEvery { repository.updateSession(any()) } just runs

        When("세션을 업데이트하면") {
            Then("성공적으로 업데이트되어야 한다") {
                runTest {
                    val result = useCase(updatedSession)

                    result.isSuccess shouldBe true
                    coVerify(exactly = 1) { repository.updateSession(updatedSession) }
                }
            }
        }
    }

    Given("존재하지 않는 세션을 업데이트하려 할 때") {
        val nonExistentSession = TimerSession(
            id = 999,
            presetId = 1,
            startTime = Instant.now(),
            endTime = Instant.now().plusSeconds(1500),
            duration = 25.minutes,
            completed = true,
            overrunTime = null
        )

        coEvery { repository.getSessionById(999) } returns null

        When("업데이트를 시도하면") {
            Then("예외가 발생해야 한다") {
                runTest {
                    val result = useCase(nonExistentSession)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "업데이트할 세션을 찾을 수 없습니다. (ID: 999)"
                }
            }
        }
    }

    Given("잘못된 데이터로 업데이트하려 할 때") {
        val invalidSession = TimerSession(
            id = 1,
            presetId = 1,
            startTime = Instant.now(),
            endTime = Instant.now().minusSeconds(100), // 종료 시간이 시작 시간보다 이름
            duration = 25.minutes,
            completed = true,
            overrunTime = null
        )

        When("업데이트를 시도하면") {
            Then("유효성 검증 실패로 예외가 발생해야 한다") {
                runTest {
                    val result = useCase(invalidSession)

                    result.isFailure shouldBe true
                    result.exceptionOrNull()?.message shouldBe "세션 종료 시간은 시작 시간보다 늦어야 합니다."
                }
            }
        }
    }
})
