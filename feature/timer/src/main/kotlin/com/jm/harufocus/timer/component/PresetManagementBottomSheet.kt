package com.jm.harufocus.timer.component

import TimeFormatter.formatDuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jm.harufocus.designsystem.icon.FocusTimerIcons
import com.jm.harufocus.designsystem.theme.FocusTimerTheme
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.usecase.preset.AddPresetUseCase
import com.jm.harufocus.timer.R
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 프리셋 관리 BottomSheet
 *
 * @param sheetState BottomSheet 상태
 * @param presets 프리셋 목록
 * @param canAddPreset 프리셋 추가 가능 여부
 * @param onDismiss BottomSheet 닫기 콜백
 * @param onPresetClick 프리셋 선택 콜백
 * @param onAddPreset 프리셋 추가 콜백
 * @param onEditPreset 프리셋 수정 콜백
 * @param onDeletePreset 프리셋 삭제 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetManagementBottomSheet(
    sheetState: SheetState,
    presets: List<Preset>,
    canAddPreset: Boolean,
    onDismiss: () -> Unit,
    onPresetClick: (Int) -> Unit,
    onAddPreset: () -> Unit,
    onEditPreset: (Preset) -> Unit,
    onDeletePreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.preset_management_bottom_sheet_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${presets.size}/${AddPresetUseCase.MAX_PRESET_COUNT}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 프리셋 목록
            if (presets.isEmpty()) {
                // 빈 상태
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = FocusTimerIcons.ListAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.preset_management_bottom_sheet_empty_preset),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = presets,
                        key = { it.id }
                    ) { preset ->
                        PresetManagementCard(
                            preset = preset,
                            onClick = { onPresetClick(preset.id) },
                            onEdit = { onEditPreset(preset) },
                            onDelete = { onDeletePreset(preset.id) }
                        )
                    }
                }
            }

            // 추가 버튼
            if (canAddPreset) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    onClick = onAddPreset,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FocusTimerIcons.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.preset_management_bottom_sheet_add_preset),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * 프리셋 관리 카드 (목록용)
 */
@Composable
private fun PresetManagementCard(
    preset: Preset,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프리셋 정보
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDuration(preset.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 액션 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = FocusTimerIcons.Edit,
                        contentDescription = stringResource(R.string.content_description_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = FocusTimerIcons.Delete,
                        contentDescription = stringResource(R.string.content_description_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PresetManagementBottomSheetPreview() {
    FocusTimerTheme {
        PresetManagementBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            presets = listOf(
                Preset(
                    id = 1,
                    name = "짧은 집중",
                    duration = 25.minutes,
                    colorIndex = 0,
                    createdAt = Instant.now()
                ),
                Preset(
                    id = 2,
                    name = "긴 집중",
                    duration = 50.minutes,
                    colorIndex = 1,
                    createdAt = Instant.now()
                ),
                Preset(
                    id = 3,
                    name = "휴식 시간",
                    duration = 5.minutes,
                    colorIndex = 2,
                    createdAt = Instant.now()
                )
            ),
            canAddPreset = true,
            onDismiss = {},
            onPresetClick = {},
            onAddPreset = {},
            onEditPreset = {},
            onDeletePreset = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PresetManagementBottomSheetEmptyPreview() {
    FocusTimerTheme {
        PresetManagementBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            presets = emptyList(),
            canAddPreset = true,
            onDismiss = {},
            onPresetClick = {},
            onAddPreset = {},
            onEditPreset = {},
            onDeletePreset = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PresetManagementBottomSheetFullPreview() {
    FocusTimerTheme {
        PresetManagementBottomSheet(
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            presets = List(10) { index ->
                Preset(
                    id = index,
                    name = "프리셋 ${index + 1}",
                    duration = (25 + index * 5).minutes,
                    colorIndex = index % 6,
                    createdAt = Instant.now()
                )
            },
            canAddPreset = false,
            onDismiss = {},
            onPresetClick = {},
            onAddPreset = {},
            onEditPreset = {},
            onDeletePreset = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetManagementCardPreview() {
    FocusTimerTheme {
        PresetManagementCard(
            preset = Preset(
                id = 1,
                name = "포모도로",
                duration = 25.minutes,
                colorIndex = 0,
                createdAt = Instant.now()
            ),
            onClick = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetManagementCardLongNamePreview() {
    FocusTimerTheme {
        PresetManagementCard(
            preset = Preset(
                id = 1,
                name = "매우 긴 프리셋 이름을 가진 포모도로 타이머 설정",
                duration = 50.minutes + 30.seconds,
                colorIndex = 0,
                createdAt = Instant.now()
            ),
            onClick = {},
            onEdit = {},
            onDelete = {}
        )
    }
}
