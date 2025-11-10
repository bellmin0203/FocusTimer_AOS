package com.jm.focustimer.timer.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.icon.FocusTimerIcons
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.model.Preset
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 프리셋 섹션 - 저장된 프리셋 목록을 표시
 *
 * @param presets 프리셋 목록
 * @param selectedPresetId 현재 선택된 프리셋 ID
 * @param canAddPreset 프리셋 추가 가능 여부
 * @param onPresetClick 프리셋 선택 콜백
 * @param onAddPreset 프리셋 추가 버튼 클릭 콜백
 * @param onEditPreset 프리셋 수정 콜백
 * @param onDeletePreset 프리셋 삭제 콜백
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetSection(
    presets: List<Preset>,
    selectedPresetId: Int?,
    canAddPreset: Boolean,
    onPresetClick: (Int) -> Unit,
    onAddPreset: () -> Unit,
    onEditPreset: (Preset) -> Unit,
    onDeletePreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = presets.isNotEmpty() || canAddPreset,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 제목
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "프리셋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${presets.size}/10",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 프리셋 목록 (가로 스크롤)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 프리셋 추가 버튼
                if (canAddPreset) {
                    item {
                        AddPresetCard(
                            onClick = onAddPreset
                        )
                    }
                }

                // 프리셋 카드들
                items(
                    items = presets,
                    key = { it.id }
                ) { preset ->
                    PresetCard(
                        preset = preset,
                        isSelected = preset.id == selectedPresetId,
                        onClick = { onPresetClick(preset.id) },
                    )
                }
            }
        }
    }
}

/**
 * 프리셋 추가 카드
 */
@Composable
private fun AddPresetCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(120.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = FocusTimerIcons.Add,
                contentDescription = "프리셋 추가",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 프리셋 카드
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetCard(
    preset: Preset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .width(140.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        // 프리셋 이름
        // 수정/삭제 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = preset.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 시간 표시
            Text(
                text = formatDuration(preset.duration),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Duration을 "MM분" 또는 "MM분 SS초" 형식으로 포맷
 */
private fun formatDuration(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60

    return if (seconds == 0L) {
        "${minutes}분"
    } else {
        "${minutes}분 ${seconds}초"
    }
}

// Preview용 샘플 데이터
private val samplePresets = listOf(
    Preset(id = 1, name = "집중", duration = 25.minutes, createdAt = Instant.now()),
    Preset(id = 2, name = "짧은 휴식", duration = 5.minutes, createdAt = Instant.now()),
    Preset(id = 3, name = "긴 휴식", duration = 15.minutes, createdAt = Instant.now()),
    Preset(id = 4, name = "딥워크", duration = 50.minutes + 30.seconds, createdAt = Instant.now())
)

@Preview(showBackground = true)
@Composable
private fun PresetSectionPreview() {
    FocusTimerTheme {
        PresetSection(
            presets = samplePresets,
            selectedPresetId = 1,
            canAddPreset = true,
            onPresetClick = {},
            onAddPreset = {},
            onEditPreset = {},
            onDeletePreset = {},
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetSectionEmptyPreview() {
    FocusTimerTheme {
        PresetSection(
            presets = emptyList(),
            selectedPresetId = null,
            canAddPreset = true,
            onPresetClick = {},
            onAddPreset = {},
            onEditPreset = {},
            onDeletePreset = {},
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetSectionMaxPresetsPreview() {
    FocusTimerTheme {
        PresetSection(
            presets = List(10) { index ->
                Preset(
                    id = index,
                    name = "프리셋 ${index + 1}",
                    duration = (index + 1).minutes,
                    createdAt = Instant.now()
                )
            },
            selectedPresetId = 5,
            canAddPreset = false,
            onPresetClick = {},
            onAddPreset = {},
            onEditPreset = {},
            onDeletePreset = {},
            modifier = Modifier.padding(vertical = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPresetCardPreview() {
    FocusTimerTheme {
        AddPresetCard(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetCardPreview() {
    FocusTimerTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            PresetCard(
                preset = Preset(
                    id = 1,
                    name = "집중",
                    duration = 25.minutes,
                    createdAt = Instant.now()
                ),
                isSelected = false,
                onClick = {},
            )
            PresetCard(
                preset = Preset(
                    id = 2,
                    name = "짧은 휴식",
                    duration = 5.minutes,
                    createdAt = Instant.now()
                ),
                isSelected = true,
                onClick = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PresetCardLongNamePreview() {
    FocusTimerTheme {
        PresetCard(
            preset = Preset(
                id = 1,
                name = "매우 긴 프리셋 이름 테스트",
                duration = 50.minutes + 30.seconds,
                createdAt = Instant.now()
            ),
            isSelected = false,
            onClick = {},
        )
    }
}
