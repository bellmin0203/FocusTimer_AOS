package com.jm.focustimer.timer

import app.cash.turbine.Event
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.jm.focustimer.testing.rule.MainDispatcherExtension
import com.jm.focustimer.timer.model.HapticPattern
import com.jm.focustimer.timer.model.TimerEvent
import com.jm.focustimer.timer.model.TimerIntent
import com.jm.focustimer.timer.model.TimerSideEffect
import com.jm.focustimer.timer.model.TimerUiState
import com.jm.focustimer.timer.usecase.CountdownTimerUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@DisplayName("TimerViewModel 테스트")
class TimerViewModelTest {

    companion object {
        // JUnit5 Extension 등록 - 각 테스트마다 Main Dispatcher 설정
        @JvmField
        @RegisterExtension
        val mainDispatcherExtension = MainDispatcherExtension(StandardTestDispatcher())
    }

    private lateinit var viewModel: TimerViewModel
    private val countdownTimerUseCase: CountdownTimerUseCase = CountdownTimerUseCase()

    // Mock UseCase를 사용하는 ViewModel 생성 헬퍼 함수
    private fun createViewModelWithMockUseCase(flow: Flow<TimerEvent>): TimerViewModel {
        val useCase = mockk<CountdownTimerUseCase>()
        coEvery { useCase(any()) } returns flow
        return TimerViewModel(
            useCase,
            getAllPresetsUseCase = TODO(),
            addPresetUseCase = TODO(),
            updatePresetUseCase = TODO(),
            deletePresetUseCase = TODO()
        )
    }

    // 안전한 테스트 실행을 위한 래퍼 함수
    private fun runTimerSafeTest(
        testViewModel: TimerViewModel = this.viewModel,
        testBody: suspend TestScope.() -> Unit
    ) {
        runTest {
            try {
                testBody()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                testViewModel.onIntent(TimerIntent.Stop)
            }
        }
    }

    @BeforeEach
    fun setup() {
        this.viewModel = TimerViewModel(
            countdownTimerUseCase,
            getAllPresetsUseCase = TODO(),
            addPresetUseCase = TODO(),
            updatePresetUseCase = TODO(),
            deletePresetUseCase = TODO()
        )
    }

    @AfterEach
    fun tearDown() {
        viewModel.onIntent(TimerIntent.Stop)
    }

    @Nested
    @DisplayName("초기 상태")
    inner class InitialState {

        @Test
        fun `초기 상태가 올바르게 설정되는지 확인`() = runTimerSafeTest {
            // Given: ViewModel 생성

            // When: 초기 상태 확인
            val state = viewModel.uiState.value

            // Then: 초기값 검증
            assertThat(state.remainingTime).isEqualTo(0.seconds)
            assertThat(state.progress).isEqualTo(0f)
            assertThat(state.isRunning).isFalse()
            assertThat(state.isPaused).isFalse()
            assertThat(state.isCompleted).isFalse()
            assertThat(state.overtime).isEqualTo(0.seconds)
            assertThat(state.error).isNull()
        }
    }

    @Nested
    @DisplayName("시간 설정")
    inner class TimeSettings {

        @Test
        fun `시간 설정 시 상태가 올바르게 업데이트되는지 확인`() = runTimerSafeTest {
            // When
            viewModel.onIntent(TimerIntent.SetTime(60.seconds))

            // Then
            val state = viewModel.uiState.value
            assertThat(state.remainingTime).isEqualTo(60.seconds)
            assertThat(state.progress).isGreaterThan(0f)
            assertThat(state.error).isNull()
        }

        @Test
        fun `최대 시간(60분) 설정 시 progress가 1이 되는지 확인`() = runTimerSafeTest {
            // When
            viewModel.onIntent(TimerIntent.SetTime(60.minutes))

            // Then
            val state = viewModel.uiState.value
            assertThat(state.remainingTime).isEqualTo(60.minutes)
            assertThat(state.progress).isEqualTo(1f)
        }

        @Test
        fun `0 이하의 시간 설정 시 에러 SideEffect가 발생하는지 확인`() = runTimerSafeTest {
            viewModel.sideEffect.test {
                // When
                viewModel.onIntent(TimerIntent.SetTime(0.seconds))

                // Then
                val sideEffect = awaitItem()
                assertThat(sideEffect).isInstanceOf(TimerSideEffect.ShowError::class.java)
                assertThat((sideEffect as TimerSideEffect.ShowError).message)
                    .contains("0보다 커야")
            }
        }

        @Test
        fun `타이머 실행 중 시간 설정 시도 시 에러가 발생하는지 확인`() = runTimerSafeTest {
            viewModel.sideEffect.test {
                // Given: 타이머 실행 중
                viewModel.onIntent(TimerIntent.SetTime(10.seconds))
                viewModel.onIntent(TimerIntent.Start)

                // When: 실행 중에 시간 변경 시도
                viewModel.onIntent(TimerIntent.SetTime(20.seconds))

                // Then: 에러 발생
                val sideEffect = awaitItem()
                assertThat(sideEffect).isInstanceOf(TimerSideEffect.ShowError::class.java)
                assertThat((sideEffect as TimerSideEffect.ShowError).message)
                    .contains("실행 중에는 시간을 변경할 수 없습니다")

                // 정리: 타이머 정지
                viewModel.onIntent(TimerIntent.Stop)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Nested
        @DisplayName("최대 시간 제한")
        inner class MaxTimeConstraints {

            @Test
            fun `MAX_TIME 상수가 60분인지 확인`() = runTimerSafeTest {
                // When
                val maxTime = TimerViewModel.MAX_TIME

                // Then
                assertThat(maxTime).isEqualTo(60.minutes)
            }

            @Test
            fun `60분 초과 시간 설정 시 정상 동작하는지 확인`() = runTimerSafeTest {
                // Given
                val overMaxTime = 70.minutes

                // When
                viewModel.onIntent(TimerIntent.SetTime(overMaxTime))

                // Then: 70분도 설정은 가능하지만 progress는 1.0을 초과할 수 있음
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(overMaxTime)
                assertThat(state.progress).isAtLeast(1.0f)
            }

            @Test
            fun `SetTime으로 59분 59초 설정 시 progress 계산 확인`() = runTimerSafeTest {
                // Given
                val almostMaxTime = 59.minutes + 59.seconds

                // When
                viewModel.onIntent(TimerIntent.SetTime(almostMaxTime))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(almostMaxTime)
                assertThat(state.progress).isLessThan(1.0f)
                assertThat(state.progress).isGreaterThan(0.99f)
            }

            @Test
            fun `타이머 실행 중 progress 계산이 MAX_TIME 기준으로 되는지 확인`() = runTimerSafeTest {
                // Given: 30분 설정
                viewModel.onIntent(TimerIntent.SetTime(30.minutes))

                // When
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(1.seconds)
                runCurrent()

                // Then: progress는 MAX_TIME(60분) 기준으로 계산
                val state = viewModel.uiState.value
                val expectedProgress = (30.minutes - 1.seconds).inWholeMilliseconds.toFloat() /
                        TimerViewModel.MAX_TIME.inWholeMilliseconds.toFloat()
                assertThat(state.progress).isWithin(0.01f).of(expectedProgress)
            }

            @Test
            fun `매우 긴 시간 120분 설정 시 정상 동작하는지 확인`() = runTimerSafeTest {
                // Given
                val longTime = 120.minutes

                // When
                viewModel.onIntent(TimerIntent.SetTime(longTime))

                // Then: 설정은 가능
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(longTime)
                assertThat(state.progress).isAtLeast(1.0f)
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
            fun `타이머 시작 시 isRunning이 true가 되는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                // When
                viewModel.onIntent(TimerIntent.Start)

                // Then
                assertThat(viewModel.uiState.value.isRunning).isTrue()
                assertThat(viewModel.uiState.value.isPaused).isFalse()
            }

            @Test
            fun `시간 미설정 상태에서 시작 시 에러가 발생하는지 확인`() = runTimerSafeTest {
                viewModel.sideEffect.test {
                    // When
                    viewModel.onIntent(TimerIntent.Start)

                    // Then
                    val sideEffect = awaitItem()
                    assertThat(sideEffect).isInstanceOf(TimerSideEffect.ShowError::class.java)
                    assertThat((sideEffect as TimerSideEffect.ShowError).message)
                        .contains("시간을 설정해주세요")
                }
            }

            @Test
            fun `타이머 Tick 이벤트 시 남은 시간이 업데이트되는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                viewModel.uiState.test {
                    // 초기 상태
                    assertThat(awaitItem().remainingTime).isEqualTo(5.seconds)

                    // When
                    viewModel.onIntent(TimerIntent.Start)
                    advanceTimeBy(5.seconds)
                    runCurrent()

                    // Then
                    try {
                        val events = cancelAndConsumeRemainingEvents()
                        val timeItems = events.filterIsInstance<Event.Item<TimerUiState>>()
                            .map { it.value.remainingTime }
                        assertThat(timeItems).containsExactly(
                            5.seconds,
                            4.seconds,
                            3.seconds,
                            2.seconds,
                            1.seconds,
                            0.seconds
                        )
                    } finally {
                        viewModel.onIntent(TimerIntent.Stop)
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            @Test
            fun `이미 실행 중인 타이머를 다시 시작해도 무시되는지 확인`() = runTimerSafeTest {
                // Given: 타이머 실행 중
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))
                viewModel.onIntent(TimerIntent.Start)

                val stateAfterFirstStart = viewModel.uiState.value

                // When: 다시 시작 시도
                viewModel.onIntent(TimerIntent.Start)

                // Then: 상태 변화 없음
                assertThat(viewModel.uiState.value).isEqualTo(stateAfterFirstStart)
            }
        }

        @Nested
        @DisplayName("타이머 완료")
        inner class Completion {

            @Test
            fun `타이머 완료 시 상태가 올바르게 변경되는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                // When
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(5.seconds)
                runCurrent()

                // Then
                val state = viewModel.uiState.value
                assertThat(state.isCompleted).isTrue()
                assertThat(state.isRunning).isFalse()
                assertThat(state.remainingTime).isEqualTo(0.seconds)
                assertThat(state.progress).isEqualTo(0f)
            }

            @Test
            fun `타이머 완료 시 완료 SideEffect가 발생하는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                viewModel.sideEffect.test {
                    // When
                    viewModel.onIntent(TimerIntent.Start)
                    advanceTimeBy(5.seconds)
                    runCurrent()

                    // Then
                    val events = cancelAndConsumeRemainingEvents()

                    val tickHaptics = events.count {
                        it is Event.Item && it.value == TimerSideEffect.HapticFeedback(HapticPattern.TICK)
                    }
                    assertThat(tickHaptics).isEqualTo(5)

                    assertThat(events).contains(Event.Item(TimerSideEffect.ShowTimerCompleted))
                    assertThat(events).contains(
                        Event.Item(TimerSideEffect.HapticFeedback(HapticPattern.COMPLETED))
                    )

                    viewModel.onIntent(TimerIntent.Stop)
                    cancelAndIgnoreRemainingEvents()
                }
            }

            @Test
            fun `타이머 완료 후 초과 시간이 증가하는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(5.seconds)

                // When
                advanceTimeBy(100.milliseconds)

                // Then
                val overtimeSeconds = viewModel.uiState.value.overtime

                viewModel.onIntent(TimerIntent.Stop)

                assertThat(overtimeSeconds).isAtLeast(0.seconds)
            }

            @Test
            fun `Completed 이벤트 발생 시 완료 SideEffect가 전송되는지 확인`() {
                // Given
                val timerFlow = flow {
                    emit(TimerEvent.Completed)
                }

                val testViewModel = createViewModelWithMockUseCase(timerFlow)
                runTimerSafeTest(testViewModel) {
                    testViewModel.onIntent(TimerIntent.SetTime(10.seconds))

                    testViewModel.sideEffect.test {
                        // When
                        testViewModel.onIntent(TimerIntent.Start)
                        advanceTimeBy(1.seconds)
                        runCurrent()

                        // Then
                        val events = cancelAndConsumeRemainingEvents()

                        assertThat(events).contains(Event.Item(TimerSideEffect.ShowTimerCompleted))
                        assertThat(events).contains(
                            Event.Item(TimerSideEffect.HapticFeedback(HapticPattern.COMPLETED))
                        )
                    }
                }
            }

            @Test
            fun `Completed 이벤트 후 overtime 추적이 시작되는지 확인`() {
                // Given
                val timerFlow = flow {
                    emit(TimerEvent.Completed)
                }

                val testViewModel = createViewModelWithMockUseCase(timerFlow)
                runTimerSafeTest(testViewModel) {
                    testViewModel.onIntent(TimerIntent.SetTime(5.seconds))

                    // When
                    testViewModel.onIntent(TimerIntent.Start)
                    advanceTimeBy(1.seconds)
                    runCurrent()

                    advanceTimeBy(3.seconds)
                    runCurrent()

                    // Then
                    val state = testViewModel.uiState.value
                    assertThat(state.isCompleted).isTrue()
                    assertThat(state.overtime).isAtLeast(3.seconds)
                }
            }
        }

        @Nested
        @DisplayName("일시정지와 재개")
        inner class PauseAndResume {

            @Test
            fun `타이머 일시정지 시 상태가 올바르게 변경되는지 확인`() = runTimerSafeTest {
                // Given: 타이머 실행 중
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))
                viewModel.onIntent(TimerIntent.Start)

                // When: 일시정지
                viewModel.onIntent(TimerIntent.Pause)

                // Then
                val state = viewModel.uiState.value
                assertThat(state.isRunning).isFalse()
                assertThat(state.isPaused).isTrue()
            }

            @Test
            fun `일시정지 후 재개 시 타이머가 다시 시작되는지 확인`() = runTimerSafeTest {
                // Given: 일시정지 상태
                viewModel.onIntent(TimerIntent.SetTime(10.seconds))
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(3.seconds)
                runCurrent()

                viewModel.onIntent(TimerIntent.Pause)
                val remainingTimeWhenPaused = viewModel.uiState.value.remainingTime

                // When: 재개
                viewModel.onIntent(TimerIntent.Resume)
                advanceTimeBy(2.seconds)
                runCurrent()

                // Then
                val state = viewModel.uiState.value
                assertThat(state.isRunning).isTrue()
                assertThat(state.isPaused).isFalse()
                assertThat(state.remainingTime).isEqualTo(remainingTimeWhenPaused - 2.seconds)
            }

            @Test
            fun `일시정지 중 남은 시간이 유지되는지 확인`() = runTimerSafeTest {
                // Given: 타이머 실행 중
                viewModel.onIntent(TimerIntent.SetTime(10.seconds))
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(3.seconds)
                runCurrent()

                // When: 일시정지
                viewModel.onIntent(TimerIntent.Pause)
                val remainingTimeWhenPaused = viewModel.uiState.value.remainingTime

                // Then
                advanceTimeBy(5.seconds)
                runCurrent()

                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(remainingTimeWhenPaused)
                assertThat(state.isPaused).isTrue()
                assertThat(state.isRunning).isFalse()
            }

            @Test
            fun `일시정지 상태에서 Stop 호출 시 초기 설정 시간으로 돌아가는지 확인`() = runTimerSafeTest {
                // Given: 일시정지 상태
                viewModel.onIntent(TimerIntent.SetTime(10.seconds))
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(3.seconds)
                runCurrent()
                viewModel.onIntent(TimerIntent.Pause)

                // When: 정지
                viewModel.onIntent(TimerIntent.Stop)

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(10.seconds)
                assertThat(state.isRunning).isFalse()
                assertThat(state.isPaused).isFalse()
                assertThat(state.isIdle).isTrue()
            }

            @Test
            fun `일시정지 중 SetTime 시도 시 에러가 발생하는지 확인`() = runTimerSafeTest {
                viewModel.sideEffect.test {
                    // Given: 일시정지 상태
                    viewModel.onIntent(TimerIntent.SetTime(10.seconds))
                    viewModel.onIntent(TimerIntent.Start)
                    viewModel.onIntent(TimerIntent.Pause)

                    // When: 시간 변경 시도
                    viewModel.onIntent(TimerIntent.SetTime(20.seconds))

                    // Then
                    val sideEffect = awaitItem()
                    assertThat(sideEffect).isInstanceOf(TimerSideEffect.ShowError::class.java)
                    assertThat((sideEffect as TimerSideEffect.ShowError).message)
                        .contains("실행 중에는 시간을 변경할 수 없습니다")
                }
            }

            @Test
            fun `정지 상태에서 일시정지 시도 시 무시되는지 확인`() = runTimerSafeTest {
                // Given: 정지 상태
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                // When: 일시정지 시도
                viewModel.onIntent(TimerIntent.Pause)

                // Then: 상태 변화 없음
                assertThat(viewModel.uiState.value.isPaused).isFalse()
                assertThat(viewModel.uiState.value.isRunning).isFalse()
            }

            @Test
            fun `정지 상태에서 재개 시도 시 무시되는지 확인`() = runTimerSafeTest {
                // Given: 정지 상태
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                // When: 재개 시도
                viewModel.onIntent(TimerIntent.Resume)

                // Then: 상태 변화 없음
                assertThat(viewModel.uiState.value.isRunning).isFalse()
            }

            @Test
            fun `일시정지와 재개를 반복해도 정상 동작하는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(20.seconds))
                viewModel.onIntent(TimerIntent.Start)

                // When & Then: 일시정지 -> 재개 반복
                advanceTimeBy(2.seconds)
                runCurrent()
                viewModel.onIntent(TimerIntent.Pause)
                val time1 = viewModel.uiState.value.remainingTime
                assertThat(time1).isEqualTo(18.seconds)
                assertThat(viewModel.uiState.value.isPaused).isTrue()

                viewModel.onIntent(TimerIntent.Resume)
                advanceTimeBy(3.seconds)
                runCurrent()
                viewModel.onIntent(TimerIntent.Pause)
                val time2 = viewModel.uiState.value.remainingTime
                assertThat(time2).isEqualTo(15.seconds)
                assertThat(viewModel.uiState.value.isPaused).isTrue()

                viewModel.onIntent(TimerIntent.Resume)
                advanceTimeBy(5.seconds)
                runCurrent()
                val time3 = viewModel.uiState.value.remainingTime
                assertThat(time3).isEqualTo(10.seconds)
                assertThat(viewModel.uiState.value.isRunning).isTrue()
            }
        }

        @Nested
        @DisplayName("타이머 정지")
        inner class Stop {

            @Test
            fun `타이머 정지 시 초기 상태로 돌아가는지 확인`() = runTimerSafeTest {
                // Given: 타이머 실행 중
                viewModel.onIntent(TimerIntent.SetTime(10.seconds))
                viewModel.onIntent(TimerIntent.Start)

                // When: 정지
                viewModel.onIntent(TimerIntent.Stop)

                // Then
                val state = viewModel.uiState.value
                assertThat(state.isRunning).isFalse()
                assertThat(state.isPaused).isFalse()
                assertThat(state.isCompleted).isFalse()
                assertThat(state.remainingTime).isEqualTo(10.seconds)
                assertThat(state.overtime).isEqualTo(0.seconds)
            }

            @Test
            fun `타이머 Job이 null일 때 Stop을 호출해도 안전한지 확인`() = runTest {
                // Given: 타이머를 시작하지 않은 상태
                val testViewModel = TimerViewModel(
                    countdownTimerUseCase,
                    getAllPresetsUseCase = TODO(),
                    addPresetUseCase = TODO(),
                    updatePresetUseCase = TODO(),
                    deletePresetUseCase = TODO()
                )
                testViewModel.onIntent(TimerIntent.SetTime(10.seconds))

                // When: Stop 호출
                try {
                    testViewModel.onIntent(TimerIntent.Stop)

                    // Then
                    val state = testViewModel.uiState.value
                    assertThat(state.isRunning).isFalse()
                } catch (e: Exception) {
                    throw AssertionError("Job이 null일 때 Stop 호출 중 예외 발생: ${e.message}", e)
                }
            }
        }
    }

    @Nested
    @DisplayName("드래그 진행률 조정")
    inner class DragProgress {

        @Test
        fun `드래그로 시간 조정 시 상태가 업데이트되는지 확인`() = runTimerSafeTest {
            // Given
            val progress = 0.5f // 30분

            // When
            viewModel.onIntent(TimerIntent.DragProgress(progress))

            // Then
            val state = viewModel.uiState.value
            assertThat(state.progress).isEqualTo(progress)
            assertThat(state.remainingTime).isEqualTo(30.minutes)
        }

        @Test
        fun `타이머 실행 중 드래그 시도 시 무시되는지 확인`() = runTimerSafeTest {
            // Given: 타이머 실행 중
            viewModel.onIntent(TimerIntent.SetTime(5.seconds))
            viewModel.onIntent(TimerIntent.Start)

            val stateBefore = viewModel.uiState.value.remainingTime

            // When: 드래그 시도
            viewModel.onIntent(TimerIntent.DragProgress(0.8f))

            // Then: 시간이 변경되지 않음
            assertThat(viewModel.uiState.value.remainingTime).isEqualTo(stateBefore)
        }

        @Test
        fun `일시정지 상태에서 드래그 시도 시 무시되는지 확인`() = runTimerSafeTest {
            // Given: 타이머 일시정지 상태
            viewModel.onIntent(TimerIntent.SetTime(10.seconds))
            viewModel.onIntent(TimerIntent.Start)
            viewModel.onIntent(TimerIntent.Pause)

            val stateBefore = viewModel.uiState.value.remainingTime

            // When: 드래그 시도
            viewModel.onIntent(TimerIntent.DragProgress(0.5f))

            // Then: 시간이 변경되지 않음
            assertThat(viewModel.uiState.value.remainingTime).isEqualTo(stateBefore)
        }

        @Nested
        @DisplayName("경계값 테스트")
        inner class BoundaryValues {

            @Test
            fun `드래그 progress가 정확히 0일 때 0분으로 설정되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.DragProgress(0f))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(0.seconds)
                assertThat(state.progress).isEqualTo(0f)
            }

            @Test
            fun `드래그 progress가 0_01f 미만일 때 0분으로 설정되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.DragProgress(0.009f))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(0.seconds)
            }

            @Test
            fun `드래그 progress가 1일 때 60분으로 설정되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.DragProgress(1f))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(60.minutes)
            }

            @Test
            fun `드래그 progress가 1_0f를 초과할 때 최대 60분으로 제한되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.DragProgress(1.5f))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isAtMost(60.minutes)
            }

            @Test
            fun `드래그 progress가 음수일 때 0분으로 설정되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.DragProgress(-0.1f))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(0.seconds)
            }

            @Test
            fun `드래그 progress가 0_5f일 때 30분으로 설정되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.DragProgress(0.5f))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.remainingTime).isEqualTo(30.minutes)
                assertThat(state.progress).isEqualTo(0.5f)
            }

            @Test
            fun `드래그 progress로 연속적인 값 변경 테스트`() = runTimerSafeTest {
                // When & Then
                viewModel.onIntent(TimerIntent.DragProgress(0f))
                assertThat(viewModel.uiState.value.remainingTime).isEqualTo(0.seconds)

                viewModel.onIntent(TimerIntent.DragProgress(0.5f))
                assertThat(viewModel.uiState.value.remainingTime).isEqualTo(30.minutes)

                viewModel.onIntent(TimerIntent.DragProgress(1.0f))
                assertThat(viewModel.uiState.value.remainingTime).isEqualTo(60.minutes)
            }
        }
    }

    @Nested
    @DisplayName("리마인더 및 햅틱 피드백")
    inner class ReminderAndHaptic {

        @Test
        fun `리마인더 이벤트 발생 시 SideEffect가 전달되는지 확인`() = runTimerSafeTest {
            // Given
            val timerFlow = flow {
                for (i in 60 downTo 31) {
                    emit(TimerEvent.Tick(i.seconds))
                    delay(1.seconds)
                }
                emit(TimerEvent.Reminder(30.seconds))
                emit(TimerEvent.Tick(30.seconds))
            }

            val testViewModel = createViewModelWithMockUseCase(timerFlow)
            testViewModel.onIntent(TimerIntent.SetTime(60.seconds))

            testViewModel.sideEffect.test {
                // When
                testViewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(30.seconds)
                runCurrent()

                // Then
                val reminderEffect = awaitItem()
                assertThat(reminderEffect).isInstanceOf(TimerSideEffect.ShowReminder::class.java)
                assertThat((reminderEffect as TimerSideEffect.ShowReminder).remainingTime)
                    .isEqualTo(30.seconds)
                runCurrent()

                val hapticEffect = awaitItem()
                assertThat(hapticEffect).isEqualTo(
                    TimerSideEffect.HapticFeedback(HapticPattern.REMINDER)
                )

                testViewModel.onIntent(TimerIntent.Stop)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `마지막 5초 동안 햅틱 피드백이 발생하는지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(5.seconds))

            viewModel.sideEffect.test {
                // When
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(5.seconds)
                runCurrent()

                // Then
                val events = cancelAndConsumeRemainingEvents()

                val tickHaptics = events.count {
                    it is Event.Item && it.value == TimerSideEffect.HapticFeedback(HapticPattern.TICK)
                }
                assertThat(tickHaptics).isEqualTo(5)

                assertThat(events).contains(Event.Item(TimerSideEffect.ShowTimerCompleted))
                assertThat(events).contains(
                    Event.Item(TimerSideEffect.HapticFeedback(HapticPattern.COMPLETED))
                )

                viewModel.onIntent(TimerIntent.Stop)
            }
        }

        @Test
        fun `여러 SideEffect가 순서대로 전송되는지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(5.seconds))

            viewModel.sideEffect.test {
                // When
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(5.seconds)
                runCurrent()

                // Then
                val events = cancelAndConsumeRemainingEvents()
                val sideEffects = events
                    .filterIsInstance<Event.Item<TimerSideEffect>>()
                    .map { it.value }

                val tickCount = sideEffects.count {
                    it is TimerSideEffect.HapticFeedback && it.pattern == HapticPattern.TICK
                }
                assertThat(tickCount).isEqualTo(5)

                assertThat(sideEffects).contains(TimerSideEffect.ShowTimerCompleted)
                assertThat(sideEffects).contains(
                    TimerSideEffect.HapticFeedback(HapticPattern.COMPLETED)
                )
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
            fun `초기 상태에서 true인지 확인`() = runTimerSafeTest {
                // When
                val state = viewModel.uiState.value

                // Then
                assertThat(state.isIdle).isTrue()
                assertThat(state.isRunning).isFalse()
                assertThat(state.isPaused).isFalse()
                assertThat(state.isCompleted).isFalse()
            }

            @Test
            fun `타이머 실행 중 false가 되는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                // When
                viewModel.onIntent(TimerIntent.Start)

                // Then
                val state = viewModel.uiState.value
                assertThat(state.isIdle).isFalse()
                assertThat(state.isRunning).isTrue()
            }

            @Test
            fun `일시정지 중 false가 되는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))
                viewModel.onIntent(TimerIntent.Start)

                // When
                viewModel.onIntent(TimerIntent.Pause)

                // Then
                val state = viewModel.uiState.value
                assertThat(state.isIdle).isFalse()
                assertThat(state.isPaused).isTrue()
            }
        }

        @Nested
        @DisplayName("formattedTime 속성")
        inner class FormattedTimeProperty {

            @Test
            fun `MM_SS 형식으로 포맷팅되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.SetTime(125.seconds)) // 2분 5초

                // Then
                val state = viewModel.uiState.value
                assertThat(state.formattedTime).isEqualTo("02:05")
            }

            @Test
            fun `10분 미만일 때 0으로 패딩되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.SetTime(65.seconds)) // 1분 5초

                // Then
                val state = viewModel.uiState.value
                assertThat(state.formattedTime).isEqualTo("01:05")
            }

            @Test
            fun `0초일 때 올바르게 표시되는지 확인`() = runTimerSafeTest {
                // When
                val state = viewModel.uiState.value

                // Then
                assertThat(state.formattedTime).isEqualTo("00:00")
            }

            @Test
            fun `60분일 때 올바르게 표시되는지 확인`() = runTimerSafeTest {
                // When
                viewModel.onIntent(TimerIntent.SetTime(60.minutes))

                // Then
                val state = viewModel.uiState.value
                assertThat(state.formattedTime).isEqualTo("60:00")
            }
        }

        @Nested
        @DisplayName("progress 계산")
        inner class ProgressCalculation {

            @Test
            fun `다양한 시간 설정 시 부동소수점 오차 확인`() = runTimerSafeTest {
                // Given
                val times = listOf(
                    1.seconds, 1.minutes, 5.minutes, 10.minutes,
                    20.minutes, 30.minutes, 45.minutes, 60.minutes
                )

                times.forEach { time ->
                    // When
                    viewModel.onIntent(TimerIntent.SetTime(time))
                    val state = viewModel.uiState.value

                    // Then
                    val expectedProgress = time.inWholeMilliseconds.toFloat() /
                            TimerViewModel.MAX_TIME.inWholeMilliseconds.toFloat()
                    assertThat(state.progress).isWithin(0.0001f).of(expectedProgress)
                }
            }

            @Test
            fun `progress가 음수가 되지 않는지 확인`() = runTimerSafeTest {
                // Given
                viewModel.onIntent(TimerIntent.SetTime(5.seconds))

                // When
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(10.seconds)
                runCurrent()

                // Then
                val state = viewModel.uiState.value
                assertThat(state.progress).isAtLeast(0f)
            }

            @Test
            fun `SetTime과 DragProgress의 progress 계산 일관성 확인`() = runTimerSafeTest {
                // Given: SetTime으로 30분 설정
                viewModel.onIntent(TimerIntent.SetTime(30.minutes))
                val setTimeProgress = viewModel.uiState.value.progress

                // When: DragProgress로 0.5 (30분) 설정
                viewModel.onIntent(TimerIntent.DragProgress(0.5f))
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
        fun `UseCase에서 예외 발생 시 에러 SideEffect가 전달되는지 확인`() = runTimerSafeTest {
            // Given
            val viewModel = createViewModelWithMockUseCase(
                flow {
                    emit(TimerEvent.Tick(5.seconds))
                    throw IllegalArgumentException("타이머 실행 중 오류")
                }
            )
            viewModel.onIntent(TimerIntent.SetTime(5.seconds))

            viewModel.sideEffect.test {
                // When
                viewModel.onIntent(TimerIntent.Start)
                advanceTimeBy(1.seconds)
                runCurrent()

                // Then
                val events = cancelAndConsumeRemainingEvents()
                val errorEvent = events
                    .filterIsInstance<Event.Item<TimerSideEffect>>()
                    .map { it.value }
                    .filterIsInstance<TimerSideEffect.ShowError>()
                    .firstOrNull()

                assertThat(errorEvent).isNotNull()
                assertThat(errorEvent?.message).contains("타이머 실행 중 오류")

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `UseCase 예외 발생 시 타이머가 정지되는지 확인`() = runTimerSafeTest {
            // Given
            val timerFlow = flow<TimerEvent> {
                emit(TimerEvent.Tick(5.seconds))
                delay(1.seconds)
                emit(TimerEvent.Tick(4.seconds))
                delay(1.seconds)
                emit(TimerEvent.Tick(3.seconds))
                delay(1.seconds)
                emit(TimerEvent.Tick(2.seconds))
                delay(1.seconds)
                throw RuntimeException("타이머 오류")
            }

            val viewModel = createViewModelWithMockUseCase(timerFlow)
            viewModel.onIntent(TimerIntent.SetTime(5.seconds))
            viewModel.onIntent(TimerIntent.Start)

            // When
            advanceTimeBy(5.seconds)
            runCurrent()

            // Then
            val state = viewModel.uiState.value
            assertThat(state.isRunning).isFalse()
            assertThat(state.remainingTime).isEqualTo(5.seconds)
        }

        @Test
        fun `동시에 여러 에러를 발생시켜도 모두 전송되는지 확인`() = runTimerSafeTest {
            viewModel.sideEffect.test {
                // When
                viewModel.onIntent(TimerIntent.SetTime(0.seconds)) // 에러 1
                viewModel.onIntent(TimerIntent.Start) // 에러 2
                viewModel.onIntent(TimerIntent.SetTime(-5.seconds)) // 에러 3

                // Then
                val error1 = awaitItem()
                assertThat(error1).isInstanceOf(TimerSideEffect.ShowError::class.java)

                val error2 = awaitItem()
                assertThat(error2).isInstanceOf(TimerSideEffect.ShowError::class.java)

                val error3 = awaitItem()
                assertThat(error3).isInstanceOf(TimerSideEffect.ShowError::class.java)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("초과 시간(overtime)")
    inner class Overtime {

        @Test
        fun `타이머 완료 후 overtime이 0초로 초기화되는지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(3.seconds))
            viewModel.onIntent(TimerIntent.Start)

            // When
            advanceTimeBy(3.seconds)
            runCurrent()

            // Then
            val state = viewModel.uiState.value
            assertThat(state.overtime).isEqualTo(0.seconds)
        }

        @Test
        fun `overtime이 1초 단위로 증가하는지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(2.seconds))
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(2.seconds)
            runCurrent()

            // When
            advanceTimeBy(1.seconds)
            runCurrent()
            val overtime1 = viewModel.uiState.value.overtime

            advanceTimeBy(1.seconds)
            runCurrent()
            val overtime2 = viewModel.uiState.value.overtime

            advanceTimeBy(1.seconds)
            runCurrent()
            val overtime3 = viewModel.uiState.value.overtime

            // Then
            assertThat(overtime1).isAtLeast(1.seconds)
            assertThat(overtime2).isAtLeast(2.seconds)
            assertThat(overtime3).isAtLeast(3.seconds)
        }

        @Test
        fun `Stop 호출 시 overtime이 0으로 초기화되는지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(3.seconds))
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(3.seconds)
            runCurrent()
            advanceTimeBy(5.seconds)
            runCurrent()

            // When
            viewModel.onIntent(TimerIntent.Stop)

            // Then
            val state = viewModel.uiState.value
            assertThat(state.overtime).isEqualTo(0.seconds)
        }

        @Test
        fun `완료 전에는 overtime이 0인지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(10.seconds))
            viewModel.onIntent(TimerIntent.Start)

            // When
            advanceTimeBy(5.seconds)
            runCurrent()

            // Then
            val state = viewModel.uiState.value
            assertThat(state.overtime).isEqualTo(0.seconds)
            assertThat(state.isCompleted).isFalse()
        }
    }

    @Nested
    @DisplayName("동시성 및 엣지 케이스")
    inner class ConcurrencyAndEdgeCases {

        @Test
        fun `Pause와 Resume을 빠르게 연속 호출해도 안전한지 확인`() = runTimerSafeTest {
            // Given: 타이머 실행 중
            viewModel.onIntent(TimerIntent.SetTime(10.seconds))
            viewModel.onIntent(TimerIntent.Start)

            advanceTimeBy(2.seconds)
            runCurrent()

            // When: Pause와 Resume을 빠르게 반복
            viewModel.onIntent(TimerIntent.Pause)
            viewModel.onIntent(TimerIntent.Resume)
            viewModel.onIntent(TimerIntent.Pause)
            viewModel.onIntent(TimerIntent.Resume)
            viewModel.onIntent(TimerIntent.Pause)
            viewModel.onIntent(TimerIntent.Resume)

            advanceTimeBy(1.seconds)
            runCurrent()

            // Then: 최종 상태는 실행 중
            val state = viewModel.uiState.value
            assertThat(state.isRunning).isTrue()
            assertThat(state.isPaused).isFalse()
        }

        @Test
        fun `Stop을 빠르게 연속 호출해도 안전한지 확인`() = runTimerSafeTest {
            // Given: 타이머 실행 중
            viewModel.onIntent(TimerIntent.SetTime(10.seconds))
            viewModel.onIntent(TimerIntent.Start)

            advanceTimeBy(3.seconds)
            runCurrent()

            // When: Stop을 빠르게 여러 번 호출
            viewModel.onIntent(TimerIntent.Stop)
            viewModel.onIntent(TimerIntent.Stop)
            viewModel.onIntent(TimerIntent.Stop)

            // Then: 예외 없이 정상 처리
            val state = viewModel.uiState.value
            assertThat(state.isRunning).isFalse()
            assertThat(state.remainingTime).isEqualTo(10.seconds)
        }

        @Test
        fun `Start와 Stop을 번갈아 빠르게 호출해도 안전한지 확인`() = runTimerSafeTest {
            // Given
            viewModel.onIntent(TimerIntent.SetTime(10.seconds))

            // When: Start와 Stop을 빠르게 반복
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(1.seconds)
            runCurrent()

            viewModel.onIntent(TimerIntent.Stop)
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(1.seconds)
            runCurrent()

            viewModel.onIntent(TimerIntent.Stop)
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(1.seconds)
            runCurrent()

            // Then: 최종적으로 실행 중
            val state = viewModel.uiState.value
            assertThat(state.isRunning).isTrue()
            assertThat(state.remainingTime).isEqualTo(9.seconds)
        }

        @Test
        fun `타이머 Job 취소 후 즉시 재시작해도 안전한지 확인`() = runTimerSafeTest {
            // Given: 타이머 실행
            viewModel.onIntent(TimerIntent.SetTime(10.seconds))
            viewModel.onIntent(TimerIntent.Start)

            advanceTimeBy(3.seconds)
            runCurrent()

            // When: 즉시 Stop 후 재시작
            viewModel.onIntent(TimerIntent.Stop)
            viewModel.onIntent(TimerIntent.Start)

            advanceTimeBy(2.seconds)
            runCurrent()

            // Then: 정상 동작
            val state = viewModel.uiState.value
            assertThat(state.isRunning).isTrue()
            assertThat(state.remainingTime).isEqualTo(8.seconds)
        }

        @Test
        fun `여러 타이머를 순차적으로 실행해도 안전한지 확인`() = runTimerSafeTest {
            // 첫 번째 타이머
            viewModel.onIntent(TimerIntent.SetTime(3.seconds))
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(3.seconds)
            runCurrent()
            viewModel.onIntent(TimerIntent.Stop)

            // 두 번째 타이머
            viewModel.onIntent(TimerIntent.SetTime(5.seconds))
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(5.seconds)
            runCurrent()
            viewModel.onIntent(TimerIntent.Stop)

            // 세 번째 타이머
            viewModel.onIntent(TimerIntent.SetTime(2.seconds))
            viewModel.onIntent(TimerIntent.Start)
            advanceTimeBy(1.seconds)
            runCurrent()

            // Then: 마지막 타이머가 정상 동작
            val state = viewModel.uiState.value
            assertThat(state.isRunning).isTrue()
            assertThat(state.remainingTime).isEqualTo(1.seconds)
        }
    }
}



