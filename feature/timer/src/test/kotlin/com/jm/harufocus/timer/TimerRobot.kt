package com.jm.harufocus.timer

import app.cash.turbine.test
import com.jm.harufocus.common.model.NotificationSoundType
import com.jm.harufocus.common.time.TimeProvider
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.usecase.CountdownTimerUseCase
import com.jm.harufocus.timer.usecase.TimerControlUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * ⏱️ TimerTestHelper (DSL)
 *
 * 타이머 로직 테스트에 최적화된 헬퍼 클래스입니다.
 * 가상 시간(Coroutine Virtual Time)과 모의 시간(Mock TimeProvider)을 동기화하여
 * 시간 흐름에 따른 상태 변화를 정밀하게 검증할 수 있습니다.
 */
@ExperimentalCoroutinesApi
class TimerRobot(
    private val scope: TestScope,
    val timeProvider: TimeProvider = mockk(relaxed = true),
    val timerControlUseCase: TimerControlUseCase = TimerControlUseCase(CountdownTimerUseCase())
) {
    // 1. Core Logic Setup

    // TimerManager는 UnconfinedTestDispatcher를 사용하여 launch 즉시 실행 보장
    val timerManager = TimerManagerImpl(
        timerControlUseCase,
        UnconfinedTestDispatcher(scope.testScheduler),
        timeProvider
    )

    // 2. Default Mock Setup (Settings)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true) {
        every { isScreenOn } returns flowOf(true)
        every { isHapticFeedback } returns flowOf(true)
        every { isTickSound } returns flowOf(true)
        every { isMinimizedControls } returns flowOf(false)
        every { isPulseAnimationEnabled } returns flowOf(true)
        every { isScreenRotationEnabled } returns flowOf(false)
        every { isRememberLastSession } returns flowOf(true)
        every { defaultPresetId } returns flowOf(null)
        every { defaultSessionDuration } returns flowOf(25.minutes)
        every { notificationSoundType } returns flowOf(NotificationSoundType.BELL)
        every { isNotificationVibrate } returns flowOf(true)
    }

    // 3. ViewModel Initialization
    val viewModel = TimerViewModel(
        timerManager = timerManager,
        managePresetUseCase = mockk(relaxed = true),
        settingsRepository = settingsRepository,
        notificationSoundPlayer = mockk(relaxed = true),
        widgetUpdater = mockk(relaxed = true),
        canRequestReviewUseCase = mockk(relaxed = true),
        analyticsHelper = mockk(relaxed = true),
    )

    // 가상 시간과 동기화될 Mock Time
    private var currentMockTime = 1000L

    init {
        // Mock TimeProvider가 항상 currentMockTime을 반환하도록 설정
        every { timeProvider.currentTimeMillis() } answers { currentMockTime }
    }

    // --- ⏳ Time Control DSL ---

    /**
     * 시간을 흐르게 하고, Mock 시간도 동기화합니다.
     * @param duration 흐르게 할 시간
     */
    fun advanceTime(duration: Duration) {
        val millis = duration.inWholeMilliseconds
        currentMockTime += millis
        scope.advanceTimeBy(millis)
        scope.runCurrent() // 대기 중인 작업 처리
    }

    /**
     * 현재 시점의 모든 대기 작업을 처리합니다.
     */
    fun runCurrent() {
        scope.runCurrent()
    }

    /**
     * 모든 작업이 끝날 때까지 시간을 진행시킵니다.
     */
    fun advanceUntilIdle() {
        scope.advanceUntilIdle()
    }

    // --- 🎬 Timer Actions (Given / When) ---

    fun setTime(duration: Duration) {
        viewModel.onIntent(TimerIntent.SetTime(duration))
        scope.runCurrent()
    }

    fun start(reminderThresholds: List<Duration> = emptyList()) {
        viewModel.onIntent(TimerIntent.Start(reminderThresholds = reminderThresholds))
        scope.runCurrent()
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
        scope.runCurrent()
    }

    fun drag(progress: Float) {
        viewModel.onIntent(TimerIntent.DragProgress(progress))
        scope.runCurrent()
    }

    fun complete() {
        viewModel.onIntent(TimerIntent.Complete)
        scope.runCurrent()
    }

    // --- ✅ Assertions (Then) ---

    /**
     * UI 상태를 검증합니다.
     */
    fun verifyState(block: TimerUiState.() -> Unit) {
        block(viewModel.uiState.value)
    }

    /**
     * SideEffect를 검증합니다. (비동기 Flow)
     */
    suspend fun verifySideEffect(block: (TimerSideEffect) -> Unit) {
        viewModel.sideEffect.test {
            block(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
