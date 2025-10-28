package com.jm.focustimer.timer

import TimeFormatter
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jm.focustimer.designsystem.component.CircularTimerProgress
import com.jm.focustimer.designsystem.component.FocusIconButton
import com.jm.focustimer.designsystem.component.FocusPrimaryButton
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.icon.FocusTimerIcons
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.timer.model.HapticPattern
import com.jm.focustimer.timer.model.TimerIntent
import com.jm.focustimer.timer.model.TimerSideEffect
import com.jm.focustimer.timer.model.TimerUiState
import com.jm.focustimer.ui.component.Preset
import com.jm.focustimer.ui.component.PresetsBottomSheet
import com.jm.focustimer.ui.component.TimerTopBar
import com.jm.focustimer.ui.util.PreviewProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun TimerScreen(
    onSettingsClick: () -> Unit = {},
    presets: List<Preset> = emptyList(),
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val view = LocalView.current

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect -> 
            when (effect) {
                is TimerSideEffect.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = effect.message,
                            withDismissAction = true
                        )
                    }
                }
                is TimerSideEffect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }
                is TimerSideEffect.ShowTimerCompleted -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("타이머가 완료되었습니다! \uD83C\uDF89")
                    }
                }
                is TimerSideEffect.ShowReminder -> {
                    val message =
                        "${TimeFormatter.formatDuration(effect.remainingSeconds)} 남았습니다."
                    scope.launch {
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
        presets = presets,
        onIntent = viewModel::onIntent,
        onSettingsClick = onSettingsClick
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
    presets: List<Preset> = emptyList(),
    onIntent: (TimerIntent) -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var showPresets by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TimerTopBar()
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
            Spacer(modifier = Modifier.height(32.dp))

            // 원형 타이머
            CircularTimerProgress(
                progress = uiState.progress,
                modifier = Modifier.size(320.dp),
                enabled = !uiState.isActive, // 타이머가 유휴 상태일 때만 드래그 가능
                onProgressChange = { newProgress ->
                    onIntent(TimerIntent.DragProgress(newProgress))
                }
            ) {
                Column(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${uiState.timeInSeconds / 60}",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "minutes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    // 재생/일시정지 버튼
                    FocusIconButton(
                        onClick = {
                            if (!uiState.isRunning) onIntent(TimerIntent.Start)
                            else if (uiState.isPaused) onIntent(TimerIntent.Resume)
                            else onIntent(TimerIntent.Pause)
                        },
                        icon = if (uiState.isRunning) FocusTimerIcons.Pause else FocusTimerIcons.PlayArrow,
                        contentDescription = if (uiState.isRunning) "Pause" else "Play",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )

                    // 리셋 버튼
                    FocusIconButton(
                        onClick = {
                            onIntent(TimerIntent.Stop)
                        },
                        icon = FocusTimerIcons.RestartAlt,
                        contentDescription = "Reset"
                    )
                }

                // 프리셋 & 설정 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 프리셋 버튼
                    FocusPrimaryButton(
                        text = "Presets",
                        onClick = {
                            scope.launch {
                                showPresets = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // 설정 버튼
                    FocusPrimaryButton(
                        text = "Settings",
                        onClick = onSettingsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 프리셋 Bottom Sheet
        if (showPresets) {
            PresetsBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                        showPresets = false
                    }
                },
                presets = presets,
                onPresetClick = { preset ->
                    val timeInMillis = preset.duration * 60 * 1000L
                    onIntent(TimerIntent.SetTime(timeInMillis))
                    scope.launch {
                        sheetState.hide()
                        showPresets = false
                    }
                },
                onAddPreset = {
                    // TODO: 프리셋 추가 화면으로 이동
                    scope.launch {
                        sheetState.hide()
                        showPresets = false
                    }
                }
            )
        }
    }
}

@ThemePreviews
@Composable
fun TimerScreenPreview() {
    // 샘플 프리셋 데이터
    val presets = remember { PreviewProvider.samplePresets }
    val mockUiState = TimerUiState(
        timeInSeconds = 1500, // 25 minutes
        isRunning = false,
        progress = 0f
    )

    FocusTimerTheme {
        TimerScreen(
            onIntent = {},
            presets = presets,
            uiState = mockUiState,
        )
    }
}