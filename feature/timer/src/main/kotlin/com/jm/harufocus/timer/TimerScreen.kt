package com.jm.harufocus.timer

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jm.harufocus.common.TimerServiceAction
import com.jm.harufocus.designsystem.component.CircularTimerProgress
import com.jm.harufocus.designsystem.component.FocusIconButton
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.component.TimerColorPresets
import com.jm.harufocus.designsystem.component.TimerColorScheme
import com.jm.harufocus.designsystem.icon.HaruFocusIcons
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.timer.component.AddPresetDialog
import com.jm.harufocus.timer.component.DeletePresetDialog
import com.jm.harufocus.timer.component.EditPresetDialog
import com.jm.harufocus.timer.component.MinimizedControlsState
import com.jm.harufocus.timer.component.NavigationDrawerContent
import com.jm.harufocus.timer.component.PresetManagementBottomSheet
import com.jm.harufocus.timer.component.TimeInputBottomSheet
import com.jm.harufocus.timer.component.rememberMinimizedControlsState
import com.jm.harufocus.timer.model.HapticPattern
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerSideEffect
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.model.toUiText
import com.jm.harufocus.timer.service.TimerService
import com.jm.harufocus.timer.usecase.TimerStatus
import com.jm.harufocus.ui.component.rememberPickerState
import com.jm.harufocus.ui.util.PreviewProvider
import com.jm.harufocus.util.KeepScreenOnManager
import com.jm.harufocus.util.findActivity
import com.jm.logutil.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun TimerScreen(
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    viewModel: TimerViewModel = hiltViewModel()
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
            }
        }
    }

    TimerScreen(
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
private fun TimerScreen(
    uiState: TimerUiState,
    scope: CoroutineScope = rememberCoroutineScope(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    onIntent: (TimerIntent) -> Unit,
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {}
) {
    val context = LocalContext.current

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
            delay(MinimizedControlsState.UI_AUTO_HIDE_DELAY_MILLIS) // 2초 대기
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

    val presetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPresets by remember { mutableStateOf(false) }
    var showTimeInput by remember { mutableStateOf(false) }

    // 프리셋 다이얼로그 상태
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var showEditPresetDialog by remember { mutableStateOf<Preset?>(null) }
    var showDeletePresetDialog by remember { mutableStateOf<Preset?>(null) }

    val isLandscape =
        LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            NavigationDrawerContent(
                drawerState = drawerState,
                onSettingsClick = onSettingsClick,
                onPresetsClick = {
                    showPresets = true
                },
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
                    // 화면 터치 감지 - UI 표시
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
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 48.dp, vertical = 24.dp), // 가로 모드에서 좌우 여백 확보
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 선택된 프리셋의 색상 가져오기
                        val presetColors =
                            TimerColorPresets.presetColors[uiState.selectedPreset?.colorIndex ?: 0]

                        // 원형 타이머 (좌측 배치, weight로 공간 차지)
                        CircularTimerProgress(
                            progress = uiState.progress,
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isTimerActive, // 타이머가 유휴 상태일 때만 드래그 가능
                            progressColor = if (uiState.isCompleted) {
                                MaterialTheme.colorScheme.tertiary // 완료 상태일 때 다른 색상
                            } else {
                                presetColors.progressColor // 일반 상태
                            },
                            knobColor = presetColors.knobColor,
                            tickColor = presetColors.tickColor,
                            labelColor = presetColors.labelColor,
                            onProgressChange = { newProgress ->
                                onIntent(TimerIntent.DragProgress(newProgress))
                            },
                            isCompleted = uiState.isCompleted,
                            pulseAnimationEnabled = uiState.isPulseAnimationEnabled
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        color = if (uiState.isCompleted) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        },
                                    )
                                    .clickable(enabled = !uiState.isTimerActive) {
                                        showTimeInput = true
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // HH:MM:SS 형식으로 시간 표시
                                val timeText = uiState.formattedTime

                                Text(
                                    text = timeText,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = if (uiState.isCompleted) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                // 완료 상태일 때 안내 메시지 표시
                                if (uiState.isCompleted) {
                                    Text(
                                        text = stringResource(R.string.timer_completed_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        // 우측 패널 (프리셋 선택 + 컨트롤 버튼)
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 32.dp), // 타이머와의 간격
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 프리셋 선택 AssistChip
                            if (uiState.isIdle && !uiState.shouldHideUi(minimizedControlsState.isControlsVisible)) {
                                AssistChip(
                                    onClick = { showPresets = true },
                                    label = {
                                        Text(
                                            text = uiState.selectedPreset?.name
                                                ?: stringResource(R.string.preset_section_header),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(50),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = null
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            // 우측 컨트롤 영역 - 최소화된 컨트롤이 활성화되고 UI가 숨겨진 상태가 아닐 때만 표시
                            if (!uiState.shouldHideUi(minimizedControlsState.isControlsVisible)) {
                                TimerControlsSection(
                                    uiState = uiState,
                                    presetColors = presetColors,
                                    onIntent = onIntent,
                                    isVertical = true
                                )
                            }
                        }
                    }
                } else {
                    // 세로 모드 UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 선택된 프리셋의 색상 가져오기
                        val presetColors =
                            TimerColorPresets.presetColors[uiState.selectedPreset?.colorIndex ?: 0]

                        // 원형 타이머
                        CircularTimerProgress(
                            progress = uiState.progress,
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isTimerActive, // 타이머가 유휴 상태일 때만 드래그 가능
                            progressColor = if (uiState.isCompleted) {
                                MaterialTheme.colorScheme.tertiary // 완료 상태일 때 다른 색상
                            } else {
                                presetColors.progressColor // 일반 상태
                            },
                            knobColor = presetColors.knobColor,
                            tickColor = presetColors.tickColor,
                            labelColor = presetColors.labelColor,
                            onProgressChange = { newProgress ->
                                onIntent(TimerIntent.DragProgress(newProgress))
                            },
                            isCompleted = uiState.isCompleted,
                            pulseAnimationEnabled = uiState.isPulseAnimationEnabled
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(
                                        color = if (uiState.isCompleted) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                        },
                                    )
                                    .clickable(enabled = !uiState.isTimerActive) {
                                        showTimeInput = true
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // HH:MM:SS 형식으로 시간 표시
                                val timeText = uiState.formattedTime

                                Text(
                                    text = timeText,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = if (uiState.isCompleted) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                // 완료 상태일 때 안내 메시지 표시
                                if (uiState.isCompleted) {
                                    Text(
                                        text = stringResource(R.string.timer_completed_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }

                        // 프리셋 선택 (타이머 다이얼 바로 아래)
                        if (uiState.isIdle && !uiState.shouldHideUi(minimizedControlsState.isControlsVisible)) {
                            PresetListRow(
                                presets = uiState.presets,
                                selectedPresetId = uiState.selectedPreset?.id,
                                isEnabled = uiState.isIdle,
                                onPresetClick = { presetId ->
                                    onIntent(TimerIntent.SelectPreset(presetId))
                                },
                                onManageClick = { showPresets = true },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // 하단 컨트롤 영역 - 최소화된 컨트롤이 활성화되고 UI가 숨겨진 상태가 아닐 때만 표시
                        if (!uiState.shouldHideUi(minimizedControlsState.isControlsVisible)) {
                            TimerControlsSection(
                                uiState = uiState,
                                presetColors = presetColors,
                                onIntent = onIntent,
                                isVertical = false
                            )
                        }
                    }
                }

                if (!uiState.shouldHideUi(minimizedControlsState.isControlsVisible)) {
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

            // PickerState 생성
            val hourPickerState = rememberPickerState()
            val minutePickerState = rememberPickerState()
            val secondPickerState = rememberPickerState()

            // 시간 입력 Bottom Sheet
            if (showTimeInput) {
                TimeInputBottomSheet(
                    initialTime = uiState.remainingTime,
                    hourPickerState = hourPickerState,
                    minutePickerState = minutePickerState,
                    secondPickerState = secondPickerState,
                    onDismissRequest = {
                        val hours = hourPickerState.selectedItem.toIntOrNull() ?: 0
                        val minutes = minutePickerState.selectedItem.toIntOrNull() ?: 0
                        val seconds = secondPickerState.selectedItem.toIntOrNull() ?: 0

                        val setTime = hours.hours + minutes.minutes + seconds.seconds
                        if (setTime > 0.seconds) onIntent(TimerIntent.SetTime(setTime))
                        showTimeInput = false
                    },
                    onConfirm = { time ->
                        onIntent(TimerIntent.SetTime(time))
                        onIntent(TimerIntent.Start())

                        // 서비스 시작
                        val intent = Intent(context, TimerService::class.java).apply {
                            action = TimerServiceAction.ACTION_START
                            putExtra(TimerServiceAction.EXTRA_DURATION, time.inWholeMilliseconds)
                        }
                        ContextCompat.startForegroundService(context, intent)

                        showTimeInput = false
                    }
                )
            }

            // 프리셋 추가 다이얼로그
            if (showAddPresetDialog) {
                AddPresetDialog(
                    initialMinutes = uiState.remainingTime.inWholeMinutes.toInt(),
                    initialSeconds = (uiState.remainingTime.inWholeSeconds % 60).toInt(),
                    initialColorIndex = 0,
                    onDismiss = { showAddPresetDialog = false },
                    onConfirm = { name, minutes, seconds, colorIndex ->
                        val duration = minutes.minutes + seconds.seconds
                        onIntent(TimerIntent.SaveAsPreset(name, duration, colorIndex))
                        showAddPresetDialog = false
                    }
                )
            }

            // 프리셋 수정 다이얼로그
            showEditPresetDialog?.let { preset ->
                EditPresetDialog(
                    preset = preset,
                    onDismiss = { showEditPresetDialog = null },
                    onConfirm = { name, minutes, seconds, colorIndex ->
                        val duration = minutes.minutes + seconds.seconds
                        val updatedPreset = preset.copy(
                            name = name,
                            duration = duration,
                            colorIndex = colorIndex
                        )
                        onIntent(TimerIntent.UpdatePreset(updatedPreset))
                        showEditPresetDialog = null
                    }
                )
            }

            // 프리셋 삭제 확인 다이얼로그
            showDeletePresetDialog?.let { preset ->
                DeletePresetDialog(
                    presetName = preset.name,
                    onDismiss = { showDeletePresetDialog = null },
                    onConfirm = {
                        onIntent(TimerIntent.DeletePreset(preset.id))
                    }
                )
            }

            // 프리셋 관리 BottomSheet
            if (showPresets) {
                PresetManagementBottomSheet(
                    sheetState = presetSheetState,
                    presets = uiState.presets,
                    canAddPreset = uiState.hasPresetSpaceAvailable,
                    onDismiss = {
                        scope.launch {
                            presetSheetState.hide()
                            showPresets = false
                        }
                    },
                    onPresetClick = { presetId ->
                        onIntent(TimerIntent.SelectPreset(presetId))
                        scope.launch {
                            presetSheetState.hide()
                            showPresets = false
                        }
                    },
                    onAddPreset = {
                        showPresets = false
                        showAddPresetDialog = true
                    },
                    onEditPreset = { preset ->
//                        showPresets = false
                        showEditPresetDialog = preset
                    },
                    onDeletePreset = { presetId ->
                        onIntent(TimerIntent.DeletePreset(presetId))
                    }
                )
            }
        }
    }
}

/**
 * 화면 켜짐 유지를 관리하는 Composable Effect
 *
 * @param shouldKeepScreenOn 화면을 켜진 상태로 유지해야 하는지 여부
 */
@Composable
fun KeepScreenOnEffect(shouldKeepScreenOn: Boolean) {
    val context = LocalContext.current
    val window = context.findActivity()?.window

    DisposableEffect(shouldKeepScreenOn) {
        val manager = KeepScreenOnManager(window)

        if (shouldKeepScreenOn) {
            manager.enableKeepScreenOn()
        } else {
            manager.disableKeepScreenOn()
        }

        onDispose {
            manager.release()
        }
    }
}

/**
 * 화면 방향을 강제로 제어하는 Effect
 * 시스템 설정을 무시하고 지정된 orientation을 따르도록 함
 */
@Composable
fun ScreenOrientationEffect(orientation: Int) {
    val context = LocalContext.current

    DisposableEffect(orientation) {
        val activity = context.findActivity()
        val originalOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // 요청된 방향으로 설정 (시스템 설정 무시)
        activity?.requestedOrientation = orientation

        onDispose {
            // 화면을 벗어날 때 원래 방향 설정으로 복구
            activity?.requestedOrientation = originalOrientation
        }
    }
}

// 시스템바 가시성 제어 Effect
@Composable
fun SystemBarsVisibilityEffect(shouldHide: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(shouldHide) {
        val activity = context.findActivity()
        val windowInsetsController = activity?.window?.let {
            WindowCompat.getInsetsController(it, view)
        }

        if (shouldHide) {
            windowInsetsController?.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            LogUtil.d("시스템바 숨김")
        } else {
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            LogUtil.d("시스템바 표시")
        }

        onDispose {
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

/**
 * 타이머 컨트롤 버튼들을 담당하는 섹션 컴포넌트
 *
 * @param uiState 현재 타이머 UI 상태
 * @param presetColors 선택된 프리셋의 색상 정보
 * @param onIntent 타이머 인텐트 처리 함수
 * @param isVertical 버튼들을 수직으로 배치할지 여부 (가로 모드 대응)
 */
@Composable
private fun TimerControlsSection(
    uiState: TimerUiState,
    presetColors: TimerColorScheme,
    onIntent: (TimerIntent) -> Unit,
    modifier: Modifier = Modifier,
    isVertical: Boolean = false
) {
    if (isVertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerControlButtons(uiState, presetColors, onIntent)
        }
    } else {
        Row(
            modifier = modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimerControlButtons(uiState, presetColors, onIntent)
        }
    }
}

/**
 * 실제 타이머 컨트롤 버튼들의 집합
 * 레이아웃(Row/Column)과 무관하게 버튼 로직만 포함
 */
@Composable
private fun TimerControlButtons(
    uiState: TimerUiState,
    presetColors: TimerColorScheme,
    onIntent: (TimerIntent) -> Unit
) {
    val context = LocalContext.current

    // 완료 상태일 때는 완료 버튼 표시
    if (uiState.isCompleted) {
        FocusIconButton(
            onClick = {
                // 타이머 완료 처리 (Snackbar 표시)
                onIntent(TimerIntent.Complete)

                // 서비스에 완료 처리 요청
                val intent =
                    Intent(context, TimerService::class.java).apply {
                        action = TimerServiceAction.ACTION_COMPLETE
                    }
                ContextCompat.startForegroundService(context, intent)
            },
            icon = HaruFocusIcons.Check,
            contentDescription = stringResource(R.string.content_description_complete),
            containerColor = presetColors.progressColor,
            contentColor = Color.White
        )
    } else {
        // 재생/일시정지 버튼
        FocusIconButton(
            onClick = {
                val intent = Intent(context, TimerService::class.java)

                when {
                    uiState.isPaused -> {
                        // 타이머 재개 (Paused 상태를 먼저 체크해야 함)
                        onIntent(TimerIntent.Resume)

                        // 서비스에 재개 알림
                        intent.action = TimerServiceAction.ACTION_RESUME
                        ContextCompat.startForegroundService(
                            context,
                            intent
                        )
                    }

                    uiState.isIdle -> {
                        // 타이머 시작 (Idle 상태에서만)
                        onIntent(TimerIntent.Start())

                        // 서비스 시작
                        intent.action = TimerServiceAction.ACTION_START
                        intent.putExtra(
                            TimerServiceAction.EXTRA_DURATION,
                            uiState.remainingTime.inWholeMilliseconds
                        )
                        ContextCompat.startForegroundService(
                            context,
                            intent
                        )
                    }

                    else -> {
                        // 타이머 일시정지
                        onIntent(TimerIntent.Pause)

                        // 서비스에 일시정지 알림
                        intent.action = TimerServiceAction.ACTION_PAUSE
                        ContextCompat.startForegroundService(
                            context,
                            intent
                        )
                    }
                }
            },
            icon = if (uiState.isRunning) HaruFocusIcons.Pause else HaruFocusIcons.PlayArrow,
            contentDescription = if (uiState.isRunning) stringResource(R.string.content_description_pause) else stringResource(
                R.string.content_description_play
            ),
            containerColor = presetColors.progressColor,
            contentColor = Color.White
        )

        if (uiState.isTimerActive) {
            // 리셋 버튼
            FocusIconButton(
                onClick = {
                    // 타이머 정지
                    onIntent(TimerIntent.Stop)

                    // 서비스 종료
                    val intent = Intent(
                        context,
                        TimerService::class.java
                    ).apply {
                        action = TimerServiceAction.ACTION_STOP
                    }
                    ContextCompat.startForegroundService(
                        context,
                        intent
                    )
                },
                icon = HaruFocusIcons.RestartAlt,
                contentDescription = stringResource(R.string.content_description_reset)
            )
        }
    }
}

@Composable
private fun PresetListRow(
    presets: List<Preset>,
    selectedPresetId: Int?,
    isEnabled: Boolean,
    onPresetClick: (Int) -> Unit,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 프리셋 관리(목록/추가) 버튼을 맨 앞에 배치
        item {
            AssistChip(
                onClick = onManageClick,
                label = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.content_description_manage_presets),
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null,
                enabled = isEnabled
            )
        }

        // 2. 프리셋 목록 표시
        items(items = presets, key = { it.id }) { preset ->
            // 선택된 프리셋의 색상 테마 가져오기 (시각적 피드백)
            val presetColor = TimerColorPresets.presetColors.getOrNull(preset.colorIndex)
                ?: TimerColorPresets.presetColors[0]

            val isSelected = preset.id == selectedPresetId

            FilterChip(
                selected = isSelected,
                onClick = { onPresetClick(preset.id) },
                label = {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = presetColor.progressColor.copy(alpha = 0.2f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = isEnabled,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = presetColor.progressColor
                ),
                enabled = isEnabled
            )
        }
    }
}

@ThemePreviews
@Composable
fun ScreenPreviewHaru() {
    // 샘플 프리셋 데이터
    val presets = remember { PreviewProvider.samplePresets }
    val mockUiState = TimerUiState(
        initialTime = 25.minutes,
        remainingTime = 25.minutes,
        progress = 0f,
        presets = presets
    )

    HaruFocusTheme {
        TimerScreen(
            onIntent = {},
            uiState = mockUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@ThemePreviews
@Composable
fun ScreenIdlePreviewHaru() {
    // 샘플 프리셋: UI 확인용 목업 데이터 (실제 데이터 구조로 기입)
    val presets = remember { PreviewProvider.samplePresets }
    // Idle 상태의 UI State 예시
    val idleUiState = TimerUiState(
        initialTime = 25.minutes,
        remainingTime = 25.minutes,
        progress = 0.5f,
        selectedPreset = presets.first(), // 첫번째 프리셋 선택
        presets = presets,
    )

    HaruFocusTheme {
        TimerScreen(
            onIntent = {}, // 미리보기: 인텐트 기본 처리
            uiState = idleUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@ThemePreviews
@Composable
fun ScreenCompletePreviewHaru() {
    // 샘플 프리셋: UI 확인용 목업 데이터 (실제 데이터 구조로 기입)
    val presets = remember { PreviewProvider.samplePresets }
    // Completed 상태의 UI State 예시
    val completedUiState = TimerUiState(
        initialTime = 25.minutes,
        status = TimerStatus.Overtime,
        overtime = 5.minutes,
        progress = 0f,
        selectedPreset = presets.first(), // 첫번째 프리셋 선택
        presets = presets,
    )

    HaruFocusTheme {
        TimerScreen(
            onIntent = {}, // 미리보기: 인텐트 기본 처리
            uiState = completedUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}
