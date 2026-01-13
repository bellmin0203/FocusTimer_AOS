package com.jm.harufocus.domain.usecase


import com.jm.harufocus.domain.repository.PresetRepository
import com.jm.harufocus.domain.usecase.preset.AddPresetUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.seconds

/**
 * AddPresetUseCase 단위 테스트
 *
 * Kotest BehaviorSpec 사용 (Given-When-Then 스타일)
 */
class AddPresetUseCaseTest : BehaviorSpec({

    val MAX_PRESET = 5

    // Mock 객체와 SUT(System Under Test) 선언
    val presetRepository = mockk<PresetRepository>()
    val useCase = AddPresetUseCase(presetRepository)

    Given("프리셋이 ${MAX_PRESET}개 존재하는 경우") {
        every { runBlocking { presetRepository.getTotalPresetCount() } } returns MAX_PRESET

        When("새로운 프리셋을 추가 시도하면") {
            var result: Result<Long>? = null
            beforeEach {
                runTest {
                    result = useCase("프리셋A", 10.seconds, 1)
                }
            }

            Then("예외(최대 개수 초과)로 추가에 실패해야 한다") {
                result?.isFailure shouldBe true
                result?.exceptionOrNull() shouldBe IllegalStateException("프리셋은 최대 ${MAX_PRESET}개까지 저장할 수 있습니다.")
            }
        }
    }

    Given("프리셋 이름이 빈 문자열일 때") {
        every { runBlocking { presetRepository.getTotalPresetCount() } } returns 0

        When("프리셋 추가를 시도하면") {
            var result: Result<Long>? = null
            beforeEach {
                runTest {
                    result = useCase("", 10.seconds, 1)
                }
            }

            Then("이름 유효성 예외가 발생해야 한다") {
                result?.isFailure shouldBe true
                result?.exceptionOrNull()?.message shouldBe "프리셋 이름을 입력해주세요."
            }
        }
    }

    Given("프리셋 이름이 21자로 너무 길 때") {
        every { runBlocking { presetRepository.getTotalPresetCount() } } returns 0
        val longName = "a".repeat(21)

        When("프리셋 추가를 시도하면") {
            var result: Result<Long>? = null
            beforeEach {
                runTest {
                    result = useCase(longName, 10.seconds, 1)
                }
            }

            Then("이름 길이 예외가 발생해야 한다") {
                result?.isFailure shouldBe true
                result?.exceptionOrNull()?.message shouldBe "프리셋 이름은 20자 이하로 입력해주세요."
            }
        }
    }

    Given("타이머 시간이 0초 이하일 때") {
        every { runBlocking { presetRepository.getTotalPresetCount() } } returns 0

        When("프리셋 추가를 시도하면") {
            var result: Result<Long>? = null
            beforeEach {
                runTest {
                    result = useCase("ValidName", 0.seconds, 1)
                }
            }

            Then("타이머 시간 예외가 발생해야 한다") {
                result?.isFailure shouldBe true
                result?.exceptionOrNull()?.message shouldBe "타이머 시간은 0보다 커야 합니다."
            }
        }
    }

    Given("컬러 인덱스가 0~5 범위를 벗어나면") {
        every { runBlocking { presetRepository.getTotalPresetCount() } } returns 0

        When("프리셋 추가시 컬러 인덱스가 7이면") {
            var result: Result<Long>? = null
            beforeEach {
                runTest {
                    result = useCase("ValidName", 10.seconds, 7)
                }
            }

            Then("컬러 인덱스 예외가 발생해야 한다") {
                result?.isFailure shouldBe true
                result?.exceptionOrNull()?.message shouldBe "컬러 인덱스는 0에서 5 사이여야 합니다."
            }
        }
    }

    Given("모든 조건이 올바른 경우") {
        every { runBlocking { presetRepository.getTotalPresetCount() } } returns 0
        every { runBlocking { presetRepository.insertPreset(any()) } } returns 100L

        When("정상적으로 프리셋을 추가하면") {
            var result: Result<Long>? = null
            beforeEach {
                runTest {
                    result = useCase("Valid", 10.seconds, 2)
                }
            }

            Then("성공적으로 Preset id를 반환해야 한다") {
                result?.isSuccess shouldBe true
                result?.getOrThrow() shouldBe 100L
            }
        }
    }
})
