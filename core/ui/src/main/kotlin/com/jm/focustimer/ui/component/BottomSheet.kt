package com.jm.focustimer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.PresetAddCard
import com.jm.focustimer.designsystem.component.PresetCard
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.icon.FocusTimerIcons
import com.jm.focustimer.designsystem.theme.FocusTimerTheme

/**
 * Presets 하단 시트 (타이머 화면)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetsBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    presets: List<Preset>,
    onPresetClick: (Preset) -> Unit,
    onAddPreset: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        dragHandle = {
            // 커스텀 드래그 핸들
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { /* Edit mode */ }) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Preset 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    PresetCard(
                        title = preset.title,
                        duration = preset.duration.toString(),
                        unit = preset.unit,
                        onClick = { onPresetClick(preset) }
                    )
                }

                // 추가 버튼
                item {
                    PresetAddCard(
                        onClick = onAddPreset,
                        icon = FocusTimerIcons.Add
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Preset 데이터 클래스
 */
data class Preset(
    val id: Int,
    val title: String,
    val duration: Int,
    val unit: String
)

/**
 * Preview Parameter Provider - 다양한 Preset 상태
 */
private class PresetListProvider : PreviewParameterProvider<List<Preset>> {
    override val values = sequenceOf(
        // 기본 3개 프리셋
        listOf(
            Preset(1, "Focus", 25, "min"),
            Preset(2, "Short Break", 5, "min"),
            Preset(3, "Long Break", 15, "min")
        ),
        // 많은 프리셋
        listOf(
            Preset(1, "Focus", 25, "min"),
            Preset(2, "Short Break", 5, "min"),
            Preset(3, "Long Break", 15, "min"),
            Preset(4, "Deep Work", 90, "min"),
            Preset(5, "Quick Break", 2, "min"),
            Preset(6, "Pomodoro", 25, "min")
        ),
        // 빈 리스트
        emptyList()
    )
}

/**
 * 기본 Presets Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun PresetsBottomSheetPreview() {
    FocusTimerTheme() {
        PresetsBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = {},
            presets = listOf(
                Preset(1, "Focus", 25, "min"),
                Preset(2, "Short Break", 5, "min"),
                Preset(3, "Long Break", 15, "min")
            ),
            onPresetClick = {},
            onAddPreset = {}
        )
    }
}

/**
 * 많은 Presets - 스크롤 가능
 */
@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun PresetsBottomSheetManyPresetsPreview() {
    FocusTimerTheme {
        PresetsBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = {},
            presets = listOf(
                Preset(1, "Focus", 25, "min"),
                Preset(2, "Short Break", 5, "min"),
                Preset(3, "Long Break", 15, "min"),
                Preset(4, "Deep Work", 90, "min"),
                Preset(5, "Quick Break", 2, "min"),
                Preset(6, "Pomodoro", 25, "min"),
                Preset(7, "Study", 50, "min"),
                Preset(8, "Rest", 10, "min")
            ),
            onPresetClick = {},
            onAddPreset = {}
        )
    }
}

/**
 * 빈 Presets (추가 버튼만)
 */
@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun PresetsBottomSheetEmptyPreview() {
    FocusTimerTheme {
        PresetsBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = {},
            presets = emptyList(),
            onPresetClick = {},
            onAddPreset = {}
        )
    }
}

/**
 * 커스텀 Presets (다양한 시간 단위)
 */
@OptIn(ExperimentalMaterial3Api::class)
@ThemePreviews
@Composable
private fun PresetsBottomSheetCustomPreview() {
    FocusTimerTheme {
        PresetsBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = {},
            presets = listOf(
                Preset(1, "Ultra Focus", 120, "min"),
                Preset(2, "Micro Break", 1, "min"),
                Preset(3, "Power Nap", 20, "min"),
                Preset(4, "Sprint", 15, "min"),
                Preset(5, "Marathon", 180, "min")
            ),
            onPresetClick = {},
            onAddPreset = {}
        )
    }
}

/**
 * PreviewParameter를 사용한 다양한 상태 테스트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Presets with Parameter",
    showBackground = true,
    group = "Parameter Tests"
)
@Composable
private fun PresetsBottomSheetParameterPreview(
    @PreviewParameter(PresetListProvider::class) presets: List<Preset>
) {
    FocusTimerTheme {
        PresetsBottomSheet(
            sheetState = rememberModalBottomSheetState(),
            onDismissRequest = {},
            presets = presets,
            onPresetClick = {},
            onAddPreset = {}
        )
    }
}
