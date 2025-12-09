package com.jm.focustimer.domain.usecase


import com.jm.focustimer.domain.model.preset.Preset
import com.jm.focustimer.domain.repository.PresetRepository
import com.jm.focustimer.domain.repository.SettingsRepository
import com.jm.focustimer.domain.usecase.preset.DeletePresetUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * DeletePresetUseCase 단위 테스트
 *
 * Kotest BehaviorSpec 사용 (Given-When-Then 스타일)
 */
class DeletePresetUseCaseTest : BehaviorSpec({

    // Mock 객체와 SUT 선언
    val presetRepository = mockk<PresetRepository>(relaxed = true)
    val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    val useCase = DeletePresetUseCase(presetRepository, settingsRepository)

    val preset = Preset(
        id = 1,
        name = "테스트",
        duration = 60.minutes,
        colorIndex = 2,
        createdAt = Instant.now()
    )

    Given("기본 프리셋이 삭제 대상인 경우") {
        every { settingsRepository.defaultPresetId } returns flowOf(1)
        coEvery { settingsRepository.updateDefaultPresetId(null) } just Runs
        coEvery { presetRepository.deletePreset(preset) } just Runs

        When("DeletePresetUseCase를 실행하면") {
            var result: Result<Unit>? = null
            beforeEach {
                runTest {
                    result = useCase(preset)
                }
            }

            Then("기본 프리셋이 해제되고 삭제가 성공해야 한다") {
                result?.isSuccess shouldBe true
                coVerify { settingsRepository.updateDefaultPresetId(null) }
                coVerify { presetRepository.deletePreset(preset) }
            }
        }
    }

    Given("기본 프리셋이 아닌 프리셋을 삭제하는 경우") {
        every { settingsRepository.defaultPresetId } returns flowOf(2)
        coEvery { presetRepository.deletePreset(preset) } just Runs

        When("DeletePresetUseCase를 실행하면") {
            var result: Result<Unit>? = null
            beforeEach {
                runTest {
                    clearMocks(presetRepository, settingsRepository, answers = false)
                    result = useCase(preset)
                }
            }
            Then("삭제만 되고 기본 프리셋 해제는 호출되지 않아야 한다") {
                result?.isSuccess shouldBe true
                coVerify(exactly = 0) { settingsRepository.updateDefaultPresetId(any()) }
                coVerify { presetRepository.deletePreset(preset) }
            }
        }
    }

    Given("삭제 중 예외가 발생하는 경우") {
        every { settingsRepository.defaultPresetId } returns flowOf(1)
        coEvery { settingsRepository.updateDefaultPresetId(null) } just Runs
        coEvery { presetRepository.deletePreset(preset) } throws RuntimeException("삭제 실패")

        When("DeletePresetUseCase를 실행하면") {
            var result: Result<Unit>? = null
            beforeEach {
                runTest {
                    result = useCase(preset)
                }
            }
            Then("실패 결과와 예외 메시지를 반환해야 한다") {
                result?.isFailure shouldBe true
                result?.exceptionOrNull()?.message shouldBe "삭제 실패"
            }
        }
    }
})
