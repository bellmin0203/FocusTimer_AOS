package com.jm.harufocus.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jm.harufocus.designsystem.icon.HaruFocusIcons
import com.jm.harufocus.designsystem.theme.HaruFocusTheme

/**
 * 통계 카드 - 통계 화면의 요약 카드
 */
@Composable
fun StatisticsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@ThemePreviews
@Composable
fun StatisticsCardPreview() {
    HaruFocusTheme {
        StatisticsCard(
            title = "오늘 집중한 시간",
            value = "3h 25m"
        )
    }
}

/**
 * Preset 카드 - 타이머 설정 카드 (25 min, 5 min, 15 min)
 */
@Composable
fun PresetCard(
    title: String,
    duration: String,
    unit: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = duration,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@ThemePreviews
@Composable
fun PresetCardPreview() {
    HaruFocusTheme {
        PresetCard(
            title = "포모도로",
            duration = "25",
            unit = "min",
            onClick = {}
        )
    }
}

/**
 * Preset 추가 카드 - 새 프리셋 추가 버튼
 */
@Composable
fun PresetAddCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Add Preset",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
        )
    }
}

@ThemePreviews
@Composable
fun PresetAddCardPreview() {
    HaruFocusTheme {
        PresetAddCard(
            onClick = {},
            icon = HaruFocusIcons.Add
        )
    }
}

/**
 * 세션 카드 - 세션 리스트 아이템
 */
@Composable
fun SessionCard(
    icon: ImageVector,
    iconColor: Color,
    iconBackgroundColor: Color,
    title: String,
    duration: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아이콘
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }

        // 제목과 시간
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = duration,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 시간
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 화살표 아이콘
        if (trailingIcon != null) {
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@ThemePreviews
@Composable
fun SessionCardPreview() {
    HaruFocusTheme {
        SessionCard(
            icon = HaruFocusIcons.Timer,
            iconColor = MaterialTheme.colorScheme.primary,
            iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            title = "집중",
            duration = "25분",
            time = "오후 2:30",
            onClick = {}
        )
    }
}

@ThemePreviews
@Composable
fun SessionCardWithTrailingIconPreview() {
    HaruFocusTheme {
        SessionCard(
            icon = HaruFocusIcons.Timer,
            iconColor = MaterialTheme.colorScheme.primary,
            iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
            title = "집중",
            duration = "25분",
            time = "오후 2:30",
            onClick = { /*TODO*/ },
            trailingIcon = HaruFocusIcons.KeyboardArrowRight
        )
    }
}
