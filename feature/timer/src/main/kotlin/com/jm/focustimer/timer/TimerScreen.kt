package com.jm.focustimer.timer

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jm.focustimer.designsystem.component.CircularTimerProgress
import com.jm.focustimer.designsystem.component.FocusIconButton
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.icon.FocusTimerIcons
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.designsystem.theme.TimerColorPresets
import com.jm.focustimer.domain.model.Preset
import com.jm.focustimer.timer.component.AddPresetDialog
import com.jm.focustimer.timer.component.DeletePresetDialog
import com.jm.focustimer.timer.component.EditPresetDialog
import com.jm.focustimer.timer.component.NavigationDrawerContent
import com.jm.focustimer.timer.component.PresetManagementBottomSheet
import com.jm.focustimer.timer.component.PresetSection
import com.jm.focustimer.timer.component.TimeInputBottomSheet
import com.jm.focustimer.timer.model.HapticPattern
import com.jm.focustimer.timer.model.TimerIntent
import com.jm.focustimer.timer.model.TimerSideEffect
import com.jm.focustimer.timer.model.TimerUiState
import com.jm.focustimer.timer.model.toUiText
import com.jm.focustimer.ui.component.TimerTopBar
import com.jm.focustimer.ui.component.rememberPickerState
import com.jm.focustimer.ui.util.PreviewProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun TimerScreen(
    onSettingsClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)

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
                    val feedbackConstant = when (effect.pattern) {
                        HapticPattern.TICK -> HapticFeedbackConstants.CLOCK_TICK
                        HapticPattern.COMPLETED -> HapticFeedbackConstants.LONG_PRESS
                        HapticPattern.REMINDER -> HapticFeedbackConstants.CONTEXT_CLICK
                    }
                    view.performHapticFeedback(feedbackConstant)
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
    val presetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showPresets by remember { mutableStateOf(false) }
    var showTimeInput by remember { mutableStateOf(false) }

    // 프리셋 다이얼로그 상태
    var showAddPresetDialog by remember { mutableStateOf(false) }
    var showEditPresetDialog by remember { mutableStateOf<Preset?>(null) }
    var showDeletePresetDialog by remember { mutableStateOf<Preset?>(null) }

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
            topBar = {
                TimerTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // 선택된 프리셋의 색상 가져오기
                val selectedPreset = uiState.presets.find { it.id == uiState.selectedPresetId }
                val presetColors = selectedPreset?.let { preset ->
                    TimerColorPresets.lightPresets.getOrNull(preset.colorIndex)
                } ?: TimerColorPresets.lightPresets[0]

                if (uiState.isIdle) {
                    PresetSection(
                        presets = uiState.presets,
                        selectedPresetId = uiState.selectedPresetId,
                        canAddPreset = uiState.canAddPreset,
                        onPresetClick = { presetId ->
                            onIntent(TimerIntent.SelectPreset(presetId))
                        },
                        onAddPreset = { showAddPresetDialog = true },
                        onEditPreset = { showEditPresetDialog = it },
                        onDeletePreset = { showDeletePresetDialog = it },
                    )
                }

                // 원형 타이머
                CircularTimerProgress(
                    progress = uiState.progress,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isActive, // 타이머가 유휴 상태일 때만 드래그 가능
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
                    }
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
                            .clickable(enabled = !uiState.isActive) {
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

                // 하단 컨트롤 영역
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    // 타이머 컨트롤 버튼
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 완료 상태일 때는 완료 버튼 표시
                        if (uiState.isCompleted) {
                            FocusIconButton(
                                onClick = {
                                    onIntent(TimerIntent.Complete)
                                },
                                icon = FocusTimerIcons.Check,
                                contentDescription = "Complete",
                                containerColor = presetColors.progressColor,
                                contentColor = Color.White
                            )
                        } else {
                            // 재생/일시정지 버튼
                            FocusIconButton(
                                onClick = {
                                    if (!uiState.isRunning) onIntent(TimerIntent.Start())
                                    else if (uiState.isPaused) onIntent(TimerIntent.Resume)
                                    else onIntent(TimerIntent.Pause)
                                },
                                icon = if (uiState.isRunning) FocusTimerIcons.Pause else FocusTimerIcons.PlayArrow,
                                contentDescription = if (uiState.isRunning) "Pause" else "Play",
                                containerColor = presetColors.progressColor,
                                contentColor = Color.White
                            )

                            if (uiState.isActive) {
                                // 리셋 버튼
                                FocusIconButton(
                                    onClick = {
                                        onIntent(TimerIntent.Stop)
                                    },
                                    icon = FocusTimerIcons.RestartAlt,
                                    contentDescription = "Reset"
                                )
                            }
                        }
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
                    canAddPreset = uiState.canAddPreset,
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

@ThemePreviews
@Composable
fun TimerScreenPreview() {
    // 샘플 프리셋 데이터
    val presets = remember { PreviewProvider.samplePresets }
    val mockUiState = TimerUiState(
        initialTime = 25.minutes,
        remainingTime = 25.minutes,
        progress = 0f,
        presets = presets
    )

    FocusTimerTheme {
        TimerScreen(
            onIntent = {},
            uiState = mockUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@ThemePreviews
@Composable
fun TimerScreenIdlePreview() {
    // 샘플 프리셋: UI 확인용 목업 데이터 (실제 데이터 구조로 기입)
    val presets = remember { PreviewProvider.samplePresets }
    // Idle 상태의 UI State 예시
    val idleUiState = TimerUiState(
        initialTime = 25.minutes,
        remainingTime = 25.minutes,
        progress = 0.5f,
        selectedPresetId = presets.first().id, // 첫번째 프리셋 선택
        presets = presets,
    )

    FocusTimerTheme {
        TimerScreen(
            onIntent = {}, // 미리보기: 인텐트 기본 처리
            uiState = idleUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}

@ThemePreviews
@Composable
fun TimerScreenCompletePreview() {
    // 샘플 프리셋: UI 확인용 목업 데이터 (실제 데이터 구조로 기입)
    val presets = remember { PreviewProvider.samplePresets }
    // Idle 상태의 UI State 예시
    val idleUiState = TimerUiState(
        initialTime = 25.minutes,
        isCompleted = true,
        overtime = 5.minutes,
        progress = 0f,
        selectedPresetId = presets.first().id, // 첫번째 프리셋 선택
        presets = presets,
    )

    FocusTimerTheme {
        TimerScreen(
            onIntent = {}, // 미리보기: 인텐트 기본 처리
            uiState = idleUiState,
            drawerState = rememberDrawerState(DrawerValue.Closed)
        )
    }
}