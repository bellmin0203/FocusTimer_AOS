package com.jm.harufocus.setting

import com.jm.harufocus.common.model.NotificationSoundType
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.core.datastore.api.SettingsPreferencesDataSource
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.usecase.preset.GetAllPresetsUseCase
import com.jm.harufocus.setting.model.SettingCategory
import com.jm.harufocus.setting.model.SettingType
import com.jm.harufocus.setting.util.FakeSettingsRepository
import com.jm.harufocus.ui.util.UiText
import com.jm.logutil.LogUtil
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
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
 * SettingViewModel 단위 테스트
 *
 * FakeSettingsRepository를 사용하여 실제 상태 변경을 검증합니다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest : BehaviorSpec({

    // 테스트용 Dispatcher 설정
    val testDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
        unmockkAll()
    }

    lateinit var settingsRepository: FakeSettingsRepository
    lateinit var getAllPresetsUseCase: GetAllPresetsUseCase
    lateinit var viewModel : SettingViewModel

    beforeContainer {
        settingsRepository = FakeSettingsRepository()
        getAllPresetsUseCase = mockk(relaxed = true)
        every { getAllPresetsUseCase() } returns flowOf(emptyList())

        viewModel = SettingViewModel(
            settingsRepository = settingsRepository,
            getAllPresetsUseCase = getAllPresetsUseCase,
            inAppReviewManager = mockk(relaxed = true),
            analyticsHelper = mockk(relaxed = true),
            appVersionName = "1.0.0"
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    Given("SettingViewModel이 생성될 때") {
        When("초기화가 수행되면") {
            Then("TC-001: 설정 항목이 정상적으로 초기화되어야 한다") {
                viewModel.settingItems.value.shouldNotBeEmpty()
            }

            Then("TC-002: GetAllPresetsUseCase가 호출되어야 한다") {
                coVerify { getAllPresetsUseCase() }
            }

            Then("TC-003: 4개의 카테고리가 모두 포함되어야 한다") {
                val items = viewModel.settingItems.value
                val categories = items.map { it.category }.toSet()

                categories shouldContain SettingCategory.APPEARANCE
                categories shouldContain SettingCategory.NOTIFICATION
                categories shouldContain SettingCategory.TIMER
                categories shouldContain SettingCategory.INTERACTION
            }
        }
    }

    Given("외관 설정이 생성될 때") {
        When("createAppearanceSettings()가 호출되면") {
            val items = viewModel.settingItems.value.filter { it.category == SettingCategory.APPEARANCE }

            Then("TC-004: 테마 모드 Selector가 포함되어야 한다") {
                items.any { it is SettingType.Selector<*> } shouldBe true
            }

            Then("TC-005: 테마 모드 옵션에 SYSTEM, LIGHT, DARK가 포함되어야 한다") {
                val selector = items.first { it is SettingType.Selector<*> } as SettingType.Selector<*>
                selector.options shouldContain ThemeMode.SYSTEM
                selector.options shouldContain ThemeMode.LIGHT
                selector.options shouldContain ThemeMode.DARK
            }

            Then("TC-006: 테마 모드의 기본값이 DEFAULT_THEME_MODE이어야 한다") {
                val selector = items.first { it is SettingType.Selector<*> } as SettingType.Selector<*>
                selector.defaultValue shouldBe SettingsPreferencesDataSource.DEFAULT_THEME_MODE
            }
        }
    }

    Given("알림 설정이 생성될 때") {
        When("createNotificationSettings()가 호출되면") {
            val items = viewModel.settingItems.value.filter { it.category == SettingCategory.NOTIFICATION }

            Then("TC-007: 진동 Toggle이 포함되어야 한다") {
                items.any { it is SettingType.Toggle } shouldBe true
            }

            Then("TC-008: 진동 설정의 기본값이 DEFAULT_IS_NOTIFICATION_VIBRATE이어야 한다") {
                val toggle = items.first { it is SettingType.Toggle } as SettingType.Toggle
                toggle.defaultValue shouldBe SettingsPreferencesDataSource.DEFAULT_IS_NOTIFICATION_VIBRATE
            }

            Then("TC-009: 진동 설정의 카테고리가 NOTIFICATION이어야 한다") {
                items.all { it.category == SettingCategory.NOTIFICATION } shouldBe true
            }
        }
    }

    Given("타이머 설정이 생성될 때") {
        When("createTimerSettings()가 호출되면") {
            val items = viewModel.settingItems.value.filter { it.category == SettingCategory.TIMER }

            Then("TC-010: '마지막 세션 기억' Toggle이 포함되어야 한다") {
                items.any { it is SettingType.Toggle && it.title == UiText.StringResource(R.string.pref_title_remember_session) } shouldBe true
            }

            Then("TC-011: '화면 켜기' Toggle이 포함되어야 한다") {
                items.any { it is SettingType.Toggle && it.title == UiText.StringResource(R.string.pref_title_screen_on) } shouldBe true
            }

            Then("TC-012: '기본 세션 지속 시간' Selector가 포함되어야 한다") {
                items.any { it is SettingType.Selector<*> && it.title == UiText.StringResource(R.string.pref_title_default_duration) } shouldBe true
            }

            Then("TC-013: '기본 프리셋' Selector가 포함되어야 한다") {
                items.any { it is SettingType.Selector<*> && it.title == UiText.StringResource(R.string.pref_title_default_preset) } shouldBe true
            }

            Then("TC-014: 세션 지속 시간 옵션이 [5, 10, 15, 25, 30, 45, 60]분을 포함해야 한다") {
                val selector = items.first { it.title == UiText.StringResource(R.string.pref_title_default_duration) } as SettingType.Selector<*>
                val options = selector.options
                options shouldContain 5.minutes
                options shouldContain 60.minutes
                options shouldHaveSize 7
            }

            Then("TC-015: 기본 프리셋 옵션에 null(선택 안 함)이 포함되어야 한다") {
                val selector = items.first { it.title == UiText.StringResource(R.string.pref_title_default_preset) } as SettingType.Selector<*>
                selector.options shouldContain null
            }

            Then("TC-016: Preset 목록이 변경될 때 기본 프리셋 옵션이 업데이트되어야 한다") {
                val newPresets = listOf(Preset(1, "Test", 25.minutes, 0, Instant.now()))
                every { getAllPresetsUseCase() } returns flowOf(newPresets)

                val newViewModel = SettingViewModel(
                    settingsRepository = settingsRepository,
                    getAllPresetsUseCase = getAllPresetsUseCase,
                    inAppReviewManager = mockk(relaxed = true),
                    analyticsHelper = mockk(relaxed = true),
                    appVersionName = "1.0.0"
                )
                testDispatcher.scheduler.advanceUntilIdle()

                val newItems = newViewModel.settingItems.value.filter { it.category == SettingCategory.TIMER }
                val selector = newItems.first { it.title == UiText.StringResource(R.string.pref_title_default_preset) } as SettingType.Selector<*>

                selector.options shouldContain newPresets[0]
            }
        }
    }

    Given("상호작용 설정이 생성될 때") {
        When("createInteractionSettings()가 호출되면") {
            val items = viewModel.settingItems.value.filter { it.category == SettingCategory.INTERACTION }

            Then("TC-017: '최소화된 컨트롤' Toggle이 포함되어야 한다") {
                items.any { it is SettingType.Toggle && it.title == UiText.StringResource(R.string.pref_title_minimized_controls) } shouldBe true
            }

            Then("TC-018: '펄스 애니메이션' Toggle이 포함되어야 한다") {
                items.any { it is SettingType.Toggle && it.title == UiText.StringResource(R.string.pref_title_pulse_animation) } shouldBe true
            }

            Then("TC-018-1: '화면 자동 회전' Toggle이 포함되어야 한다") {
                items.any { it is SettingType.Toggle && it.title == UiText.StringResource(R.string.pref_title_screen_rotation) } shouldBe true
            }

            Then("TC-019: 최소화된 컨트롤의 기본값이 DEFAULT_IS_MINIMIZED_CONTROLS이어야 한다") {
                val toggle = items.first { it.title == UiText.StringResource(R.string.pref_title_minimized_controls) } as SettingType.Toggle
                toggle.defaultValue shouldBe SettingsPreferencesDataSource.DEFAULT_IS_MINIMIZED_CONTROLS
            }

            Then("TC-020: 펄스 애니메이션의 기본값이 DEFAULT_IS_PULSE_ANIMATION_ENABLED이어야 한다") {
                val toggle = items.first { it.title == UiText.StringResource(R.string.pref_title_pulse_animation) } as SettingType.Toggle
                toggle.defaultValue shouldBe SettingsPreferencesDataSource.DEFAULT_IS_PULSE_ANIMATION_ENABLED
            }

            Then("TC-020-1: 화면 자동 회전의 기본값이 DEFAULT_IS_SCREEN_ROTATION_ENABLED이어야 한다") {
                val toggle = items.first { it.title == UiText.StringResource(R.string.pref_title_screen_rotation) } as SettingType.Toggle
                toggle.defaultValue shouldBe SettingsPreferencesDataSource.DEFAULT_IS_SCREEN_ROTATION_ENABLED
            }
        }
    }

    // 변경점: coVerify 대신 settingsRepository의 실제 값(value)을 검증함

    Given("테마 모드를 업데이트할 때") {
        When("updateThemeMode(ThemeMode.SYSTEM)이 호출되면") {
            viewModel.updateThemeMode(ThemeMode.SYSTEM)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-021: Repository의 값이 SYSTEM으로 변경되어야 한다") {
                settingsRepository.themeMode.value shouldBe ThemeMode.SYSTEM
            }
        }

        When("updateThemeMode(ThemeMode.LIGHT)가 호출되면") {
            viewModel.updateThemeMode(ThemeMode.LIGHT)
            testDispatcher.scheduler.runCurrent()

            Then("TC-023: Repository의 값이 LIGHT로 변경되어야 한다") {
                settingsRepository.themeMode.value shouldBe ThemeMode.LIGHT
            }
        }

        When("updateThemeMode(ThemeMode.DARK)가 호출되면") {
            viewModel.updateThemeMode(ThemeMode.DARK)
            testDispatcher.scheduler.runCurrent()

            Then("TC-024: Repository의 값이 DARK로 변경되어야 한다") {
                settingsRepository.themeMode.value shouldBe ThemeMode.DARK
            }
        }
    }

    Given("알림 설정을 업데이트할 때") {
        When("updateNotificationSoundType이 호출되면") {
            val sound = NotificationSoundType.DEFAULT
            viewModel.updateNotificationSoundType(sound)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-025: Repository의 알림음 설정이 변경되어야 한다") {
                settingsRepository.notificationSoundType.value shouldBe sound
            }
        }

        When("updateIsNotificationVibrate이 호출되면") {
            viewModel.updateIsNotificationVibrate(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-026: Repository의 진동 설정이 true로 변경되어야 한다") {
                settingsRepository.isNotificationVibrate.value shouldBe true
            }
        }

        When("updateIsTickSound가 호출되면") {
            viewModel.updateIsTickSound(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-027: Repository의 초침음 설정이 true로 변경되어야 한다") {
                settingsRepository.isTickSound.value shouldBe true
            }
        }
    }

    Given("타이머 설정을 업데이트할 때") {
        When("updateIsRememberLastSession이 호출되면") {
            viewModel.updateIsRememberLastSession(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-028: Repository의 마지막 세션 기억 설정이 true로 변경되어야 한다") {
                settingsRepository.isRememberLastSession.value shouldBe true
            }
        }

        When("updateIsScreenOn이 호출되면") {
            viewModel.updateIsScreenOn(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-029: Repository의 화면 켜짐 설정이 true로 변경되어야 한다") {
                settingsRepository.isScreenOn.value shouldBe true
            }
        }

        When("updateDefaultSessionDuration이 호출되면") {
            val duration = 25.minutes
            viewModel.updateDefaultSessionDuration(duration)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-030: Repository의 기본 지속 시간이 변경되어야 한다") {
                settingsRepository.defaultSessionDuration.value shouldBe duration
            }
        }

        When("updateDefaultPresetId가 호출되면") {
            viewModel.updateDefaultPresetId(1)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-031: Repository의 기본 프리셋 ID가 1로 변경되어야 한다") {
                settingsRepository.defaultPresetId.value shouldBe 1
            }
        }

        When("updateDefaultPresetId에 null을 전달하면") {
            viewModel.updateDefaultPresetId(null)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-032: Repository의 기본 프리셋 ID가 null로 변경되어야 한다") {
                settingsRepository.defaultPresetId.value shouldBe null
            }
        }
    }

    Given("상호작용 설정을 업데이트할 때") {
        When("updateIsHapticFeedback이 호출되면") {
            viewModel.updateIsHapticFeedback(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-033: Repository의 햅틱 피드백 설정이 true로 변경되어야 한다") {
                settingsRepository.isHapticFeedback.value shouldBe true
            }
        }

        When("updateIsMinimizedControls가 호출되면") {
            viewModel.updateIsMinimizedControls(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-034: Repository의 최소화 컨트롤 설정이 true로 변경되어야 한다") {
                settingsRepository.isMinimizedControls.value shouldBe true
            }
        }

        When("updateIsPulseAnimationEnabled가 호출되면") {
            viewModel.updateIsPulseAnimationEnabled(true)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-035: Repository의 펄스 애니메이션 설정이 true로 변경되어야 한다") {
                settingsRepository.isPulseAnimationEnabled.value shouldBe true
            }
        }

        When("updateIsScreenRotationEnabled가 호출되면") {
            viewModel.updateIsScreenRotationEnabled(false)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-035-1: Repository의 화면 회전 설정이 false로 변경되어야 한다") {
                settingsRepository.isScreenRotationEnabled.value shouldBe false
            }
        }
    }

    Given("repository 업데이트 중 예외가 발생할 때") {
        When("updateThemeMode에서 예외가 발생하도록 설정하면") {
            // MockK stubbing 대신 FakeRepository의 기능 사용
            settingsRepository.setShouldFail("updateThemeMode", true)

            // LogUtil은 Static Mocking 필요 (Repository 외부 의존성)
            mockkObject(LogUtil)
            every { LogUtil.e(any<String>(), any()) } just Runs

            viewModel.updateThemeMode(ThemeMode.DARK)
            testDispatcher.scheduler.advanceUntilIdle()

            Then("TC-036: 로그가 기록되어야 한다") {
                verify { LogUtil.e(any<String>(), any()) }
            }

            Then("TC-037: 값이 변경되지 않아야 한다 (예외 발생으로 인한 중단)") {
                // 초기값이 SYSTEM이라고 가정하거나, 변경 시도가 실패했는지 확인
                settingsRepository.themeMode.value shouldBe ThemeMode.SYSTEM // beforeEach에서 초기화된 값
            }
        }

        When("여러 설정을 연속으로 업데이트할 때 하나만 실패하도록 설정하면") {
            Then("TC-038: 실패한 설정 외에는 정상적으로 처리되어야 한다") {
                // updateThemeMode만 실패하도록 설정
                settingsRepository.setShouldFail("updateThemeMode", true)
                settingsRepository.setShouldFail("updateScreenOn", false) // 명시적 성공 설정 (기본값 false지만 확실하게)

                mockkObject(LogUtil)
                every { LogUtil.e(any<String>(), any()) } just Runs

                viewModel.updateThemeMode(ThemeMode.DARK) // 실패 예상
                viewModel.updateIsScreenOn(false)         // 성공 예상
                testDispatcher.scheduler.advanceUntilIdle()

                // ThemeMode는 변경되지 않아야 함 (초기값 유지)
                settingsRepository.themeMode.value shouldBe ThemeMode.SYSTEM
                // ScreenOn은 변경되어야 함
                settingsRepository.isScreenOn.value shouldBe false
            }
        }
    }

    Given("settingItems StateFlow를 관찰할 때") {
        When("ViewModel이 초기화되면") {
            Then("TC-039: 초기에는 빈 리스트를 방출해야 한다 (StateFlow 초기값 확인)") {
                // 주의: StateFlow는 항상 초기값을 가짐.
                // ViewModel init 블록에서 combine을 사용하므로,
                // Repository의 초기값들이 반영된 리스트가 즉시 생성될 수 있음.
                // 만약 Loading 상태가 없다면 shouldNotBeEmpty일 수 있음.
                // 여기서는 기존 테스트 로직을 존중하되, FakeRepo는 즉시 값을 방출함을 인지해야 함.

                val freshVM = SettingViewModel(
                    settingsRepository = settingsRepository,
                    getAllPresetsUseCase = getAllPresetsUseCase,
                    inAppReviewManager = mockk(relaxed = true),
                    analyticsHelper = mockk(relaxed = true),
                    appVersionName = "1.0.0"
                )
                // FakeRepo는 기본값을 즉시 가지고 있으므로 비어있지 않을 가능성이 높음.
                // 로직상 combine이 즉시 실행됨.
                freshVM.settingItems.value.shouldHaveSize(0)
            }
        }

        When("Preset 목록이 변경되면") {
            Then("TC-040: settingItems가 재구성되어야 한다") {
                val flow = MutableStateFlow<List<Preset>>(emptyList())
                every { getAllPresetsUseCase() } returns flow

                val vm = SettingViewModel(
                    settingsRepository = settingsRepository,
                    getAllPresetsUseCase = getAllPresetsUseCase,
                    inAppReviewManager = mockk(relaxed = true),
                    analyticsHelper = mockk(relaxed = true),
                    appVersionName = "1.0.0"
                )
                testDispatcher.scheduler.advanceUntilIdle()

                flow.value = listOf(Preset(1, "A", 1.minutes, 0, Instant.now()))
                testDispatcher.scheduler.advanceUntilIdle()

                vm.settingItems.value.shouldNotBeEmpty()
            }
        }
    }

    Given("ViewModel 전체 흐름을 테스트할 때") {
        When("초기화부터 설정 업데이트까지 수행하면") {
            Then("TC-042: 전체 흐름이 정상 동작해야 한다") {
                viewModel.updateThemeMode(ThemeMode.DARK)
                testDispatcher.scheduler.advanceUntilIdle()

                // 검증: Repository 상태 확인
                settingsRepository.themeMode.value shouldBe ThemeMode.DARK
                viewModel.settingItems.value.shouldNotBeEmpty()
            }
        }

        When("여러 설정을 동시에 변경하면") {
            Then("TC-043: 모두 정상적으로 처리되어야 한다") {
                viewModel.updateThemeMode(ThemeMode.LIGHT)
                viewModel.updateIsScreenOn(false)
                testDispatcher.scheduler.advanceUntilIdle()

                settingsRepository.themeMode.value shouldBe ThemeMode.LIGHT
                settingsRepository.isScreenOn.value shouldBe false
            }
        }
    }
})