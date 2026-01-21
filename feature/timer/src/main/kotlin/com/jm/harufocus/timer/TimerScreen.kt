package com.jm.harufocus.timer

import android.content.pm.ActivityInfo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.timer.component.LandscapeTimerLayout
import com.jm.harufocus.timer.component.MinimizedControlsState
import com.jm.harufocus.timer.component.NavigationDrawerContent
import com.jm.harufocus.timer.component.PortraitTimerLayout
import com.jm.harufocus.timer.component.TimerDialogs
import com.jm.harufocus.timer.component.rememberMinimizedControlsState
import com.jm.harufocus.timer.effect.KeepScreenOnEffect
import com.jm.harufocus.timer.effect.ScreenOrientationEffect
import com.jm.harufocus.timer.effect.SystemBarsVisibilityEffect
import com.jm.harufocus.timer.model.HapticPattern
import com.jm.harufocus.timer.model.TimerDialogState
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.model.toUiText
import com.jm.harufocus.timer.usecase.TimerStatus
import com.jm.harufocus.timer.util.getPresetColorScheme
import com.jm.harufocus.ui.component.rememberPickerState
import com.jm.harufocus.ui.util.PreviewProvider
import com.jm.logutil.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

@Composable
fun TimerScreen(
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: TimerViewModel = hiltViewModel(),
    onRequestInAppReview: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // 시스템 설정과 무관하게 센서에 따라 화면 회전 허용
    ScreenOrientationEffect(orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR)

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is TimerSideEffect.ShowError -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = effect.errorType.toUiText().asString(context),
                            withDismissAction = true
                        )
                    }
                }

                is TimerSideEffect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(effect.toUiText().asString(context))
                    }
                }

                is TimerSideEffect.ShowTimerCompleted -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(context.getString(R.string.timer_is_completed_text))
                    }
                }

                is TimerSideEffect.ShowReminder -> {
                    val message = effect.remainingTime.toComponents { _, minutes, seconds, _ ->
                        "${minutes}분 ${seconds}초 남았습니다."
                    }

                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(message)
                    }
                }

                is TimerSideEffect.HapticFeedback -> {
                    // 햅틱 피드백 설정이 활성화된 경우에만 실행
                    if (uiState.isHapticFeedbackEnabled) {
                        val feedbackConstant = when (effect.pattern) {
                            HapticPattern.TICK -> HapticFeedbackType.SegmentTick
                            HapticPattern.COMPLETED -> HapticFeedbackType.LongPress
                            HapticPattern.REMINDER -> HapticFeedbackType.ContextClick
                        }
                        haptics.performHapticFeedback(feedbackConstant)
                    }
                }

                is TimerSideEffect.RequestInAppReview -> {
                    onRequestInAppReview()
                }
            }
        }
    }

    TimerScreenContent(
        uiState = uiState,
        scope = scope,
        snackbarHostState = snackbarHostState,
        drawerState = drawerState,
        onIntent = viewModel::onIntent,
        onSettingsClick = onSettingsClick,
        onStatsClick = onStatsClick
    )
}

/**
 * 타이머 메인 화면
 * - 상단: 앱 제목만 표시
 * - 중앙: 원형 타이머 UI
 * - 하단: 타이머 컨트롤 + 프리셋/설정 버튼 (모바일 환경에서 접근 용이)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerScreenContent(
    uiState: TimerUiState,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    onIntent: (TimerIntent) -> Unit,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {}
) {
    // 화면 켜짐 유지
    KeepScreenOnEffect(
        shouldKeepScreenOn = uiState.isRunning && uiState.isScreenOnEnabled
    )

    // 최소화된 컨트롤 상태 관리
    val minimizedControlsState = rememberMinimizedControlsState()

    // 최소화된 컨트롤 - 자동 숨김 타이머
    LaunchedEffect(
        uiState.isRunning,
        uiState.isMinimizedControlsEnabled,
        minimizedControlsState.isControlsVisible
    ) {
        if (uiState.shouldStartAutoHideTimer(minimizedControlsState.isControlsVisible)) {
            delay(MinimizedControlsState.UI_AUTO_HIDE_DELAY_MILLIS)
            if (minimizedControlsState.shouldAutoHide()) {
                minimizedControlsState.hideControls()
            }
        }
    }

    // 타이머가 멈추면 UI 다시 표시
    LaunchedEffect(uiState.isRunning) {
        if (!uiState.isRunning) {
            minimizedControlsState.showControls()
        }
    }

    // 시스템바 제어
    SystemBarsVisibilityEffect(
        shouldHide = uiState.shouldHideUi(minimizedControlsState.isControlsVisible)
    )

    // 다이얼로그/바텀시트 상태
    val presetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var dialogState by remember { mutableStateOf<TimerDialogState>(TimerDialogState.None) }

    // PickerState 생성
    val hourPickerState = rememberPickerState()
    val minutePickerState = rememberPickerState()
    val secondPickerState = rememberPickerState()

    val isLandscape =
        LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            NavigationDrawerContent(
                drawerState = drawerState,
                onSettingsClick = onSettingsClick,
                onPresetsClick = { dialogState = TimerDialogState.PresetManagement },
                onStatsClick = onStatsClick
            )
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            // 전체 화면에 터치 감지를 위한 Box
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pointerInput(
                        uiState.isRunning,
                        uiState.isMinimizedControlsEnabled,
                        minimizedControlsState.isControlsVisible
                    ) {
                        detectTapGestures {
                            if (uiState.shouldHideUi(minimizedControlsState.isControlsVisible)) {
                                minimizedControlsState.recordInteraction()
                                LogUtil.d("화면 터치 - UI 표시")
                            } else {
                                minimizedControlsState.hideControls()
                            }
                        }
                    }
            ) {
                // 선택된 프리셋의 색상 가져오기 (안전한 범위 검증 적용)
                val presetColors = getPresetColorScheme(uiState.selectedPreset?.colorIndex)

                // UI 컨트롤 표시 여부
                val shouldShowUiControls = !uiState.shouldHideUi(minimizedControlsState.isControlsVisible)

                // 가로/세로 모드에 따른 레이아웃 분기
                if (isLandscape) {
                    LandscapeTimerLayout(
                        uiState = uiState,
                        presetColors = presetColors,
                        shouldShowUiControls = shouldShowUiControls,
                        onIntent = onIntent,
                        onTimeClick = { dialogState = TimerDialogState.TimeInput },
                        onPresetManagementClick = { dialogState = TimerDialogState.PresetManagement }
                    )
                } else {
                    PortraitTimerLayout(
                        uiState = uiState,
                        presetColors = presetColors,
                        shouldShowUiControls = shouldShowUiControls,
                        onIntent = onIntent,
                        onTimeClick = { dialogState = TimerDialogState.TimeInput },
                        onPresetManagementClick = { dialogState = TimerDialogState.PresetManagement }
                    )
                }

                // 메뉴 버튼
                if (shouldShowUiControls) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.content_description_menu),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 다이얼로그/바텀시트 통합 표시
            TimerDialogs(
                dialogState = dialogState,
                uiState = uiState,
                presetSheetState = presetSheetState,
                hourPickerState = hourPickerState,
                minutePickerState = minutePickerState,
                secondPickerState = secondPickerState,
                scope = scope,
                onIntent = onIntent,
                onDialogStateChange = { dialogState = it }
            )
        }
    }
}

// region Preview

@ThemePreviews
@Composable
private fun ScreenPreviewHaru() {
    val presets = remember { PreviewProvider.samplePresets }
    val mockUiState = TimerUiState(
        initialTime = 25.minutes,
        remainingTime = 25.minutes,
        progress = 0f,
        presets = presets
    )

    HaruFocusTheme {
        TimerScreenContent(
            onIntent = {},
            uiState = mockUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@ThemePreviews
@Composable
private fun ScreenIdlePreviewHaru() {
    val presets = remember { PreviewProvider.samplePresets }
    val idleUiState = TimerUiState(
        initialTime = 25.minutes,
        remainingTime = 25.minutes,
        progress = 0.5f,
        selectedPreset = presets.first(),
        presets = presets,
    )

    HaruFocusTheme {
        TimerScreenContent(
            onIntent = {},
            uiState = idleUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@ThemePreviews
@Composable
private fun ScreenCompletePreviewHaru() {
    val presets = remember { PreviewProvider.samplePresets }
    val completedUiState = TimerUiState(
        initialTime = 25.minutes,
        status = TimerStatus.Overtime,
        overtime = 5.minutes,
        progress = 0f,
        selectedPreset = presets.first(),
        presets = presets,
    )

    HaruFocusTheme {
        TimerScreenContent(
            onIntent = {},
            uiState = completedUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

// endregion

