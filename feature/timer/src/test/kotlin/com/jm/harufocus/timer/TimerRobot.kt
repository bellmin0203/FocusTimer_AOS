package com.jm.harufocus.timer

import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.jm.harufocus.common.time.TimeProvider
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.domain.usecase.preset.ManagePresetUseCase
import com.jm.harufocus.domain.usecase.review.CanRequestReviewUseCase
import com.jm.harufocus.setting.util.FakeSettingsRepository
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.usecase.CountdownTimerUseCase
import com.jm.harufocus.timer.usecase.TimerControlUseCase
import com.jm.harufocus.util.AnalyticsHelper
import com.jm.harufocus.util.NotificationSoundPlayer
import com.jm.harufocus.widget.HaruFocusWidgetUpdater
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlin.time.Duration

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
    val timerControlUseCase: TimerControlUseCase = TimerControlUseCase(CountdownTimerUseCase()),
    val managePresetUseCase: ManagePresetUseCase = mockk(relaxed = true),
    val analyticsHelper: AnalyticsHelper = mockk(relaxed = true),
    val notificationSoundPlayer: NotificationSoundPlayer = mockk(relaxed = true),
    val widgetUpdater: HaruFocusWidgetUpdater = mockk(relaxed = true),
    val canRequestReviewUseCase: CanRequestReviewUseCase = mockk(relaxed = true),
    val settingsRepository: SettingsRepository = FakeSettingsRepository()
) {
    // 1. Core Logic Setup

    // TimerManager는 UnconfinedTestDispatcher를 사용하여 launch 즉시 실행 보장
    val timerManager = TimerManagerImpl(
        timerControlUseCase,
        UnconfinedTestDispatcher(scope.testScheduler),
        timeProvider
    )

    // 2. Default Mock Setup (Settings)
    // Removed: val settingsRepository: SettingsRepository = FakeSettingsRepository()

    // 3. ViewModel Initialization
    val viewModel: TimerViewModel =
        TimerViewModel(
            timerManager = timerManager,
            managePresetUseCase = managePresetUseCase,
            settingsRepository = settingsRepository,
            notificationSoundPlayer = notificationSoundPlayer,
            widgetUpdater = widgetUpdater,
            canRequestReviewUseCase = canRequestReviewUseCase,
            analyticsHelper = analyticsHelper
        )


    // 가상 시간과 동기화될 Mock Time
    private var currentMockTime = 1000L

    init {
        // Mock TimeProvider가 항상 currentMockTime을 반환하도록 설정
        every { timeProvider.currentTimeMillis() } answers { currentMockTime }
        scope.runCurrent()
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
        
        // Simulate Service: SideEffect를 받아서 TimerManager 시작
        val state = viewModel.uiState.value
        if (state.remainingTime > Duration.ZERO && !state.isRunning) {
            timerManager.start(state.initialTime, state.remainingTime, reminderThresholds)
            scope.runCurrent()
        }
    }

    fun pause() {
        viewModel.onIntent(TimerIntent.Pause)
        scope.runCurrent()
        
        // Simulate Service
        if (viewModel.uiState.value.isRunning) {
            timerManager.pause()
            scope.runCurrent()
        }
    }

    fun resume() {
        viewModel.onIntent(TimerIntent.Resume)
        scope.runCurrent()
        
        // Simulate Service
        if (viewModel.uiState.value.isPaused) {
            timerManager.resume()
            scope.runCurrent()
        }
    }

    fun stop() {
        viewModel.onIntent(TimerIntent.Stop)
        scope.runCurrent()
        
        // Simulate Service
        timerManager.stop(viewModel.uiState.value.initialTime)
        scope.runCurrent()
    }

    fun drag(progress: Float) {
        viewModel.onIntent(TimerIntent.DragProgress(progress))
        scope.runCurrent()
    }

    fun complete() {
        viewModel.onIntent(TimerIntent.Complete)
        scope.runCurrent()

        if (viewModel.uiState.value.isOvertime) {
            timerManager.stop(viewModel.uiState.value.initialTime)
            scope.runCurrent()
        }
    }

    fun onIntent(intent: TimerIntent) {
        viewModel.onIntent(intent)
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

    suspend fun testSideEffects(
        validate: suspend TurbineTestContext<TimerSideEffect>.() -> Unit
    ) {
        viewModel.sideEffect.test {
            validate()
        }
    }

    /**
     * SideEffect를 하나 소비하고 무시합니다.
     * 테스트 시나리오상 발생했지만 검증할 필요가 없는 이전 이벤트를 처리할 때 사용합니다.
     */
    suspend fun consumeSideEffect() {
        viewModel.sideEffect.test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
