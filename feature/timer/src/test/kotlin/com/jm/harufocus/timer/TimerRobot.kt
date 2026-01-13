package com.jm.harufocus.timer

import app.cash.turbine.test
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.usecase.CountdownTimerUseCase
import com.jm.harufocus.timer.usecase.TimerControlUseCase
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlin.time.Duration

/**
 * 🤖 Test Robot (DSL)
 * 복잡한 Setup과 Intent 호출을 캡슐화하여 테스트 가독성을 높입니다.
 */
class TimerRobot(private val scope: TestScope) {
    // 실제 의존성 주입 (통합 테스트용)
    private val timerControlUseCase = TimerControlUseCase(CountdownTimerUseCase())
    private val timerManager = TimerManagerImpl(timerControlUseCase, Dispatchers.Main)

    // ViewModel 생성 (UseCase들은 Mock으로 단순화 - 필요시 실제 객체 사용 가능)
    val viewModel = TimerViewModel(
        getAllPresetsUseCase = mockk(relaxed = true),
        addPresetUseCase = mockk(),
        updatePresetUseCase = mockk(),
        deletePresetUseCase = mockk(),
        timerManager = timerManager
    )

    // --- Actions (Given / When) ---

    fun setTime(duration: Duration) {
        viewModel.onIntent(TimerIntent.SetTime(duration))
        scope.advanceUntilIdle()
    }

    fun start(reminderThresholds: List<Duration> = emptyList()) {
        viewModel.onIntent(TimerIntent.Start(reminderThresholds = reminderThresholds))
        scope.runCurrent()
    }

    fun startAndAdvance(duration: Duration) {
        start()
        scope.advanceTimeBy(duration)
        scope.runCurrent()
    }

    fun advanceTimeBy(duration: Duration) {
        scope.advanceTimeBy(duration)
        scope.runCurrent()
    }

    fun advanceUntilIdle() {
        scope.advanceUntilIdle()
    }

    fun pause() {
        viewModel.onIntent(TimerIntent.Pause)
        scope.runCurrent()
    }

    fun resume() {
        viewModel.onIntent(TimerIntent.Resume)
        scope.runCurrent()
    }

    fun stop() {
        viewModel.onIntent(TimerIntent.Stop)
        scope.advanceUntilIdle()
    }

    fun drag(progress: Float) {
        viewModel.onIntent(TimerIntent.DragProgress(progress))
        scope.runCurrent()
    }

    // --- Assertions (Then) ---

    fun verifyState(block: TimerUiState.() -> Unit) {
        block(viewModel.uiState.value)
    }

    suspend fun verifySideEffect(block: (TimerSideEffect) -> Unit) {
        viewModel.sideEffect.test {
            block(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}