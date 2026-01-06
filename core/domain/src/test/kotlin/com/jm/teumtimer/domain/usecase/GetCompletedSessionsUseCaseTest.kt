package com.jm.teumtimer.domain.usecase

import com.jm.teumtimer.domain.model.session.TimerSession
import com.jm.teumtimer.domain.repository.TimerSessionRepository
import com.jm.teumtimer.domain.usecase.session.GetCompletedSessionsUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * GetCompletedSessionsUseCase 단위 테스트
 *
 * TDD Red 단계 - 실패하는 테스트를 먼저 작성
 */
class GetCompletedSessionsUseCaseTest : BehaviorSpec({

    val repository = mockk<TimerSessionRepository>()
    val useCase = GetCompletedSessionsUseCase(repository)

    Given("완료된 세션이 여러 개 있을 때") {
        val completedSessions = listOf(
            TimerSession(
                id = 1,
                presetId = 1,
                startTime = Instant.now(),
                endTime = Instant.now().plusSeconds(1500),
                duration = 25.minutes,
                completed = true,
                overrunTime = null
            ),
            TimerSession(
                id = 2,
                presetId = 1,
                startTime = Instant.now(),
                endTime = Instant.now().plusSeconds(900),
                duration = 15.minutes,
                completed = true,
                overrunTime = null
            )
        )

        every { repository.getCompletedSessions() } returns flowOf(completedSessions)

        When("완료된 세션을 조회하면") {
            Then("완료된 모든 세션을 반환해야 한다") {
                runTest {
                    val result = useCase().first()

                    result shouldHaveSize 2
                    result[0].completed shouldBe true
                    result[1].completed shouldBe true
                }
            }
        }
    }

    Given("완료된 세션이 없을 때") {
        every { repository.getCompletedSessions() } returns flowOf(emptyList())

        When("완료된 세션을 조회하면") {
            Then("빈 리스트를 반환해야 한다") {
                runTest {
                    val result = useCase().first()

                    result.shouldBeEmpty()
                }
            }
        }
    }
})
