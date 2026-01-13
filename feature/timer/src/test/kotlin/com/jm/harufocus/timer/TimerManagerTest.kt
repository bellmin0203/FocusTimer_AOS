package com.jm.harufocus.timer

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jm.harufocus.testing.rule.MainDispatcherExtension
import com.jm.harufocus.timer.model.SetTimeError
import com.jm.harufocus.timer.model.TimerError
import com.jm.harufocus.timer.model.TimerEvent
import com.jm.harufocus.timer.usecase.TimerControlUseCase
import com.jm.harufocus.timer.usecase.TimerStatus
import com.jm.harufocus.timer.usecase.TimerValidationResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
class TimerManagerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension(StandardTestDispatcher())
    }

    private lateinit var timerManager: TimerManagerImpl
    private val timerControlUseCase: TimerControlUseCase = mockk()

    private fun runTimerTestWithCleanup(
        testBody: suspend TestScope.() -> Unit
    ) = runTest {
        try {
            testBody()
        } finally {
            timerManager.stop(0.seconds)
        }
    }

    @BeforeEach
    fun setUp() {
        timerManager = TimerManagerImpl(
            timerControlUseCase = timerControlUseCase,
            defaultDispatcher = Dispatchers.Main,
        )
    }

    @Test
    fun `유효한 시간으로 setTime시 상태가 업데이트된다`() = runTimerTestWithCleanup {
        // Given
        val duration = 10.minutes
        every { timerControlUseCase.validateTimerTime(duration) } returns TimerValidationResult.Valid

        // When
        timerManager.setTime(duration)

        // Then
        timerManager.timerState.test {
            val state = awaitItem()
            assertThat(state.status).isEqualTo(TimerStatus.Idle)
            assertThat(state.initialDuration).isEqualTo(duration)
            assertThat(state.remainingTime).isEqualTo(duration)
        }
    }

    @Test
    fun `유효하지 않은 시간으로 setTime시 에러를 방출한다`() = runTimerTestWithCleanup {
        // Given
        val duration = 10.minutes
        every { timerControlUseCase.validateTimerTime(duration) } returns TimerValidationResult.InvalidTime

        // When
        timerManager.setTime(duration)

        // Then
        timerManager.timerError.test {
            val error = awaitItem()
            assertThat(error).isInstanceOf(TimerError.SetTime::class.java)
            assertThat((error as TimerError.SetTime).code).isEqualTo(SetTimeError.InvalidTime)
        }
    }

    @Test
    fun `타이머 시작 시 틱과 완료에 따라 상태가 업데이트된다`() = runTimerTestWithCleanup {
        // Given
        val initialDuration = 2.seconds
        val tickFlow = flow {
            emit(TimerEvent.Tick(1.seconds))
            emit(TimerEvent.Completed)
        }
        every { timerControlUseCase.validateTimerTime(any()) } returns TimerValidationResult.Valid
        every { timerControlUseCase.startTimer(any(), any()) } returns tickFlow

        // When
        timerManager.setTime(initialDuration)
        timerManager.start(initialDuration, initialDuration)

        // Then
        timerManager.timerState.test {
            assertThat(awaitItem().status).isInstanceOf(TimerStatus.Idle::class.java) // Initial state

            val runningState = awaitItem()
            assertThat(runningState.status).isInstanceOf(TimerStatus.Running::class.java)
            assertThat(runningState.remainingTime).isEqualTo(1.seconds)
            assertThat(runningState.initialDuration).isEqualTo(initialDuration)

            val completedState = awaitItem()
            assertThat(completedState.status).isInstanceOf(TimerStatus.Completed::class.java)
            assertThat(completedState.remainingTime).isEqualTo(Duration.ZERO)

            // Overtime tracking starts
            val overtimeState = awaitItem()
            assertThat(overtimeState.status).isInstanceOf(TimerStatus.Running::class.java)
            assertThat(overtimeState.overtime).isEqualTo(1.seconds)

            timerManager.stop(initialDuration)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `타이머 일시정지 및 재개가 올바르게 동작한다`() = runTimerTestWithCleanup {
        // Given
        val initialDuration = 5.minutes
        val remainingTime = 4.minutes

        // Use a flow that doesn't complete to test pause
        val tickFlow = flowOf(TimerEvent.Tick(remainingTime))
        every { timerControlUseCase.startTimer(any(), any()) } returns tickFlow

        timerManager.start(initialDuration, initialDuration)

        timerManager.timerState.test {
            awaitItem() // initial
            assertThat(awaitItem().status).isInstanceOf(TimerStatus.Running::class.java)

            // When
            timerManager.pause()

            // Then
            assertThat(awaitItem().status).isInstanceOf(TimerStatus.Paused::class.java)

            // When
            timerManager.resume()

            // Then
            val resumedState = awaitItem()
            assertThat(resumedState.status).isInstanceOf(TimerStatus.Running::class.java)
            assertThat(resumedState.remainingTime).isEqualTo(remainingTime)
        }
        verify(exactly = 2) { timerControlUseCase.startTimer(any(), any()) }
    }

    @Test
    fun `타이머 중지 시 상태가 유휴 상태로 리셋된다`() = runTimerTestWithCleanup {
        // Given
        val initialDuration = 5.minutes
        val tickFlow = flowOf(TimerEvent.Tick(4.minutes))
        every { timerControlUseCase.startTimer(any(), any()) } returns tickFlow
        timerManager.start(initialDuration, initialDuration)

        timerManager.timerState.test {
            awaitItem() // initial
            assertThat(awaitItem().status).isInstanceOf(TimerStatus.Running::class.java)

            // When
            timerManager.stop(initialDuration)

            // Then
            val stoppedState = awaitItem()
            assertThat(stoppedState.status).isInstanceOf(TimerStatus.Idle::class.java)
            assertThat(stoppedState.initialDuration).isEqualTo(initialDuration)
            assertThat(stoppedState.remainingTime).isEqualTo(initialDuration)
            assertThat(stoppedState.overtime).isEqualTo(Duration.ZERO)
        }
    }

    @Test
    fun `리마인더와 함께 타이머 시작 시 상태가 업데이트된다`() = runTimerTestWithCleanup {
        // Given
        val initialDuration = 10.seconds
        val reminderTime = 5.seconds
        val reminderThresholds = listOf(reminderTime)
        val tickFlow = flow {
            emit(TimerEvent.Tick(6.seconds))
            emit(TimerEvent.Reminder(reminderTime))
            emit(TimerEvent.Tick(4.seconds))
        }
        every { timerControlUseCase.startTimer(any(), any()) } returns tickFlow

        // When
        timerManager.start(initialDuration, initialDuration, reminderThresholds)

        // Then
        timerManager.timerState.test {
            awaitItem() // initial
            assertThat(awaitItem().status).isInstanceOf(TimerStatus.Running::class.java)

            val reminderState = awaitItem()
            assertThat(reminderState.status).isInstanceOf(TimerStatus.Running::class.java)
            assertThat(reminderState.isShowReminder).isTrue()
            assertThat(reminderState.remainingTime).isEqualTo(reminderTime)

            assertThat(awaitItem().status).isInstanceOf(TimerStatus.Running::class.java)
        }
    }

    @Test
    fun `타이머 에러 발생 시 에러를 방출하고 타이머를 중지시킨다`() = runTimerTestWithCleanup {
        // Given
        val initialDuration = 5.minutes
        val errorMessage = "Timer failed"
        val errorFlow = flow<TimerEvent> { throw RuntimeException(errorMessage) }
        coEvery { timerControlUseCase.startTimer(any(), any()) } returns errorFlow

        // When
        timerManager.start(initialDuration, initialDuration)

        // Then
        timerManager.timerError.test {
            val error = awaitItem()
            assertThat(error).isInstanceOf(TimerError.Run::class.java)
            assertThat((error as TimerError.Run).message).isEqualTo(errorMessage)
        }

        timerManager.timerState.test {
            val lastState = expectMostRecentItem()
            assertThat(lastState.status).isInstanceOf(TimerStatus.Idle::class.java)
        }
    }
}
