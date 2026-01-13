package com.jm.harufocus.setting.integration

import app.cash.turbine.test
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.usecase.preset.GetAllPresetsUseCase
import com.jm.harufocus.setting.SettingViewModel
import com.jm.harufocus.setting.model.SettingCategory
import com.jm.harufocus.setting.model.SettingType
import com.jm.harufocus.setting.util.FakeSettingsRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Setting 모듈 통합 테스트
 *
 * Kotest BehaviorSpec을 사용한 Given-When-Then 스타일의 통합 테스트 코드
 * - Mock 대신 FakeSettingsRepository를 사용하여 실제 상태 변화를 검증함
 * - Turbine을 사용하여 Flow 상태 변화를 정밀하게 검증함
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingIntegrationTest : BehaviorSpec({
    // 테스트용 Dispatcher 설정
    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // 객체 생성 (Fake 사용)
    lateinit var settingsRepository: FakeSettingsRepository
    lateinit var getAllPresetsUseCase: GetAllPresetsUseCase
    lateinit var viewModel: SettingViewModel

    beforeContainer {
        // Fake Repository 생성
        settingsRepository = FakeSettingsRepository()
        
        getAllPresetsUseCase = mockk(relaxed = true)
        coEvery { getAllPresetsUseCase() } returns flowOf(emptyList())
    }

    Given("설정 항목을 생성하고 표시할 때") {
        When("ViewModel이 초기화되면") {
            Then("TC-058: 설정 항목들이 카테고리별로 올바르게 그룹화되어야 한다") {
                viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
                testDispatcher.scheduler.advanceUntilIdle()

                val items = viewModel.settingItems.value
                val categories = items.map { it.category }.distinct()

                // 모든 카테고리가 존재하는지 확인
                categories shouldContainAll SettingCategory.entries
            }
        }

        When("설정 항목을 카테고리별로 필터링하면") {
            Then("TC-059: 각 카테고리가 최소 1개 이상의 설정 항목을 포함해야 한다") {
                viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
                testDispatcher.scheduler.advanceUntilIdle()

                val items = viewModel.settingItems.value
                
                SettingCategory.entries.forEach { category ->
                    val categoryItems = items.filter { it.category == category }
                    categoryItems shouldHaveAtLeastSize 1
                }
            }
        }
    }

    Given("사용자가 테마를 변경하는 시나리오") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("초기 상태에서 테마를 LIGHT로 변경하면") {
            Then("TC-060: 상태가 LIGHT로 변경되어야 한다") {
                viewModel.updateThemeMode(ThemeMode.LIGHT)
                testDispatcher.scheduler.advanceUntilIdle()

                // 상태 검증
                settingsRepository.themeMode.value shouldBe ThemeMode.LIGHT
            }
        }

        When("테마를 DARK로 변경한 후 다시 SYSTEM으로 변경하면") {
            Then("순차적으로 상태가 변경되어야 한다") {
                viewModel.updateThemeMode(ThemeMode.DARK)
                testDispatcher.scheduler.advanceUntilIdle()
                settingsRepository.themeMode.value shouldBe ThemeMode.DARK

                viewModel.updateThemeMode(ThemeMode.SYSTEM)
                testDispatcher.scheduler.advanceUntilIdle()
                settingsRepository.themeMode.value shouldBe ThemeMode.SYSTEM
            }
        }
    }

    Given("사용자가 여러 설정을 연속으로 변경하는 시나리오") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("진동, 화면 켜기, 최소화 컨트롤을 순차적으로 변경하면") {
            Then("TC-061: 모든 설정 상태가 정상적으로 변경되어야 한다") {
                viewModel.updateIsNotificationVibrate(true)
                viewModel.updateIsScreenOn(true)
                viewModel.updateIsMinimizedControls(false)
                
                testDispatcher.scheduler.advanceUntilIdle()

                settingsRepository.isNotificationVibrate.value shouldBe true
                settingsRepository.isScreenOn.value shouldBe true
                settingsRepository.isMinimizedControls.value shouldBe false
            }
        }

        When("설정 변경 중 하나가 실패해도") {
            Then("다른 설정은 정상 처리되어야 한다") {
                // Fake 에러 설정
                settingsRepository.setShouldFail("updateNotificationVibrate", true)

                viewModel.updateIsNotificationVibrate(true)
                viewModel.updateIsScreenOn(true)
                viewModel.updateIsMinimizedControls(false)

                testDispatcher.scheduler.advanceUntilIdle()

                // 실패한 설정은 값이 바뀌지 않아야 함 (초기값 true 유지)
                settingsRepository.isScreenOn.value shouldBe true
                settingsRepository.isMinimizedControls.value shouldBe false
            }
        }
    }

    Given("기본 프리셋을 선택/해제하는 시나리오") {
        val testPresets = listOf(
            Preset(id = 1, name = "포모도로", duration = 25.minutes, createdAt = Instant.now()),
            Preset(id = 2, name = "짧은 휴식", duration = 5.minutes, createdAt = Instant.now()),
            Preset(id = 3, name = "긴 휴식", duration = 15.minutes, createdAt = Instant.now())
        )

        beforeEach {
            coEvery { getAllPresetsUseCase() } returns flowOf(testPresets)
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("프리셋을 선택하면") {
            Then("TC-062: 프리셋 ID가 저장되어야 한다") {
                // settingItems에서 프리셋 선택 항목 찾기
                val items = viewModel.settingItems.value
                val presetSelector = items.filterIsInstance<SettingType.Selector<*>>()
                    .firstOrNull { it.category == SettingCategory.TIMER && it.options.contains(null) }
                
                presetSelector shouldNotBe null

                viewModel.updateDefaultPresetId(1)
                testDispatcher.scheduler.advanceUntilIdle()

                settingsRepository.defaultPresetId.value shouldBe 1
            }
        }

        When("프리셋 선택을 해제하면 (null 설정)") {
            Then("프리셋 ID가 null이 되어야 한다") {
                viewModel.updateDefaultPresetId(null)
                testDispatcher.scheduler.advanceUntilIdle()

                settingsRepository.defaultPresetId.value shouldBe null
            }
        }

        When("프리셋 목록이 업데이트되면") {
            Then("기본 프리셋 옵션이 업데이트되어야 한다") {
                val presetFlow = MutableStateFlow(testPresets)
                coEvery { getAllPresetsUseCase() } returns presetFlow
                
                val dynamicViewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
                testDispatcher.scheduler.advanceUntilIdle()

                val initialItems = dynamicViewModel.settingItems.value
                val initialSelector = initialItems.filterIsInstance<SettingType.Selector<*>>()
                    .first { it.options.contains(null) }
                
                initialSelector.options.size shouldBe testPresets.size + 1

                val newPresets = testPresets + Preset(id = 4, name = "New Preset", duration = 60.minutes, createdAt = Instant.now())
                presetFlow.emit(newPresets)
                testDispatcher.scheduler.advanceUntilIdle()

                val updatedItems = dynamicViewModel.settingItems.value
                val updatedSelector = updatedItems.filterIsInstance<SettingType.Selector<*>>()
                    .first { it.options.contains(null) }
                updatedSelector.options.size shouldBe newPresets.size + 1
            }
        }
    }

    Given("세션 지속 시간을 설정하는 시나리오") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("다양한 지속 시간을 설정하면") {
            Then("모든 옵션이 정상적으로 저장되어야 한다") {
                val durations = listOf(5, 10, 15, 25, 30, 45, 60)
                
                durations.forEach { minutes ->
                    viewModel.updateDefaultSessionDuration(minutes.minutes)
                    testDispatcher.scheduler.advanceUntilIdle()
                    
                    settingsRepository.defaultSessionDuration.value shouldBe minutes.minutes
                }
            }
        }
    }

    Given("ViewModel 생명주기를 테스트할 때") {
        When("초기화부터 종료까지의 전체 흐름을 실행하면") {
            Then("TC-042: 전체 흐름이 정상 동작해야 한다") {
                viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
                testDispatcher.scheduler.advanceUntilIdle()
                
                viewModel.settingItems.value shouldNotBe emptyList<Any>()

                viewModel.updateIsScreenOn(true)
                testDispatcher.scheduler.advanceUntilIdle()
                settingsRepository.isScreenOn.value shouldBe true
            }
        }
    }

    Given("복잡한 설정 변경 시나리오") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("모든 카테고리의 설정을 동시에 변경하면") {
            Then("TC-043: 모두 정상적으로 처리되어야 한다") {
                viewModel.updateThemeMode(ThemeMode.DARK)
                viewModel.updateIsNotificationVibrate(false)
                viewModel.updateDefaultSessionDuration(30.minutes)
                viewModel.updateIsScreenOn(false)
                viewModel.updateIsMinimizedControls(true)
                viewModel.updateIsPulseAnimationEnabled(false)

                testDispatcher.scheduler.advanceUntilIdle()

                settingsRepository.themeMode.value shouldBe ThemeMode.DARK
                settingsRepository.isNotificationVibrate.value shouldBe false
                settingsRepository.defaultSessionDuration.value shouldBe 30.minutes
                settingsRepository.isScreenOn.value shouldBe false
                settingsRepository.isMinimizedControls.value shouldBe true
                settingsRepository.isPulseAnimationEnabled.value shouldBe false
            }
        }

        When("설정을 빠르게 연속으로 변경하면") {
            Then("race condition 없이 마지막 상태가 반영되어야 한다") {
                repeat(10) {
                    val isEven = it % 2 == 0
                    viewModel.updateIsScreenOn(isEven)
                }
                
                testDispatcher.scheduler.advanceUntilIdle()

                // 10번째 호출 (index 9)은 홀수이므로 false
                settingsRepository.isScreenOn.value shouldBe false
            }
        }
    }

    Given("에러 복구 시나리오") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("repository 에러 후 정상 동작으로 복구하면") {
            Then("이후 설정이 정상적으로 동작해야 한다") {
                // 1. 에러 발생 설정
                settingsRepository.setShouldFail("updateThemeMode", true)

                viewModel.updateThemeMode(ThemeMode.DARK)
                testDispatcher.scheduler.advanceUntilIdle()
                
                // 에러로 인해 값이 안 바뀜
                settingsRepository.themeMode.value shouldNotBe ThemeMode.DARK

                // 2. 에러 해제
                settingsRepository.setShouldFail("updateThemeMode", false)

                viewModel.updateThemeMode(ThemeMode.LIGHT)
                testDispatcher.scheduler.advanceUntilIdle()
                
                // 정상 처리
                settingsRepository.themeMode.value shouldBe ThemeMode.LIGHT
            }
        }

        When("여러 설정 중 일부만 실패하면") {
            Then("TC-038: 성공한 설정은 적용되어야 한다") {
                // 실패 설정
                settingsRepository.setShouldFail("updateNotificationVibrate", true)
                settingsRepository.setShouldFail("updateScreenOn", true)
                
                viewModel.updateIsNotificationVibrate(true) // Fail
                viewModel.updateIsScreenOn(true) // Fail
                viewModel.updateThemeMode(ThemeMode.DARK) // Success
                viewModel.updateIsMinimizedControls(true) // Success
                viewModel.updateIsPulseAnimationEnabled(true) // Success

                testDispatcher.scheduler.advanceUntilIdle()

                // 성공한 설정 확인
                settingsRepository.themeMode.value shouldBe ThemeMode.DARK
                settingsRepository.isMinimizedControls.value shouldBe true
                settingsRepository.isPulseAnimationEnabled.value shouldBe true
            }
        }
    }

    Given("Turbine을 활용한 정밀한 Flow 검증") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("테마를 연속으로 변경하면") {
            Then("모든 상태 변화가 순서대로 방출되어야 한다") {
                // 특정 설정 항목(테마)의 StateFlow를 찾아서 검증
                val themeSelector = viewModel.settingItems.value
                    .filterIsInstance<SettingType.Selector<ThemeMode>>()
                    .first { it.category == SettingCategory.APPEARANCE }

                themeSelector.stateFlow.test {
                    // 1. 초기 상태 확인
                    awaitItem() shouldBe ThemeMode.SYSTEM

                    // 2. 변경 1: LIGHT
                    viewModel.updateThemeMode(ThemeMode.LIGHT)
                    testDispatcher.scheduler.runCurrent()
                    awaitItem() shouldBe ThemeMode.LIGHT

                    // 3. 변경 2: DARK
                    viewModel.updateThemeMode(ThemeMode.DARK)
                    testDispatcher.scheduler.runCurrent()
                    awaitItem() shouldBe ThemeMode.DARK

                    // 4. 변경 3: SYSTEM
                    viewModel.updateThemeMode(ThemeMode.SYSTEM)
                    testDispatcher.scheduler.runCurrent()
                    awaitItem() shouldBe ThemeMode.SYSTEM
                }
            }
        }

        When("설정 변경 중 에러가 발생했다가 복구되면") {
            Then("상태가 변경되지 않다가 다시 변경되어야 한다") {
                val vibrateToggle = viewModel.settingItems.value
                    .filterIsInstance<SettingType.Toggle>()
                    .first { it.category == SettingCategory.NOTIFICATION }

                vibrateToggle.stateFlow.test {
                    awaitItem() shouldBe true // 초기값

                    // 에러 상황: 변경 시도 -> 실패 -> 값 유지 (이벤트 발생 안함)
                    settingsRepository.setShouldFail("updateNotificationVibrate", true)
                    viewModel.updateIsNotificationVibrate(false)
                    testDispatcher.scheduler.runCurrent()
                    
                    // Turbine은 새로운 이벤트가 발생하지 않음을 확인할 수 있음
                    expectNoEvents()

                    // 복구 상황
                    settingsRepository.setShouldFail("updateNotificationVibrate", false)
                    viewModel.updateIsNotificationVibrate(false)
                    testDispatcher.scheduler.runCurrent()
                    
                    awaitItem() shouldBe false // 이제 변경됨
                }
            }
        }
    }

    Given("실제 사용 패턴 시나리오") {
        beforeEach {
            viewModel = SettingViewModel(settingsRepository, getAllPresetsUseCase)
            testDispatcher.scheduler.advanceUntilIdle()
        }

        When("앱 시작 후 설정 화면을 열고 테마를 변경하면") {
            Then("상태가 매끄럽게 변경되어야 한다") {
                viewModel.updateThemeMode(ThemeMode.DARK)
                testDispatcher.scheduler.advanceUntilIdle()
                
                settingsRepository.themeMode.value shouldBe ThemeMode.DARK
            }
        }

        When("설정을 여러 번 왔다갔다 변경하면") {
            Then("최종 설정이 정확히 적용되어야 한다") {
                viewModel.updateThemeMode(ThemeMode.LIGHT)
                testDispatcher.scheduler.advanceUntilIdle()
                settingsRepository.themeMode.value shouldBe ThemeMode.LIGHT

                viewModel.updateThemeMode(ThemeMode.DARK)
                testDispatcher.scheduler.advanceUntilIdle()
                settingsRepository.themeMode.value shouldBe ThemeMode.DARK

                viewModel.updateThemeMode(ThemeMode.SYSTEM)
                testDispatcher.scheduler.advanceUntilIdle()
                settingsRepository.themeMode.value shouldBe ThemeMode.SYSTEM
            }
        }
    }
})