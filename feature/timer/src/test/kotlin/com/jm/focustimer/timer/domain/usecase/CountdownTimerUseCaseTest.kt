package com.jm.focustimer.timer.domain.usecase

import com.jm.focustimer.timer.model.TimerEvent
import com.jm.focustimer.timer.usecase.CountdownTimerUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.milliseconds

/**
 * CountdownTimerUseCase 테스트
 *
 * Kotest의 BehaviorSpec을 사용하여 Given-When-Then 스타일로 작성
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CountdownTimerUseCaseTest : BehaviorSpec({

    // 테스트 헬퍼: 타이머 이벤트 수집
    suspend fun TestScope.collectTimerEvents(
        durationMillis: Long,
        reminderThresholds: List<Long> = emptyList(),
    ): List<TimerEvent> {
        val events = mutableListOf<TimerEvent>()
        val useCase = CountdownTimerUseCase()
        val job = launch {
            useCase(
                durationMillis.milliseconds,
                reminderThresholds.map { it.milliseconds }
            ).collect { events.add(it) }
        }
        advanceTimeBy(durationMillis)
        advanceUntilIdle()
        job.join()
        return events
    }

    Given("5초 타이머가 주어졌을 때") {
        val durationMillis = FIVE_SECONDS

        When("타이머를 실행하면") {
            lateinit var events: List<TimerEvent>

            beforeEach {
                runTest {
                    events = collectTimerEvents(durationMillis)
                }
            }

            Then("5개의 Tick 이벤트와 1개의 Completed 이벤트가 발생해야 한다") {
                val tickEvents = events.filterIsInstance<TimerEvent.Tick>()
                val completedEvents = events.filterIsInstance<TimerEvent.Completed>()

                tickEvents.size shouldBe 5
                completedEvents.size shouldBe 1
            }

            Then("Tick 이벤트는 5초부터 1초까지 카운트다운 해야 한다") {
                val tickEvents = events.filterIsInstance<TimerEvent.Tick>()
                tickEvents[0].remainingTime shouldBe FIVE_SECONDS.milliseconds
                tickEvents[1].remainingTime shouldBe FOUR_SECONDS.milliseconds
                tickEvents[2].remainingTime shouldBe THREE_SECONDS.milliseconds
                tickEvents[3].remainingTime shouldBe TWO_SECONDS.milliseconds
                tickEvents[4].remainingTime shouldBe ONE_SECOND.milliseconds
            }

            Then("마지막 이벤트는 Completed 이벤트여야 한다") {
                events.last().shouldBeInstanceOf<TimerEvent.Completed>()
            }
        }
    }

    Given("10초 타이머와 5초 리마인더가 주어졌을 때") {
        val durationMillis = TEN_SECONDS
        val reminderThresholds = listOf(FIVE_SECONDS)

        When("타이머를 실행하면") {
            lateinit var events: List<TimerEvent>

            beforeEach {
                runTest {
                    events = collectTimerEvents(durationMillis, reminderThresholds)
                }
            }

            Then("5초 남았을 때 Reminder 이벤트가 발생해야 한다") {
                val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                reminderEvents.size shouldBe 1
                reminderEvents[0].remainingTime shouldBe FIVE_SECONDS.milliseconds
            }

            Then("Reminder 이벤트는 해당 Tick 이벤트 직후에 발생해야 한다") {
                val reminderIndex = events.indexOfFirst { it is TimerEvent.Reminder }
                val previousEvent = events[reminderIndex - 1]
                previousEvent.shouldBeInstanceOf<TimerEvent.Tick>()
                (previousEvent as TimerEvent.Tick).remainingTime shouldBe FIVE_SECONDS.milliseconds
            }
        }
    }

    Given("30초 타이머와 여러 개의 리마인더(10초, 5초, 3초)가 주어졌을 때") {
        val durationMillis = THIRTY_SECONDS
        val reminderThresholds = listOf(TEN_SECONDS, FIVE_SECONDS, THREE_SECONDS)

        When("타이머를 실행하면") {
            Then("각 임계값에 도달했을 때 Reminder 이벤트가 발생해야 한다") {
                runTest {
                    val events = collectTimerEvents(durationMillis, reminderThresholds)

                    val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                    reminderEvents.size shouldBe 3
                    reminderEvents[0].remainingTime shouldBe TEN_SECONDS.milliseconds
                    reminderEvents[1].remainingTime shouldBe FIVE_SECONDS.milliseconds
                    reminderEvents[2].remainingTime shouldBe THREE_SECONDS.milliseconds
                }
            }
        }
    }

    Given("중복된 리마인더가 주어졌을 때") {
        val durationMillis = TEN_SECONDS
        val reminderThresholds = listOf(FIVE_SECONDS, FIVE_SECONDS, THREE_SECONDS, THREE_SECONDS)

        When("타이머를 실행하면") {
            Then("중복된 리마인더는 제거되고 한 번씩만 발생해야 한다") {
                runTest {
                    val events = collectTimerEvents(durationMillis, reminderThresholds)

                    val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                    reminderEvents.size shouldBe 2
                    reminderEvents[0].remainingTime shouldBe FIVE_SECONDS.milliseconds
                    reminderEvents[1].remainingTime shouldBe THREE_SECONDS.milliseconds
                }
            }
        }
    }

    Given("리마인더 없이 1분 타이머가 주어졌을 때") {
        val durationMillis = ONE_MINUTE

        When("타이머를 실행하면") {
            lateinit var events: List<TimerEvent>

            beforeEach {
                runTest {
                    events = collectTimerEvents(durationMillis)
                }
            }

            Then("60개의 Tick 이벤트와 1개의 Completed 이벤트가 발생해야 한다") {
                val tickEvents = events.filterIsInstance<TimerEvent.Tick>()
                val completedEvents = events.filterIsInstance<TimerEvent.Completed>()

                tickEvents.size shouldBe 60
                completedEvents.size shouldBe 1
            }

            Then("Reminder 이벤트는 발생하지 않아야 한다") {
                val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                reminderEvents.size shouldBe 0
            }
        }
    }

    Given("음수 duration이 주어졌을 때") {
        val durationMillis = NEGATIVE_DURATION

        When("타이머를 실행하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                runTest {
                    val useCase = CountdownTimerUseCase()
                    val result = runCatching {
                        useCase(durationMillis.milliseconds).collect { }
                    }
                    result.isFailure shouldBe true
                    result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
                }
            }
        }
    }

    Given("음수 리마인더가 주어졌을 때") {
        val durationMillis = TEN_SECONDS
        val reminderThresholds = listOf(FIVE_SECONDS, NEGATIVE_DURATION)

        When("타이머를 실행하면") {
            Then("IllegalArgumentException이 발생해야 한다") {
                runTest {
                    val useCase = CountdownTimerUseCase()
                    val result = runCatching {
                        useCase(
                            durationMillis.milliseconds,
                            reminderThresholds.map { it.milliseconds }
                        ).collect { }
                    }
                    result.isFailure shouldBe true
                    result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
                }
            }
        }
    }

    Given("5분 타이머와 1분, 30초 리마인더가 주어졌을 때") {
        val durationMillis = FIVE_MINUTES
        val reminderThresholds = listOf(ONE_MINUTE, THIRTY_SECONDS)

        When("타이머를 실행하면") {
            lateinit var events: List<TimerEvent>

            beforeEach {
                runTest {
                    events = collectTimerEvents(durationMillis, reminderThresholds)
                }
            }

            Then("총 300개의 Tick, 2개의 Reminder, 1개의 Completed 이벤트가 발생해야 한다") {
                val tickEvents = events.filterIsInstance<TimerEvent.Tick>()
                val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                val completedEvents = events.filterIsInstance<TimerEvent.Completed>()

                tickEvents.size shouldBe 300
                reminderEvents.size shouldBe 2
                completedEvents.size shouldBe 1
            }

            Then("1분과 30초에 Reminder가 발생해야 한다") {
                val reminderEvents = events.filterIsInstance<TimerEvent.Reminder>()
                reminderEvents[0].remainingTime shouldBe ONE_MINUTE.milliseconds
                reminderEvents[1].remainingTime shouldBe THIRTY_SECONDS.milliseconds
            }
        }
    }
}) {
    companion object {
        // 시간 상수 정의 (밀리초)
        private const val ONE_SECOND = 1000L
        private const val TWO_SECONDS = 2000L
        private const val THREE_SECONDS = 3000L
        private const val FOUR_SECONDS = 4000L
        private const val FIVE_SECONDS = 5000L
        private const val TEN_SECONDS = 10000L
        private const val THIRTY_SECONDS = 30000L
        private const val ONE_MINUTE = 60000L
        private const val FIVE_MINUTES = 300000L

        // 테스트용 특수 값
        private const val NEGATIVE_DURATION = -1000L
    }
}
