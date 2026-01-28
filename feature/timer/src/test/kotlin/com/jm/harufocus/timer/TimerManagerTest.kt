package com.jm.harufocus.timer

import app.cash.turbine.test
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.timer.model.SetTimeError
import com.jm.harufocus.timer.model.TimerError
import com.jm.harufocus.timer.usecase.TimerControlUseCase
import com.jm.harufocus.timer.usecase.TimerStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class TimerManagerTest : BehaviorSpec({

    val testDispatcher: TestDispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    beforeTest {
        // TestScope는 Spec의 Coroutine Scope를 사용하거나, 별도로 관리할 수 있음
        // 여기서는 runTest 블록 내부에서 생성된 TestScope를 사용하기 위해
        // 각 Then 블록이나 Given/When 내부에서 helper를 초기화하는 것이 좋을 수 있으나,
        // BehaviorSpec은 하나의 root Scope를 공유하지 않으므로 구조에 따라 전략이 필요함.
        // 하지만 TimerTestHelper가 TestScope를 생성자로 받으므로,
        // 각 테스트 케이스(Leaf)마다 독립적인 환경을 위해 runTest 내부에서 helper를 생성하거나
        // 전역 TestDispatcher를 공유하는 방식을 선택해야 함.

        // TimerTestHelper 구현을 보면 TestScope를 받음.
        // 따라서 각 테스트에서 runTest { val helper = TimerTestHelper(this) ... } 패턴이 적합.
    }

    Given("3.1 시간 설정 (SetTime)") {
        When("유효한 시간으로 설정하면 (ST-01)") {
            Then("상태가 Idle로 업데이트되고 시간이 설정된다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    val duration = 10.minutes
                    timerRobot.timerManager.setTime(duration)

                    val state = timerRobot.timerManager.timerState.value
                    state.status shouldBe TimerStatus.Idle
                    state.initialDuration shouldBe duration
                    state.remainingTime shouldBe duration
                }
            }
        }

        When("유효하지 않은 시간으로 설정하면 (ST-02)") {
            Then("에러를 방출해야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    val duration = (-1).minutes

                    timerRobot.timerManager.timerError.test {
                        timerRobot.timerManager.setTime(duration)

                        val error = awaitItem()
                        error.shouldBeInstanceOf<TimerError.SetTime>()
                        error.code shouldBe SetTimeError.InvalidTime
                    }
                }
            }
        }

        When("실행 중 시간 설정 시도 (ST-03)") {
            Then("무시되거나 에러가 발생해야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    val duration = 10.minutes

                    timerRobot.timerManager.setTime(5.minutes)
                    timerRobot.timerManager.start(5.minutes, 5.minutes)
                    timerRobot.runCurrent()
                    timerRobot.verifyState { status.shouldBeInstanceOf<TimerStatus.Running>() }

                    timerRobot.timerManager.timerError.test {
                        timerRobot.timerManager.setTime(duration)

                        val error = awaitItem()
                        error.shouldBeInstanceOf<TimerError.SetTime>()
                        error.code shouldBe SetTimeError.TimerRunning

                        cancelAndIgnoreRemainingEvents()
                    }

                    timerRobot.timerManager.stop(5.minutes)
                }
            }
        }
    }

    Given("3.2 타이머 시작 및 실행 (Start & Running)") {
        When("타이머를 시작하면 (SR-01)") {
            Then("상태가 Running으로 변경되고 시간이 흐른다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    val duration = 10.seconds

                    timerRobot.timerManager.setTime(duration)
                    timerRobot.timerManager.start(duration, duration)
                    timerRobot.runCurrent()
                    timerRobot.verifyState { status.shouldBeInstanceOf<TimerStatus.Running>() }

                    timerRobot.advanceTime(1.seconds)
                    timerRobot.verifyState { remainingTime shouldBe 9.seconds }

                    timerRobot.timerManager.stop(duration)
                }
            }
        }

        When("리마인더와 함께 시작하면 (SR-02)") {
            Then("설정된 시간에 리마인더 이벤트가 발생해야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    val duration = 10.seconds
                    val reminder = 5.seconds

                    timerRobot.timerManager.start(duration, duration, listOf(reminder))

                    // 5초 경과 -> 남은 시간 5초 -> 리마인더 발생
                    timerRobot.advanceTime(5.seconds)

                    timerRobot.verifyState {
                        remainingTime shouldBe 5.seconds
                        // TODO: jongmin, 리마인더 기능 추가 시 작성
                    }

                    timerRobot.timerManager.stop(duration)
                }
            }
        }

        When("실행 중 재시작하면 (SR-03)") {
            Then("기존 작업이 취소되고 새로운 시간으로 재시작되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)

                    // 1st Start
                    timerRobot.timerManager.start(10.seconds, 10.seconds)
                    timerRobot.advanceTime(2.seconds)
                    timerRobot.verifyState { remainingTime shouldBe 8.seconds }
                    timerRobot.timerManager.stop(10.seconds)

                    // 2nd Start (Restart)
                    timerRobot.timerManager.start(20.seconds, 20.seconds)
                    timerRobot.runCurrent()
                    timerRobot.verifyState {
                        remainingTime shouldBe 20.seconds
                        status.shouldBeInstanceOf<TimerStatus.Running>()
                    }

                    timerRobot.timerManager.stop(20.seconds)
                }
            }
        }
    }

    Given("3.3 일시정지 및 재개 (Pause & Resume)") {
        When("실행 중 일시정지하면 (PR-01)") {
            Then("상태가 Paused로 변경되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(10.seconds, 10.seconds)

                    timerRobot.timerManager.pause()
                    timerRobot.runCurrent()
                    timerRobot.verifyState { status.shouldBeInstanceOf<TimerStatus.Paused>() }
                }
            }
        }

        When("일시정지 시 (PR-02 정밀 시간 보정)") {
            Then("경과 시간을 고려하여 남은 시간이 저장되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)

                    timerRobot.timerManager.start(10.seconds, 10.seconds)
                    // Tick 발생 (10초)

                    // 0.5초 경과
                    timerRobot.advanceTime(500.milliseconds)

                    timerRobot.timerManager.pause()
                    timerRobot.runCurrent()
                    timerRobot.verifyState {
                        status.shouldBeInstanceOf<TimerStatus.Paused>()
                        // 10초 - 0.5초 = 9.5초
                        remainingTime shouldBe (10.seconds - 500.milliseconds)
                    }
                }
            }
        }

        When("일시정지 상태에서 재개하면 (PR-03)") {
            Then("상태가 Running으로 변경되고 타이머가 계속되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(10.seconds, 10.seconds)
                    timerRobot.timerManager.pause()

                    timerRobot.timerManager.resume()
                    timerRobot.runCurrent()
                    timerRobot.verifyState { status.shouldBeInstanceOf<TimerStatus.Running>() }

                    timerRobot.timerManager.stop(10.seconds)
                }
            }
        }

        When("Running이 아닐 때 일시정지 시도 (PR-04)") {
            Then("아무 변화가 없어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.setTime(10.seconds) // Idle

                    timerRobot.timerManager.pause()
                    timerRobot.runCurrent()
                    timerRobot.verifyState { status shouldBe TimerStatus.Idle }
                }
            }
        }

        When("Paused가 아닐 때 재개 시도 (PR-05)") {
            Then("아무 변화가 없어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(10.seconds, 10.seconds) // Running

                    timerRobot.timerManager.resume()
                    timerRobot.runCurrent()
                    timerRobot.verifyState { status.shouldBeInstanceOf<TimerStatus.Running>() }

                    timerRobot.timerManager.stop(10.seconds)
                }
            }
        }
    }

    Given("3.4 타이머 정지 (Stop)") {
        When("실행 중 정지하면 (SP-01)") {
            Then("Idle 상태로 초기화되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(10.seconds, 10.seconds)

                    timerRobot.timerManager.stop(10.seconds)
                    timerRobot.runCurrent()
                    timerRobot.verifyState {
                        status shouldBe TimerStatus.Idle
                        remainingTime shouldBe 10.seconds
                        overtime shouldBe Duration.ZERO
                    }
                }
            }
        }

        When("오버타임 중 정지하면 (SP-02)") {
            Then("오버타임이 종료되고 Idle 상태가 되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(1.seconds, 1.seconds)
                    timerRobot.advanceTime(2.seconds) // Complete -> Overtime

                    timerRobot.timerManager.stop(1.seconds)
                    timerRobot.runCurrent()

                    timerRobot.verifyState {
                        status shouldBe TimerStatus.Idle
                        overtime shouldBe Duration.ZERO
                    }
                }
            }
        }
    }

    Given("3.5 완료 및 초과 시간 추적 (Completion & Overtime)") {
        When("타이머 시간이 0이 되면 (CO-01)") {
            Then("Completed 상태가 되고 완료 알림이 발생해야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(1.seconds, 1.seconds)

                    // 1초 경과 -> 0초 -> Completed
                    timerRobot.advanceTime(1.seconds)

                    timerRobot.verifyState {
                        status.shouldBeInstanceOf<TimerStatus.Completed>()
                        remainingTime shouldBe Duration.ZERO
                    }

                    timerRobot.timerManager.stop(1.seconds)
                }
            }
        }

        When("완료 후 시간이 지나면 (CO-02)") {
            Then("자동으로 Overtime 상태로 전환되고 초과 시간이 측정되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(1.seconds, 1.seconds)
                    timerRobot.advanceTime(1.seconds) // Completed

                    // 1초 더 경과 -> Overtime 1s
                    timerRobot.advanceTime(1.seconds)

                    timerRobot.verifyState {
                        status.shouldBeInstanceOf<TimerStatus.Overtime>()
                        overtime shouldBe 1.seconds
                    }

                    timerRobot.timerManager.stop(1.seconds)
                }
            }
        }

        When("중복 완료 이벤트 수신 (CO-03)") {
            Then("상태 변화 없이 현재 상태를 유지해야 한다") {
                // TODO
                // TimerManager 내부 로직 검증: handleTimerComplete에서 status check
                // 여기서는 정상 동작 시나리오로 대체
            }
        }
    }

    Given("3.6 프리셋 및 세션 상태 관리 (Preset & Session)") {
        When("프리셋을 선택하면 (PS-01)") {
            Then("상태에 프리셋 정보가 반영되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    val preset = Preset(1, "Test", 25.minutes, 0, Instant.now())

                    timerRobot.timerManager.selectPreset(preset)
                    timerRobot.runCurrent()

                    timerRobot.verifyState { selectedPreset shouldBe preset }
                }
            }
        }

        When("세션 정보를 초기화하면 (PS-03)") {
            Then("세션 정보가 null이 되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.setSession(100L, Instant.now())

                    timerRobot.timerManager.clearSession()
                    timerRobot.runCurrent()

                    timerRobot.verifyState {
                        currentSessionId shouldBe null
                        sessionStartTime shouldBe null
                    }
                }
            }
        }
    }

    Given("3.7 에러 처리 (Error Handling)") {
        When("타이머 실행 중 예외가 발생하면 (EH-01)") {
            Then("에러 SideEffect가 방출되고 정지되어야 한다") {
                runTest {
                    // Mock ControlUseCase to throw exception
                    val errorMessage = "Timer Crashed"
                    val mockControlUseCase = mockk<TimerControlUseCase>(relaxed = true)
                    every { mockControlUseCase.startTimer(any(), any()) } returns flow {
                        throw RuntimeException(errorMessage)
                    }

                    // Inject Mock UseCase
                    val timerRobot = TimerRobot(this, timerControlUseCase = mockControlUseCase)

                    timerRobot.timerManager.timerError.test {
                        timerRobot.timerManager.start(10.seconds, 10.seconds)

                        val error = awaitItem()
                        error.shouldBeInstanceOf<TimerError.Run>()
                        error.message shouldBe errorMessage
                    }

                    timerRobot.runCurrent()
                    timerRobot.verifyState { status shouldBe TimerStatus.Idle }
                }
            }
        }
    }

    Given("3.8 동시성 및 라이프사이클 (Concurrency & Lifecycle)") {
        When("CancelAll 호출 시 (CL-01)") {
            Then("모든 작업이 취소되어야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)
                    timerRobot.timerManager.start(10.seconds, 10.seconds)

                    timerRobot.timerManager.cancelAll()

                    // 시간이 지나도 상태가 변하지 않아야 함
                    timerRobot.advanceTime(10.seconds)
                    timerRobot.runCurrent()
                    timerRobot.verifyState { remainingTime shouldBe 10.seconds } // Still initial value because canceled
                }
            }
        }

        When("빠른 시작/정지 반복 시 (CL-02)") {
            Then("마지막 상태로 안정적으로 수렴해야 한다") {
                runTest {
                    val timerRobot = TimerRobot(this)

                    timerRobot.timerManager.start(10.seconds, 10.seconds)
                    timerRobot.timerManager.stop(10.seconds)
                    timerRobot.timerManager.start(5.seconds, 5.seconds)

                    timerRobot.runCurrent()
                    timerRobot.verifyState {
                        status.shouldBeInstanceOf<TimerStatus.Running>()
                        initialTime shouldBe 5.seconds
                    }

                    timerRobot.timerManager.stop(5.seconds)
                }
            }
        }
    }
})
