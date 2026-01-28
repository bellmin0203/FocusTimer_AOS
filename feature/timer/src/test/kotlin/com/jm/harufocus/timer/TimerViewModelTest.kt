package com.jm.harufocus.timer

import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.usecase.preset.ManagePresetUseCase
import com.jm.harufocus.domain.usecase.preset.PresetException
import com.jm.harufocus.domain.usecase.review.CanRequestReviewUseCase
import com.jm.harufocus.setting.util.FakeSettingsRepository
import com.jm.harufocus.timer.model.SetTimeError
import com.jm.harufocus.timer.model.TimerError
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.usecase.TimerStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimerViewModelTest : BehaviorSpec({

    val testDispatcher: TestDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    Given("TimerViewModel이 초기화될 때") {

        When("ViewModel이 생성되면") {

            Then("저장된 프리셋 목록을 불러와야 한다") {
                runTest {
                    val testPresets = listOf(
                        Preset(
                            id = 100,
                            name = "Test Preset",
                            duration = 4.minutes,
                            createdAt = Instant.now()
                        )
                    )
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        every { getAllPresets() } returns flowOf(testPresets)
                    }

                    val robot = TimerRobot(scope = this, managePresetUseCase = managePresetUseCase)
                    verify(exactly = 1) { managePresetUseCase.getAllPresets() }

                    robot.verifyState {
                        presets shouldBe testPresets
                    }
                }
            }

            Then("사용자 설정을 관찰하고 UI 상태에 반영해야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    val fakeSettings = robot.settingsRepository as FakeSettingsRepository

                    fakeSettings.updateScreenOn(false)
                    fakeSettings.updateHapticFeedback(false)
                    robot.runCurrent()

                    robot.verifyState {
                        isScreenOnEnabled shouldBe false
                        isHapticFeedbackEnabled shouldBe false
                    }
                }
            }

            Then("마지막 세션 기억 설정에 따라 초기 시간을 설정해야 한다") {
                runTest {
                    val fakeSettings = FakeSettingsRepository()
                    fakeSettings.updateRememberLastSession(false)
                    fakeSettings.updateDefaultSessionDuration(30.minutes)

                    val robot = TimerRobot(this, settingsRepository = fakeSettings)
                    robot.runCurrent()

                    robot.verifyState {
                        initialTime shouldBe 30.minutes
                    }
                }
            }
        }
    }

    Given("타이머가 정지(Idle) 상태일 때") {
        When("사용자가 시간 진행바를 드래그하면 (DragProgress)") {
            Then("UI 상태의 타이머 시간이 업데이트되어야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(0.minutes) // Reset

                    robot.drag(0.5f) // 30 minutes

                    robot.verifyState {
                        progress shouldBe 0.5f
                        initialTime shouldBe 30.minutes
                    }
                }
            }

            Then("위젯에도 변경된 시간이 반영되어야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.drag(0.5f)
                    verify { robot.widgetUpdater.onTimeSet(30.minutes, any()) }
                }
            }
        }

        When("사용자가 시작 버튼을 누르면 (Start)") {
            And("설정된 시간이 0보다 크면") {
                Then("타이머 서비스 시작 이벤트를 발생시켜야 한다") {
                    runTest {
                        val robot = TimerRobot(this)
                        robot.setTime(25.minutes)

                        robot.start()

                        robot.verifySideEffect {
                            it shouldBe TimerSideEffect.StartTimerService(25.minutes.inWholeMilliseconds)
                        }

                        robot.stop()
                    }
                }

                Then("Analytics에 타이머 시작 이벤트를 기록해야 한다") {
                    runTest {
                        val robot = TimerRobot(this)
                        robot.setTime(25.minutes)

                        robot.start()

                        verify {
                            robot.analyticsHelper.logTimerStarted(
                                durationMinutes = 25,
                                presetId = null,
                                presetName = null,
                                inputMethod = any()
                            )
                        }

                        robot.stop()
                    }
                }
            }

            And("설정된 시간이 0이면") {
                Then("에러 메시지(ShowError)를 발생시켜야 한다") {
                    runTest {
                        val robot = TimerRobot(this)
                        robot.setTime(0.minutes)

                        robot.start()

                        robot.verifySideEffect {
                            it shouldBe TimerSideEffect.ShowError(TimerError.SetTime(SetTimeError.InvalidTime))
                        }
                    }
                }
            }
        }

        When("사용자가 프리셋을 선택하면 (SelectPreset)") {
            Then("해당 프리셋의 시간으로 타이머가 설정되어야 한다") {
                runTest {
                    val preset = Preset(
                        id = 1,
                        name = "Test",
                        duration = 17.minutes,
                        createdAt = Instant.now()
                    )
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        coEvery { selectPreset(any(), any(), any()) } returns Result.success(preset)
                    }

                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)

                    robot.onIntent(TimerIntent.SelectPreset(1))

                    robot.verifyState {
                        selectedPreset shouldBe preset
                        initialTime shouldBe preset.duration
                    }
                }
            }

            Then("선택 완료 스낵바를 보여줘야 한다") {
                runTest {
                    val preset = Preset(
                        id = 1,
                        name = "Test",
                        duration = 16.minutes,
                        createdAt = Instant.now()
                    )
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        coEvery { selectPreset(any(), any(), any()) } returns Result.success(preset)
                    }

                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)
                    robot.onIntent(TimerIntent.SelectPreset(1))

                    robot.verifySideEffect {
                        it.shouldBeInstanceOf<TimerSideEffect.ShowSnackbar>()
                    }
                }
            }
        }
    }

    Given("타이머가 실행(Running) 중일 때") {
        When("사용자가 일시정지 버튼을 누르면 (Pause)") {
            Then("타이머 서비스 일시정지 이벤트를 발생시켜야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(25.minutes)
                    robot.start()
                    robot.verifySideEffect {
                        it shouldBe TimerSideEffect.StartTimerService(25.minutes.inWholeMilliseconds)
                    }

                    robot.advanceTime(1.seconds) // State -> Running

                    robot.pause()

                    robot.verifySideEffect {
                        it shouldBe TimerSideEffect.PauseTimerService
                    }
                }
            }

            Then("Analytics에 일시정지 이벤트를 기록해야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(25.minutes)
                    robot.start()
                    // Analytics check doesn't care about side effects
                    robot.advanceTime(1.seconds)

                    robot.pause()

                    verify { robot.analyticsHelper.logTimerPaused(any(), any()) }
                }
            }
        }

        When("사용자가 정지 버튼을 누르면 (Stop)") {
            Then("타이머 서비스 정지 이벤트를 발생시켜야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(25.minutes)
                    robot.start()
                    robot.consumeSideEffect() // StartTimerService 소비

                    robot.stop()

                    robot.verifySideEffect {
                        it shouldBe TimerSideEffect.StopTimerService
                    }
                }
            }
        }

        When("TimerManager로부터 상태 업데이트(Running)를 받으면") {
            Then("UI 상태의 남은 시간과 진행률이 업데이트되어야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(10.minutes)
                    robot.start()
                    robot.consumeSideEffect()

                    robot.advanceTime(1.minutes)

                    robot.verifyState {
                        remainingTime shouldBe 9.minutes
                        status shouldBe TimerStatus.Running
                    }

                    robot.stop()
                }
            }
        }
    }

    Given("타이머가 일시정지(Paused) 상태일 때") {
        When("사용자가 재개 버튼을 누르면 (Resume)") {
            Then("타이머 서비스 재개 이벤트를 발생시켜야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(10.minutes)
                    robot.start()
                    robot.consumeSideEffect() // Start

                    robot.advanceTime(1.seconds)
                    robot.pause()
                    robot.consumeSideEffect() // Pause

                    robot.resume()

                    robot.verifySideEffect {
                        it shouldBe TimerSideEffect.ResumeTimerService
                    }

                    robot.stop()
                }
            }
        }
    }

    Given("타이머가 완료(Completed) 상태가 되었을 때") {
        When("TimerManager로부터 완료 상태를 전달받으면") {
            Then("완료 알림음과 진동을 재생해야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(1.minutes)
                    robot.start()

                    robot.advanceTime(1.minutes + 1.seconds) // Complete

                    verify { robot.notificationSoundPlayer.playTimerComplete(any()) }

                    robot.stop()
                }
            }

            Then("완료 화면 표시 이벤트를 발생시켜야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(1.minutes)
                    robot.start()
                    robot.consumeSideEffect() // Start

                    robot.advanceTime(1.minutes + 1.seconds)

                    robot.verifySideEffect {
                        it shouldBe TimerSideEffect.ShowTimerCompleted
                    }

                    robot.stop()
                }
            }
        }

        When("사용자가 완료 확인 버튼을 누르면 (Complete)") {
            Then("타이머 서비스 완료 처리 이벤트를 발생시켜야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(1.minutes)
                    robot.start()
                    robot.consumeSideEffect() // Start

                    robot.advanceTime(1.minutes + 1.seconds) // Complete State
                    robot.consumeSideEffect() // ShowTimerCompleted

                    robot.complete()
                    robot.testSideEffects {
                        awaitItem().shouldBeInstanceOf<TimerSideEffect.ShowSnackbar>()
                        awaitItem() shouldBe TimerSideEffect.CompleteTimerService
                    }
                }
            }

            Then("완료 스낵바를 보여줘야 한다") {
                runTest {
                    val robot = TimerRobot(this)
                    robot.setTime(1.minutes)
                    robot.start()
                    robot.consumeSideEffect() // Start

                    robot.advanceTime(1.minutes + 1.seconds)
                    robot.consumeSideEffect() // ShowTimerCompleted

                    robot.complete()

                    robot.verifySideEffect {
                        it.shouldBeInstanceOf<TimerSideEffect.ShowSnackbar>()
                    }
                }
            }
        }
    }

    Given("프리셋 관리 기능에서") {
        When("새로운 프리셋을 저장하면 (SaveAsPreset)") {
            Then("프리셋이 성공적으로 저장되면 스낵바를 보여준다") {
                runTest {
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        coEvery { addPreset(any(), any(), any()) } returns Result.success(1L)
                    }

                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)
                    robot.onIntent(TimerIntent.SaveAsPreset("New", 10.minutes, 1))

                    robot.verifySideEffect {
                        it.shouldBeInstanceOf<TimerSideEffect.ShowSnackbar>()
                    }
                }
            }

            Then("저장에 실패하면 에러를 보여준다") {
                runTest {
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        coEvery { addPreset(any(), any(), any()) } returns Result.failure<Long>(
                            PresetException.InvalidName()
                        )
                    }
                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)

                    robot.onIntent(TimerIntent.SaveAsPreset("", 10.minutes, 1))

                    robot.verifySideEffect {
                        it.shouldBeInstanceOf<TimerSideEffect.ShowError>()
                    }
                }
            }
        }

        When("프리셋을 삭제하면 (DeletePreset)") {
            Then("프리셋이 삭제되고 스낵바를 보여준다") {
                runTest {
                    val preset = Preset(100, "Test", 10.minutes, createdAt = Instant.now())
                    val presetsFlow = MutableStateFlow(listOf(preset))

                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        every { getAllPresets() } returns presetsFlow
                        coEvery { deletePreset(any()) } coAnswers {
                            presetsFlow.value = emptyList()
                            Result.success(Unit)
                        }
                    }

                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)
                    robot.verifyState {
                        presets shouldBe listOf(preset)
                    }

                    robot.onIntent(TimerIntent.DeletePreset(100))
                    robot.runCurrent()

                    robot.verifyState {
                        presets shouldBe emptyList()
                    }

                    robot.verifySideEffect {
                        it.shouldBeInstanceOf<TimerSideEffect.ShowSnackbar>()
                    }
                }
            }

            Then("삭제된 프리셋이 선택된 상태였다면 선택을 해제한다") {
                runTest {
                    val preset = Preset(100, "Test", 10.minutes, createdAt = Instant.now())
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        every { getAllPresets() } returns flowOf(listOf(preset))
                        coEvery { selectPreset(any(), any(), any()) } returns Result.success(preset)
                        coEvery { deletePreset(any()) } returns Result.success(Unit)
                    }
                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)

                    robot.onIntent(TimerIntent.SelectPreset(100)) // Select first
                    robot.onIntent(TimerIntent.DeletePreset(100)) // Delete

                    robot.verifyState {
                        selectedPreset.shouldBeNull()
                    }
                }
            }
        }

        When("프리셋을 수정하면 (UpdatePreset)") {
            Then("프리셋 정보가 업데이트되고 스낵바를 보여준다") {
                runTest {
                    val preset = Preset(100, "Test", 10.minutes, createdAt = Instant.now())
                    val managePresetUseCase = mockk<ManagePresetUseCase>(relaxed = true) {
                        every { getAllPresets() } returns flowOf(listOf(preset))
                        coEvery { updatePreset(any()) } returns Result.success(Unit)
                    }
                    val robot = TimerRobot(this, managePresetUseCase = managePresetUseCase)
                    robot.runCurrent()

                    robot.onIntent(TimerIntent.UpdatePreset(preset.copy(name = "Updated")))

                    robot.verifySideEffect {
                        it.shouldBeInstanceOf<TimerSideEffect.ShowSnackbar>()
                    }
                }
            }
        }
    }

    Given("앱 리뷰 요청 조건 확인") {
        When("타이머가 Idle 상태로 돌아올 때") {
            Then("리뷰 요청 조건이 충족되면 리뷰 요청 이벤트를 발생시킨다") {
                runTest {
                    val canRequestReviewUseCase = mockk<CanRequestReviewUseCase> {
                        coEvery { this@mockk.invoke() } returns true
                    }
                    val robot = TimerRobot(this, canRequestReviewUseCase = canRequestReviewUseCase)

                    // Trigger state change to Idle (e.g. from Drag or Init)
                    // Initial state is Idle, so it might check on Init if it checks in observeTimerStateChanges
                    // But ViewModel checks it in `TimerStatus.Idle` block of `observeTimerStateChanges`.
                    // So we need to ensure `timerManager` emits `Idle`.
                    // The `TimerManagerImpl` emits initial state.

                    robot.runCurrent()

                    robot.verifySideEffect {
                        it shouldBe TimerSideEffect.RequestInAppReview
                    }
                }
            }
        }
    }
})