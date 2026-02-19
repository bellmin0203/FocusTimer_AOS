package com.jm.harufocus.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.designsystem.component.FocusIconButton
import com.jm.harufocus.designsystem.component.TimerColorScheme
import com.jm.harufocus.designsystem.icon.HaruFocusIcons
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.timer.R
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.util.getPresetColorScheme

/**
 * 타이머 컨트롤 버튼들을 담당하는 섹션 컴포넌트
 *
 * @param uiState 현재 타이머 UI 상태
 * @param selectedPresetColorScheme 선택된 프리셋의 색상 정보
 * @param onIntent 타이머 인텐트 처리 함수
 * @param useVerticalLayout 버튼들을 수직으로 배치할지 여부 (가로 모드 대응)
 * @param modifier Modifier
 */
@Composable
fun TimerControlsSection(
    uiState: TimerUiState,
    selectedPresetColorScheme: TimerColorScheme,
    onIntent: (TimerIntent) -> Unit,
    modifier: Modifier = Modifier,
    useVerticalLayout: Boolean = false
) {
    if (useVerticalLayout) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimerControlButtons(uiState, selectedPresetColorScheme, onIntent)
        }
    } else {
        Row(
            modifier = modifier.padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimerControlButtons(uiState, selectedPresetColorScheme, onIntent)
        }
    }
}

/**
 * 실제 타이머 컨트롤 버튼들의 집합
 * 레이아웃(Row/Column)과 무관하게 버튼 로직만 포함
 */
@Composable
internal fun TimerControlButtons(
    uiState: TimerUiState,
    selectedPresetColorScheme: TimerColorScheme,
    onIntent: (TimerIntent) -> Unit
) {
    val context = LocalContext.current

    // 완료 상태일 때는 완료 버튼 표시
    if (uiState.isCompleted) {
        FocusIconButton(
            onClick = {
                // 타이머 완료 처리 (Snackbar 표시 및 서비스 완료 요청)
                onIntent(TimerIntent.Complete)
            },
            icon = HaruFocusIcons.Check,
            contentDescription = stringResource(R.string.content_description_complete),
            containerColor = selectedPresetColorScheme.progressColor,
            contentColor = Color.White
        )
    } else {
        // 재생/일시정지 버튼
        FocusIconButton(
            onClick = {
                when {
                    uiState.isPaused -> {
                        // 타이머 재개 (Paused 상태를 먼저 체크해야 함)
                        onIntent(TimerIntent.Resume)
                    }

                    uiState.isIdle -> {
                        // 타이머 시작 (Idle 상태에서만)
                        onIntent(TimerIntent.Start())
                    }

                    else -> {
                        // 타이머 일시정지
                        onIntent(TimerIntent.Pause)
                    }
                }
            },
            icon = if (uiState.isRunning) HaruFocusIcons.Pause else HaruFocusIcons.PlayArrow,
            contentDescription = if (uiState.isRunning) {
                stringResource(R.string.content_description_pause)
            } else {
                stringResource(R.string.content_description_play)
            },
            containerColor = selectedPresetColorScheme.progressColor,
            contentColor = Color.White
        )

        if (uiState.isTimerActive) {
            // 리셋 버튼
            FocusIconButton(
                onClick = {
                    // 타이머 정지
                    onIntent(TimerIntent.Stop)
                },
                icon = HaruFocusIcons.RestartAlt,
                contentDescription = stringResource(R.string.content_description_reset)
            )
        }
    }
}

/**
 * 프리셋 목록을 가로로 표시하는 컴포넌트
 *
 * @param presets 프리셋 목록
 * @param selectedPresetId 현재 선택된 프리셋 ID
 * @param isEnabled 활성화 여부
 * @param onPresetClick 프리셋 클릭 시 호출되는 콜백
 * @param onManageClick 관리 버튼 클릭 시 호출되는 콜백
 * @param modifier Modifier
 */
@Composable
fun PresetListRow(
    presets: List<Preset>,
    selectedPresetId: Int?,
    isEnabled: Boolean,
    themeMode: ThemeMode,
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
            // 선택된 프리셋의 색상 테마 가져오기 (안전한 범위 검증 적용)
            val presetColor = getPresetColorScheme(
                colorIndex = preset.colorIndex,
                themeMode = themeMode,
            )

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
