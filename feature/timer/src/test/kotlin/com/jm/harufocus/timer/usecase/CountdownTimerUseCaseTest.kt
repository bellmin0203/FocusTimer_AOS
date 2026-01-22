package com.jm.harufocus.timer.usecase

import com.jm.harufocus.timer.model.TimerEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CountdownTimerUseCaseTest : BehaviorSpec({

    val useCase = CountdownTimerUseCase()

    Given("2.1 유효성 검사 (Input Validation)") {
        When("총 시간이 0인 경우 (V-01)") {
            Then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    useCase(0.seconds).collect()
                }
            }
        }
        When("총 시간이 음수인 경우 (V-02)") {
            Then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    useCase((-10).seconds).collect()
                }
            }
        }
        When("리마인더 시간이 음수인 경우 (V-03)") {
            Then("IllegalArgumentException이 발생해야 한다") {
                shouldThrow<IllegalArgumentException> {
                    useCase(10.seconds, listOf((-5).seconds)).collect()
                }
            }
        }
    }

    Given("2.2 기본 동작 (Normal Operation)") {
        When("3초 타이머 실행 시 (N-01)") {
            Then("3, 2, 1초 Tick 후 Completed가 발생해야 한다") {
                runTest {
                    val events = useCase(3.seconds).toList()
                    events shouldHaveSize 4
                    events[0].shouldBeInstanceOf<TimerEvent.Tick>().remainingTime shouldBe 3.seconds
                    events[1].shouldBeInstanceOf<TimerEvent.Tick>().remainingTime shouldBe 2.seconds
                    events[2].shouldBeInstanceOf<TimerEvent.Tick>().remainingTime shouldBe 1.seconds
                    events[3].shouldBeInstanceOf<TimerEvent.Completed>()
                }
            }
        }
        When("1초 타이머 실행 시 (N-02)") {
            Then("마지막에 Completed 이벤트가 발생해야 한다") {
                runTest {
                    val events = useCase(1.seconds).toList()
                    events.last().shouldBeInstanceOf<TimerEvent.Completed>()
                }
            }
        }
    }

    Given("2.3 리마인더 기능 (Reminder Logic)") {
        When("5초 타이머, 3초 리마인더 (R-01)") {
            Then("3초 Tick 시점에 Reminder가 발생해야 한다") {
                runTest {
                    val events = useCase(5.seconds, listOf(3.seconds)).toList()
                    // Expected flow: T(5) -> T(4) -> T(3) -> R(3) -> T(2) -> T(1) -> C
                    // Note: UseCase implementation emits Tick then Checks reminder immediately
                    val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                    reminderEvents shouldHaveSize 1
                    reminderEvents[0].remainingTime shouldBe 3.seconds
                    
                    // Verify correct position relative to Tick(3s)
                    val tick3Index = events.indexOfFirst { it is TimerEvent.Tick && it.remainingTime == 3.seconds }
                    val reminderIndex = events.indexOfFirst { it is TimerEvent.Reminder && it.remainingTime == 3.seconds }
                    
                    (tick3Index < reminderIndex) shouldBe true
                }
            }
        }
        When("10초 타이머, [5초, 2초] 리마인더 (R-02)") {
            Then("각 시간에 리마인더가 발생해야 한다") {
                runTest {
                    val events = useCase(10.seconds, listOf(5.seconds, 2.seconds)).toList()
                    val reminders = events.filterIsInstance<TimerEvent.Reminder>()
                    reminders shouldHaveSize 2
                    reminders[0].remainingTime shouldBe 5.seconds
                    reminders[1].remainingTime shouldBe 2.seconds
                }
            }
        }
        When("10초 타이머, 10초 리마인더 (R-03)") {
            Then("시작 즉시 리마인더가 발생해야 한다") {
                runTest {
                    val events = useCase(10.seconds, listOf(10.seconds)).toList()
                    // T(10) -> R(10) -> ...
                    events[0].shouldBeInstanceOf<TimerEvent.Tick>().remainingTime shouldBe 10.seconds
                    events[1].shouldBeInstanceOf<TimerEvent.Reminder>().remainingTime shouldBe 10.seconds
                }
            }
        }
        When("중복된 리마인더 [3초, 3초] (R-04)") {
            Then("한 번만 발생해야 한다") {
                runTest {
                    val events = useCase(5.seconds, listOf(3.seconds, 3.seconds)).toList()
                    events.filterIsInstance<TimerEvent.Reminder>() shouldHaveSize 1
                }
            }
        }
        When("3.5초 타이머, 3초 리마인더 (R-05)") {
            Then("Tick(2.5s) 시점에 Reminder(3s)가 발생해야 한다") {
                runTest {
                    // Logic check:
                    // T(3.5) -> delay 1s -> T(2.5) -> 2.5 <= 3.0 && 3.5 > 3.0 -> Emit R(3.0)
                    val events = useCase(3.5.seconds, listOf(3.seconds)).toList()
                    
                    val reminder = events.filterIsInstance<TimerEvent.Reminder>().first()
                    reminder.remainingTime shouldBe 3.seconds
                    
                    // Verify it comes after T(2.5)
                    val tick2_5Index = events.indexOfFirst { it is TimerEvent.Tick && it.remainingTime == 2.5.seconds }
                    val reminderIndex = events.indexOf(reminder)
                    
                    (tick2_5Index != -1) shouldBe true
                    (tick2_5Index < reminderIndex) shouldBe true
                }
            }
        }
    }

    Given("2.4 엣지 케이스 (Edge Cases)") {
        When("1초 미만(500ms) 타이머 (E-01)") {
            Then("Tick(500ms) 후 바로 Completed") {
                runTest {
                    val events = useCase(500.milliseconds).toList()
                    events shouldHaveSize 2
                    events[0].shouldBeInstanceOf<TimerEvent.Tick>().remainingTime shouldBe 500.milliseconds
                    events[1].shouldBeInstanceOf<TimerEvent.Completed>()
                }
            }
        }
        When("정확히 1초 타이머 (E-02)") {
            Then("Tick(1s) 후 Completed") {
                runTest {
                    val events = useCase(1.seconds).toList()
                    events shouldHaveSize 2
                    events[0].shouldBeInstanceOf<TimerEvent.Tick>().remainingTime shouldBe 1.seconds
                    events[1].shouldBeInstanceOf<TimerEvent.Completed>()
                }
            }
        }
    }
})
