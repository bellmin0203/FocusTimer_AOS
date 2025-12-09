package com.jm.focustimer.domain.usecase

import com.jm.focustimer.domain.model.preset.Preset
import com.jm.focustimer.domain.repository.PresetRepository
import com.jm.focustimer.domain.usecase.preset.UpdatePresetUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * UpdatePresetUseCase 단위 테스트
 *
 * Kotest BehaviorSpec 사용 (Given-When-Then 스타일)
 */
class UpdatePresetUseCaseTest : BehaviorSpec({

    // Mock 객체와 SUT 선언
    val presetRepository = mockk<PresetRepository>(relaxed = true)
    val useCase = UpdatePresetUseCase(presetRepository)

    val validPreset = Preset(
        id = 1,
        name = "수정이름",
        duration = 60.minutes,
        colorIndex = 3,
        createdAt = Instant.now()
    )

    Given("유효한 프리셋 정보를 전달하면") {
        coEvery { presetRepository.updatePreset(validPreset) } just Runs

        When("UpdatePresetUseCase를 실행하면") {
            var result: Result<Unit>? = null
            beforeEach {
                result = useCase.invoke(validPreset)
            }

            Then("업데이트가 정상적으로 수행되어야 한다") {
                result?.isSuccess shouldBe true
                coVerify { presetRepository.updatePreset(validPreset) }
            }
        }
    }

    Given("프리셋 이름이 비어있는 경우") {
        val invalidPreset = validPreset.copy(name = "")
        When("UpdatePresetUseCase를 실행하면") {
            val result = useCase.invoke(invalidPreset)

            Then("실패 결과와 에러 메시지를 반환해야 한다") {
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "프리셋 이름을 입력해주세요."
            }
        }
    }

    Given("프리셋 이름이 21자 이상인 경우") {
        val invalidPreset = validPreset.copy(name = "a".repeat(UpdatePresetUseCase.MAX_NAME_LENGTH + 1))
        When("UpdatePresetUseCase를 실행하면") {
            val result = useCase.invoke(invalidPreset)

            Then("실패 결과와 에러 메시지를 반환해야 한다") {
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe
                        "프리셋 이름은 ${UpdatePresetUseCase.MAX_NAME_LENGTH}자 이하로 입력해주세요."
            }
        }
    }

    Given("타이머 시간이 0초 이하인 경우") {
        val invalidPreset = validPreset.copy(duration = 0.minutes)
        When("UpdatePresetUseCase를 실행하면") {
            val result = useCase.invoke(invalidPreset)

            Then("실패 결과와 에러 메시지를 반환해야 한다") {
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "타이머 시간은 0보다 커야 합니다."
            }
        }
    }

    Given("업데이트 중 예외가 발생하는 경우") {
        coEvery { presetRepository.updatePreset(validPreset) } throws RuntimeException("DB 오류")
        When("UpdatePresetUseCase를 실행하면") {
            val result = useCase.invoke(validPreset)

            Then("실패 결과와 예외 메시지를 반환해야 한다") {
                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "DB 오류"
            }
        }
    }
})
