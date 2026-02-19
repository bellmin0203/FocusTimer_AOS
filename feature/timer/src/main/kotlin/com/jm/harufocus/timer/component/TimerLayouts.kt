package com.jm.harufocus.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jm.harufocus.designsystem.component.CircularTimerProgress
import com.jm.harufocus.designsystem.component.TimerColorScheme
import com.jm.harufocus.timer.R
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerUiState

/**
 * 가로 모드(Landscape)용 타이머 레이아웃
 *
 * @param uiState 타이머 UI 상태
 * @param presetColors 선택된 프리셋 색상
 * @param shouldShowUiControls UI 컨트롤 표시 여부
 * @param onIntent 타이머 인텐트 처리 함수
 * @param onTimeClick 시간 영역 클릭 시 콜백
 * @param onPresetManagementClick 프리셋 관리 클릭 시 콜백
 * @param modifier Modifier
 */
@Composable
fun LandscapeTimerLayout(
    uiState: TimerUiState,
    presetColors: TimerColorScheme,
    shouldShowUiControls: Boolean,
    onIntent: (TimerIntent) -> Unit,
    onTimeClick: () -> Unit,
    onPresetManagementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 원형 타이머 (좌측 배치, weight로 공간 차지)
        CircularTimerProgress(
            progress = uiState.progress,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isTimerActive,
            progressColor = if (uiState.isCompleted) {
                MaterialTheme.colorScheme.tertiary
            } else {
                presetColors.progressColor
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
            TimerCenterContent(
                formattedTime = uiState.formattedTime,
                isCompleted = uiState.isCompleted,
                isTimerActive = uiState.isTimerActive,
                onTimeClick = onTimeClick
            )
        }

        // 우측 패널 (프리셋 선택 + 컨트롤 버튼)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프리셋 선택 AssistChip
            if (uiState.isIdle && shouldShowUiControls) {
                AssistChip(
                    onClick = onPresetManagementClick,
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

            // 우측 컨트롤 영역
            if (shouldShowUiControls) {
                TimerControlsSection(
                    uiState = uiState,
                    selectedPresetColorScheme = presetColors,
                    onIntent = onIntent,
                    useVerticalLayout = true
                )
            }
        }
    }
}

/**
 * 세로 모드(Portrait)용 타이머 레이아웃
 *
 * @param uiState 타이머 UI 상태
 * @param presetColors 선택된 프리셋 색상
 * @param shouldShowUiControls UI 컨트롤 표시 여부
 * @param onIntent 타이머 인텐트 처리 함수
 * @param onTimeClick 시간 영역 클릭 시 콜백
 * @param onPresetManagementClick 프리셋 관리 클릭 시 콜백
 * @param modifier Modifier
 */
@Composable
fun PortraitTimerLayout(
    uiState: TimerUiState,
    presetColors: TimerColorScheme,
    shouldShowUiControls: Boolean,
    onIntent: (TimerIntent) -> Unit,
    onTimeClick: () -> Unit,
    onPresetManagementClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 원형 타이머
        CircularTimerProgress(
            progress = uiState.progress,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isTimerActive,
            progressColor = if (uiState.isCompleted) {
                MaterialTheme.colorScheme.tertiary
            } else {
                presetColors.progressColor
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
            TimerCenterContent(
                formattedTime = uiState.formattedTime,
                isCompleted = uiState.isCompleted,
                isTimerActive = uiState.isTimerActive,
                onTimeClick = onTimeClick
            )
        }

        // 프리셋 선택 (타이머 다이얼 바로 아래)
        if (uiState.isIdle && shouldShowUiControls) {
            PresetListRow(
                presets = uiState.presets,
                selectedPresetId = uiState.selectedPreset?.id,
                isEnabled = uiState.isIdle,
                themeMode = uiState.themeMode,
                onPresetClick = { presetId ->
                    onIntent(TimerIntent.SelectPreset(presetId))
                },
                onManageClick = onPresetManagementClick,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 하단 컨트롤 영역
        if (shouldShowUiControls) {
            TimerControlsSection(
                uiState = uiState,
                selectedPresetColorScheme = presetColors,
                onIntent = onIntent,
                useVerticalLayout = false
            )
        }
    }
}
