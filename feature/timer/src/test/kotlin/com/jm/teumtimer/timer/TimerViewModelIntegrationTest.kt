package com.jm.teumtimer.timer

import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jm.teumtimer.testing.rule.MainDispatcherExtension
import com.jm.teumtimer.timer.model.HapticPattern
import com.jm.teumtimer.timer.model.SetTimeError
import com.jm.teumtimer.timer.model.TimerError
import com.jm.teumtimer.timer.model.TimerIntent
import com.jm.teumtimer.timer.model.TimerSideEffect
import com.jm.teumtimer.timer.model.TimerUiState
import com.jm.teumtimer.timer.usecase.TimerState
import com.jm.teumtimer.timer.usecase.TimerStatus
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * TimerViewModel 테스트
 */
@DisplayName("TimerViewModel 통합 테스트")
class TimerViewModelIntegrationTest {

    companion object {
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension(StandardTestDispatcher())
    }


    private fun runTimerTestWithCleanup(
        testBody: suspend TimerRobot.() -> Unit
    ) = runTest {
        val robot = TimerRobot(this)
        try {
            testBody(robot)
        } finally {
            robot.viewModel.onIntent(TimerIntent.Stop)
        }
    }

    @Nested
    @DisplayName("초기 상태")
    inner class InitialState {

        @Test
        fun `초기 상태가 올바르게 설정되는지 확인`() = runTimerTestWithCleanup {
            // Then: 초기값 검증
            verifyState {
                assertThat(remainingTime).isEqualTo(0.seconds)
                assertThat(progress).isEqualTo(0f)
                assertThat(isRunning).isFalse()
                assertThat(isPaused).isFalse()
                assertThat(isCompleted).isFalse()
                assertThat(overtime).isEqualTo(0.seconds)
            }
        }
    }

    @Nested
    @DisplayName("시간 설정")
    inner class TimeSettings {

        @Test
        fun `시간 설정 시 상태가 올바르게 업데이트되는지 확인`() = runTimerTestWithCleanup {
            // When
            setTime(duration = 60.seconds)

            // Then
            verifyState {
                assertThat(remainingTime).isEqualTo(60.seconds)
                assertThat(progress).isGreaterThan(0f)
            }
        }

        @Test
        fun `최대 시간(60분) 설정 시 progress가 1이 되는지 확인`() = runTimerTestWithCleanup {
            // When
            setTime(duration = 60.minutes)

            // Then
            verifyState {
                assertThat(remainingTime).isEqualTo(60.minutes)
                assertThat(progress).isEqualTo(1f)
            }
        }

        @Test
        fun `0 이하의 시간 설정 시 에러 SideEffect가 발생하는지 확인`() = runTimerTestWithCleanup {
            // When
            setTime(duration = 0.seconds)

            // Then
            verifySideEffect { sideEffect ->
                assertThat(sideEffect).isInstanceOf(TimerSideEffect.ShowError::class.java)
                assertThat(
                    (sideEffect as TimerSideEffect.ShowError).errorType
                ).isInstanceOf(TimerError.SetTime::class.java)
                assertThat(
                    (sideEffect.errorType as TimerError.SetTime).code
                ).isEqualTo(SetTimeError.InvalidTime)
            }
        }

        @Nested
        @DisplayName("최대 시간 제한")
        inner class MaxTimeConstraints {

            @Test
            fun `MAX_TIME 상수가 60분인지 확인`() {
                // When
                val maxTime = TimerViewModel.MAX_TIME

                // Then
                assertThat(maxTime).isEqualTo(60.minutes)
            }

            @Test
            fun `60분 초과 시간 설정 시 정상 동작하는지 확인`() = runTimerTestWithCleanup {
                // Given: 실제 TimerManager를 사용하는 ViewModel
                val overMaxTime = 70.minutes

                // When
                setTime(duration = overMaxTime)

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(overMaxTime)
                    assertThat(progress).isAtLeast(1.0f)
                }
            }

            @Test
            fun `SetTime으로 59분 59초 설정 시 progress 계산 확인`() = runTimerTestWithCleanup {
                // Given:
                val almostMaxTime = 59.minutes + 59.seconds

                // When
                setTime(almostMaxTime)

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(almostMaxTime)
                    assertThat(progress).isLessThan(1.0f)
                    assertThat(progress).isGreaterThan(0.99f)
                }
            }

            @Test
            fun `타이머 실행 중 progress 계산이 MAX_TIME 기준으로 되는지 확인`() = runTimerTestWithCleanup {
                // Given: 30분 설정
                setTime(30.minutes)

                // When: Running 상태로 변경
                startAndAdvance(5.seconds)

                // Then
                verifyState {
                    val expectedProgress = (30.minutes - 1.seconds).inWholeMilliseconds.toFloat() /
                            TimerViewModel.MAX_TIME.inWholeMilliseconds.toFloat()
                    assertThat(progress).isWithin(0.01f).of(expectedProgress)
                }
            }
        }
    }

    @Nested
    @DisplayName("타이머 동작")
    inner class TimerOperations {

        @Nested
        @DisplayName("타이머 시작")
        inner class Start {

            @Test
            fun `타이머 시작 시 TimerManager의 start가 호출되는지 확인`() = runTimerTestWithCleanup {
                // Given
                setTime(5.seconds)

                // When
                startAndAdvance(1.seconds)

                // Then
                verifyState {
                    assertThat(isRunning).isTrue()
                    assertThat(isPaused).isFalse()
                }
            }

            @Test
            fun `시간 미설정 상태에서 시작 시 에러가 발생하는지 확인`() = runTimerTestWithCleanup {
                // When
                viewModel.onIntent(TimerIntent.Start())

                // Then
                verifySideEffect { sideEffect ->
                    assertThat(
                        (sideEffect as TimerSideEffect.ShowError).errorType
                    ).isInstanceOf(TimerError.SetTime::class.java)
                    assertThat(
                        (sideEffect.errorType as TimerError.SetTime).code
                    ).isEqualTo(SetTimeError.InvalidTime)
                }
            }

            @Test
            fun `타이머 Tick 이벤트 시 상태가 업데이트되는지 확인`() = runTimerTestWithCleanup {
                setTime(5.seconds)
                startAndAdvance(0.seconds)

                viewModel.uiState.test {
                    // 초기 상태
                    val initialState = awaitItem()
                    assertThat(initialState.remainingTime).isEqualTo(5.seconds)

                    // When: Running 상태 변경 시뮬레이션
                    startAndAdvance(1.seconds)

                    val state1 = awaitItem()
                    assertThat(state1.remainingTime).isEqualTo(4.seconds)
                    assertThat(state1.isRunning).isTrue()

                    cancelAndIgnoreRemainingEvents()
                }
            }

            @Test
            fun `이미 실행 중인 타이머를 다시 시작해도 무시되는지 확인`() = runTimerTestWithCleanup {
                // Given: 타이머 실행 중
                setTime(5.seconds)
                startAndAdvance(2.seconds)

                // When: 다시 시작 시도
                startAndAdvance(1.seconds)

                // Then: start가 한 번만 호출됨 (두 번째는 무시됨)
                verifyState {
                    assertThat(remainingTime).isEqualTo(2.seconds)
                }
            }
        }

        @Nested
        @DisplayName("타이머 완료")
        inner class Completion {

            @Test
            fun `타이머 완료 시 상태가 올바르게 변경되는지 확인`() = runTimerTestWithCleanup {
                setTime(5.seconds)

                // When: Completed 상태로 변경
                startAndAdvance(5.seconds)

                // Then
                verifyState {
                    assertThat(isCompleted).isTrue()
                    assertThat(isRunning).isFalse()
                    assertThat(remainingTime).isEqualTo(0.seconds)
                }
            }

            @Test
            fun `타이머 완료 시 완료 SideEffect가 발생하는지 확인`() = runTimerTestWithCleanup {
                setTime(5.seconds)

                viewModel.sideEffect.test {
                    // When: Completed 상태로 변경
                    startAndAdvance(5.seconds)
                    val events =
                        cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<TimerSideEffect>>()
                            .map { it.value }

                    // Then
                    assertThat(events).containsExactly(
                        TimerSideEffect.HapticFeedback(HapticPattern.TICK),
                        TimerSideEffect.HapticFeedback(HapticPattern.TICK),
                        TimerSideEffect.HapticFeedback(HapticPattern.TICK),
                        TimerSideEffect.HapticFeedback(HapticPattern.TICK),
                        TimerSideEffect.HapticFeedback(HapticPattern.TICK),
                        TimerSideEffect.ShowTimerCompleted,
                        TimerSideEffect.HapticFeedback(HapticPattern.COMPLETED),
                    ).inOrder()

                    cancelAndIgnoreRemainingEvents()
                }
            }

            @Test
            fun `타이머 완료 후 초과 시간이 증가하는지 확인`() = runTimerTestWithCleanup {
                // Given: 완료 상태
                setTime(5.seconds)
                startAndAdvance(5.seconds)

                // When: overtime 증가
                advanceTimeBy(3.seconds)

                // Then
                verifyState {
                    assertThat(overtime).isEqualTo(3.seconds)
                    assertThat(isOvertime).isTrue()
                }
            }
        }

        @Nested
        @DisplayName("일시정지와 재개")
        inner class PauseAndResume {

            @Test
            fun `타이머 일시정지 시 TimerManager의 pause가 호출되는지 확인`() = runTimerTestWithCleanup {
                // Given: 타이머 실행 중
                setTime(5.seconds)
                startAndAdvance(2.seconds)

                // When: 일시정지
                pause()

                // Then
                verifyState {
                    assertThat(isPaused).isTrue()
                    assertThat(isRunning).isFalse()
                }
            }

            @Test
            fun `일시정지 후 재개 시 TimerManager의 resume이 호출되는지 확인`() = runTimerTestWithCleanup {
                // Given: 일시정지 상태
                setTime(10.seconds)
                startAndAdvance(5.seconds)
                pause()

                // When: 재개
                resume()

                // Then
                verifyState {
                    assertThat(isPaused).isFalse()
                    assertThat(isRunning).isTrue()
                }
            }

            @Test
            fun `일시정지 상태에서 Stop 호출 시 초기 설정 시간으로 돌아가는지 확인`() = runTimerTestWithCleanup {
                // Given: 일시정지 상태
                setTime(10.seconds)
                startAndAdvance(5.seconds)
                pause()

                // When: 정지
                stop()

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(10.seconds)
                    assertThat(isRunning).isFalse()
                    assertThat(isPaused).isFalse()
                    assertThat(isIdle).isTrue()
                }
            }

            @Test
            fun `정지 상태에서 일시정지 시도 시 무시되는지 확인`() = runTimerTestWithCleanup {
                // Given: 정지 상태
                setTime(5.seconds)

                // When: 일시정지 시도
                pause()

                // Then: pause가 호출되지 않음 (무시됨)
                verifyState {
                    assertThat(isPaused).isFalse()
                }
            }

            @Test
            fun `정지 상태에서 재개 시도 시 무시되는지 확인`() = runTimerTestWithCleanup {
                // Given: 정지 상태
                setTime(5.seconds)

                // When: 재개 시도
                resume()

                // Then: resume이 호출되지 않음 (무시됨)
                verifyState {
                    assertThat(isRunning).isFalse()
                }
            }
        }

        @Nested
        @DisplayName("타이머 정지")
        inner class Stop {

            @Test
            fun `타이머 정지 시 TimerManager의 stop이 호출되는지 확인`() = runTimerTestWithCleanup {
                // Given: 타이머 실행 중
                setTime(10.seconds)
                startAndAdvance(5.seconds)

                // When: 정지
                stop()

                // Then
                verifyState {
                    assertThat(isRunning).isFalse()
                }
            }
        }
    }

    @Nested
    @DisplayName("드래그 진행률 조정")
    inner class DragProgress {

        @Test
        fun `드래그로 시간 조정 시 상태가 업데이트되는지 확인`() = runTimerTestWithCleanup {
            // Given
            val progress = 0.5f // 30분

            // When
            drag(progress)

            // Then
            verifyState {
                assertThat(progress).isEqualTo(progress)
                assertThat(remainingTime).isEqualTo(30.minutes)
            }
        }

        @Test
        fun `타이머 실행 중 드래그 시도 시 무시되는지 확인`() = runTimerTestWithCleanup {
            // Given: 타이머 실행 중
            setTime(5.seconds)
            startAndAdvance(1.seconds)

            val stateBefore = viewModel.uiState.value.remainingTime

            // When: 드래그 시도
            drag(0.8f)

            // Then: 시간이 변경되지 않음
            verifyState {
                assertThat(remainingTime).isEqualTo(stateBefore)
            }
        }

        @Test
        fun `일시정지 상태에서 드래그 시도 시 무시되는지 확인`() = runTimerTestWithCleanup {
            // Given: 타이머 일시정지 상태
            setTime(10.seconds)
            startAndAdvance(1.seconds)
            pause()

            val stateBefore = viewModel.uiState.value.remainingTime

            // When: 드래그 시도
            drag(0.5f)

            // Then: 시간이 변경되지 않음
            verifyState {
                assertThat(remainingTime).isEqualTo(stateBefore)
            }
        }

        @Nested
        @DisplayName("경계값 테스트")
        inner class BoundaryValues {

            @Test
            fun `드래그 progress가 정확히 0일 때 0분으로 설정되는지 확인`() = runTimerTestWithCleanup {
                // When
                drag(0f)

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(0.seconds)
                    assertThat(progress).isEqualTo(0f)
                }
            }

            @Test
            fun `드래그 progress가 0_01f 미만일 때 0분으로 설정되는지 확인`() = runTimerTestWithCleanup {
                // When
                drag(0.009f)

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(0.seconds)
                }
            }

            @Test
            fun `드래그 progress가 1일 때 60분으로 설정되는지 확인`() = runTimerTestWithCleanup {
                // When
                drag(1f)

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(60.minutes)
                }
            }

            @Test
            fun `드래그 progress가 0_5f일 때 30분으로 설정되는지 확인`() = runTimerTestWithCleanup {
                // When
                drag(0.5f)

                // Then
                verifyState {
                    assertThat(remainingTime).isEqualTo(30.minutes)
                    assertThat(progress).isEqualTo(0.5f)
                }
            }
        }
    }

    @Nested
    @DisplayName("리마인더 및 햅틱 피드백")
    inner class ReminderAndHaptic {

        @Test
        fun `리마인더 이벤트 발생 시 SideEffect가 전달되는지 확인`() = runTimerTestWithCleanup {

            setTime(60.seconds)

            viewModel.sideEffect.test {
                // When: 30초 리마인더 발생하는 타이머
                start(listOf(30.seconds))

                // Then
                val reminderEffect = awaitItem()
                assertThat(reminderEffect).isInstanceOf(TimerSideEffect.ShowReminder::class.java)
                assertThat((reminderEffect as TimerSideEffect.ShowReminder).remainingTime)
                    .isEqualTo(30.seconds)

                val hapticEffect = awaitItem()
                assertThat(hapticEffect).isEqualTo(
                    TimerSideEffect.HapticFeedback(HapticPattern.REMINDER)
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `마지막 5초 동안 햅틱 피드백이 발생하는지 확인`() = runTimerTestWithCleanup {
            setTime(5.seconds)

            viewModel.sideEffect.test {
                // When: 5초부터 1초까지 Running 상태 변경
                startAndAdvance(5.seconds)

                // Then: 5개의 TICK 햅틱이 발생해야 함
                val events = cancelAndConsumeRemainingEvents()
                val tickHapticSize = events.filterIsInstance<Event.Item<TimerSideEffect>>()
                    .count { it.value == TimerSideEffect.HapticFeedback(HapticPattern.TICK) }

                assertThat(tickHapticSize).isEqualTo(5)
            }
        }
    }

    @Nested
    @DisplayName("UI 상태 계산 속성")
    inner class UiStateProperties {

        @Nested
        @DisplayName("isIdle 속성")
        inner class IsIdleProperty {

            @Test
            fun `초기 상태에서 true인지 확인`() = runTimerTestWithCleanup {
                // When

                // Then
                verifyState {
                    assertThat(isIdle).isTrue()
                    assertThat(isRunning).isFalse()
                    assertThat(isPaused).isFalse()
                    assertThat(isCompleted).isFalse()
                }
            }

            @Test
            fun `타이머 실행 중 false가 되는지 확인`() = runTimerTestWithCleanup {
                setTime(5.seconds)

                // When: Running 상태로 변경
                startAndAdvance(1.seconds)

                // Then
                verifyState {
                    assertThat(isIdle).isFalse()
                    assertThat(isRunning).isTrue()
                }
            }

            @Test
            fun `일시정지 중 false가 되는지 확인`() = runTimerTestWithCleanup {
                setTime(5.seconds)
                startAndAdvance(1.seconds)

                // When: Paused 상태로 변경
                pause()

                // Then
                verifyState {
                    assertThat(isIdle).isFalse()
                    assertThat(isPaused).isTrue()
                }
            }
        }

        @Nested
        @DisplayName("formattedTime 속성")
        inner class FormattedTimeProperty {

            @Test
            fun `MM_SS 형식으로 포맷팅되는지 확인`() = runTimerTestWithCleanup {
                // When
                setTime(125.seconds) // 2분 5초

                // Then
                verifyState {
                    assertThat(formattedTime).isEqualTo("02:05")
                }
            }

            @Test
            fun `10분 미만일 때 0으로 패딩되는지 확인`() = runTimerTestWithCleanup {
                // When
                setTime(65.seconds) // 1분 5초

                // Then
                verifyState {
                    assertThat(formattedTime).isEqualTo("01:05")
                }
            }

            @Test
            fun `0초일 때 올바르게 표시되는지 확인`() = runTimerTestWithCleanup {
                // When

                // Then
                verifyState {
                    assertThat(formattedTime).isEqualTo("00:00")
                }
            }

            @Test
            fun `60분일 때 올바르게 표시되는지 확인`() = runTimerTestWithCleanup {
                // When
                setTime(60.minutes)

                // Then
                verifyState {
                    assertThat(formattedTime).isEqualTo("01:00:00")
                }
            }
        }

        @Nested
        @DisplayName("progress 계산")
        inner class ProgressCalculation {

            @Test
            fun `다양한 시간 설정 시 부동소수점 오차 확인`() = runTimerTestWithCleanup {
                val times = listOf(
                    1.seconds, 1.minutes, 5.minutes, 10.minutes,
                    20.minutes, 30.minutes, 45.minutes, 60.minutes
                )

                times.forEach { time ->
                    // When
                    setTime(time)

                    // Then
                    verifyState {
                        val expectedProgress = time.inWholeMilliseconds.toFloat() /
                                TimerViewModel.MAX_TIME.inWholeMilliseconds.toFloat()
                        assertThat(progress).isWithin(0.0001f).of(expectedProgress)
                    }
                }
            }

            @Test
            fun `progress가 음수가 되지 않는지 확인`() = runTimerTestWithCleanup {
                setTime(5.seconds)

                // When: Completed 상태
                startAndAdvance(5.seconds)

                // Then
                verifyState {
                    assertThat(progress).isAtLeast(0f)
                }
            }

            @Test
            fun `SetTime과 DragProgress의 progress 계산 일관성 확인`() = runTimerTestWithCleanup {
                // Given: SetTime으로 30분 설정
                setTime(30.minutes)
                val setTimeProgress = viewModel.uiState.value.progress

                // When: DragProgress로 0.5 (30분) 설정
                drag(0.5f)
                val dragProgress = viewModel.uiState.value.progress

                // Then
                assertThat(dragProgress).isEqualTo(setTimeProgress)
            }
        }
    }

    @Nested
    @DisplayName("에러 처리")
    inner class ErrorHandling {

        @Test
        fun `TimerManager에서 에러 발생 시 SideEffect가 전달되는지 확인`() = runTest {
            // Given: 에러 발생이 되는 Mock TimerManager
            val errorMessage = "타이머 실행 중 오류"
            val timerError2 = MutableSharedFlow<TimerError>(replay = 1) // replay 추가로 emit된 이벤트를 캐싱

            val mockTimerManager = mockk<TimerManager> {
                every { setTime(any()) } just Runs
                every { start(any(), any(), any()) } just Runs
                every { cancelAll() } just Runs
                every { timerError } returns timerError2
                every { timerState } returns MutableStateFlow(
                    TimerState(
                        status = TimerStatus.Idle,
                        initialDuration = 5.seconds,
                        remainingTime = 5.seconds
                    )
                )
                every { stop(any()) } just Runs
            }

            val viewModel = TimerViewModel(
                getAllPresetsUseCase = mockk(relaxed = true),
                addPresetUseCase = mockk(),
                updatePresetUseCase = mockk(),
                deletePresetUseCase = mockk(),
                timerManager = mockTimerManager
            )

            try {
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                viewModel.sideEffect.test {
                    // When: sideEffect collector가 준비된 후 에러 발생
                    viewModel.onIntent(TimerIntent.Start())
                    runCurrent() // Intent 처리 대기

                    timerError2.emit(TimerError.Run(errorMessage))
                    runCurrent() // emit 처리 대기

                    // Then
                    val sideEffect = awaitItem()
                    assertThat(sideEffect).isInstanceOf(TimerSideEffect.ShowError::class.java)

                    val errorType = (sideEffect as TimerSideEffect.ShowError).errorType
                    assertThat(errorType).isInstanceOf(TimerError.Run::class.java)
                    assertThat((errorType as TimerError.Run).message).isEqualTo(errorMessage)

                    cancelAndIgnoreRemainingEvents()
                }
            } finally {
                viewModel.onIntent(TimerIntent.Stop)
            }
        }

        @Test
        fun `동시에 여러 에러를 발생시켜도 모두 전송되는지 확인`() = runTimerTestWithCleanup {
            viewModel.sideEffect.test {
                // When: TimerManager를 통해 발생하는 에러들을 연속으로 발생시킴
                setTime(0.seconds) // 에러 1: InvalidTime

                setTime(-5.seconds) // 에러 2: InvalidTime

                // Then: 3개의 에러가 모두 전송되었는지 확인
                val error1 = awaitItem()
                assertThat(error1).isInstanceOf(TimerSideEffect.ShowError::class.java)
                assertThat((error1 as TimerSideEffect.ShowError).errorType)
                    .isInstanceOf(TimerError.SetTime::class.java)

                val error2 = awaitItem()
                assertThat(error2).isInstanceOf(TimerSideEffect.ShowError::class.java)
                assertThat((error2 as TimerSideEffect.ShowError).errorType)
                    .isInstanceOf(TimerError.SetTime::class.java)
            }
        }
    }

    @Nested
    @DisplayName("초과 시간(overtime)")
    inner class Overtime {

        @Test
        fun `타이머 완료 후 overtime이 0초로 초기화되는지 확인`() = runTimerTestWithCleanup {
            setTime(3.seconds)

            // When: Completed 상태
            startAndAdvance(3.seconds)

            // Then
            verifyState {
                assertThat(overtime).isEqualTo(0.seconds)
            }
        }

        @Test
        fun `overtime이 1초 단위로 증가하는지 확인`() = runTimerTestWithCleanup {
            setTime(2.seconds)

            // When: Completed 상태에서 overtime 증가
            startAndAdvance(2.seconds)

            advanceTimeBy(1.seconds)
            val overtime1 = viewModel.uiState.value.overtime

            advanceTimeBy(1.seconds)
            val overtime2 = viewModel.uiState.value.overtime

            advanceTimeBy(1.seconds)
            val overtime3 = viewModel.uiState.value.overtime

            // Then
            assertThat(overtime1).isEqualTo(1.seconds)
            assertThat(overtime2).isEqualTo(2.seconds)
            assertThat(overtime3).isEqualTo(3.seconds)
        }

        @Test
        fun `Stop 호출 시 overtime이 0으로 초기화되는지 확인`() = runTimerTestWithCleanup {
            // Given: Completed 상태에서 overtime 증가
            setTime(3.seconds)
            startAndAdvance(3.seconds)
            advanceTimeBy(5.seconds)

            // When: Stop 호출
            stop()

            // Then
            verifyState {
                assertThat(overtime).isEqualTo(0.seconds)
            }
        }

        @Test
        fun `완료 전에는 overtime이 0인지 확인`() = runTimerTestWithCleanup {
            setTime(10.seconds)

            // When: Running 상태
            startAndAdvance(5.seconds)

            // Then
            verifyState {
                assertThat(overtime).isEqualTo(0.seconds)
                assertThat(isCompleted).isFalse()
            }
        }
    }

    @Nested
    @DisplayName("동시성 및 엣지 케이스")
    inner class ConcurrencyAndEdgeCases {

        @Test
        fun `Pause와 Resume을 빠르게 연속 호출해도 안전한지 확인`() = runTimerTestWithCleanup {
            // Given: 타이머 실행 중
            setTime(10.seconds)
            startAndAdvance(5.seconds)

            viewModel.uiState.test {
                // StateFlow는 구독 시점에 현재 값을 방출하므로 해당 이벤트를 소비
                awaitItem()

                // When: Pause와 Resume을 빠르게 반복
                pause()
                resume()
                pause()
                resume()
                pause()
                resume()

                // Then: 각 상태 변화가 올바르게 처리되었는지 확인
                val events =
                    cancelAndConsumeRemainingEvents().filterIsInstance<Event.Item<TimerUiState>>()
                        .map { it.value.isPaused }
                assertThat(events).containsExactly(
                    true, false,
                    true, false,
                    true, false
                ).inOrder()

                verifyState {
                    assertThat(isRunning).isTrue()
                    assertThat(isPaused).isFalse()
                }
            }
        }

        @Test
        fun `Stop을 빠르게 연속 호출해도 안전한지 확인`() = runTimerTestWithCleanup {
            // Given: 타이머 실행 중
            setTime(10.seconds)
            startAndAdvance(5.seconds)

            // When: Stop을 빠르게 여러 번 호출
            stop()
            stop()
            stop()

            // Then: 예외 없이 정상 처리되고 초기 상태로 돌아감
            verifyState {
                assertThat(isIdle).isTrue()
                assertThat(remainingTime).isEqualTo(10.seconds)
            }
        }

        @Test
        fun `Start와 Stop을 번갈아 빠르게 호출해도 안전한지 확인`() = runTimerTestWithCleanup {
            // Given
            setTime(10.seconds)

            // When: Start와 Stop을 빠르게 반복
            startAndAdvance(5.seconds)
            stop()

            verifyState {
                assertThat(isIdle).isTrue()
                assertThat(remainingTime).isEqualTo(10.seconds)
            }

            startAndAdvance(3.seconds)
            stop()

            // Then: 각 사이클이 정상 동작했는지 확인
            verifyState {
                assertThat(isIdle).isTrue()
                assertThat(remainingTime).isEqualTo(10.seconds)
            }
        }

        @Test
        fun `타이머 Job 취소 후 즉시 재시작해도 안전한지 확인`() = runTimerTestWithCleanup {
            // Given: 타이머 실행
            setTime(10.seconds)
            startAndAdvance(5.seconds)

            // When: 즉시 Stop 후 재시작
            stop()
            startAndAdvance(1.seconds)

            // Then: 정상 동작
            verifyState {
                assertThat(isRunning).isTrue()
                assertThat(remainingTime).isEqualTo(9.seconds)
            }
        }

        @Test
        fun `여러 타이머를 순차적으로 실행해도 안전한지 확인`() = runTimerTestWithCleanup {
            // 첫 번째 타이머
            setTime(3.seconds)
            startAndAdvance(3.seconds)
            stop()

            // 두 번째 타이머
            setTime(5.seconds)
            startAndAdvance(5.seconds)
            stop()

            // 세 번째 타이머
            setTime(2.seconds)
            startAndAdvance(1.seconds)

            // Then: 마지막 타이머가 정상 동작
            verifyState {
                assertThat(isRunning).isTrue()
                assertThat(remainingTime).isEqualTo(1.seconds)
            }
        }
    }
}