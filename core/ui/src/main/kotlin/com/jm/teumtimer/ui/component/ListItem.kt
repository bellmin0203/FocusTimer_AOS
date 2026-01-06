package com.jm.teumtimer.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.jm.teumtimer.designsystem.component.FocusSwitch
import com.jm.teumtimer.designsystem.component.ThemePreviews
import com.jm.teumtimer.designsystem.icon.FocusTimerIcons
import com.jm.teumtimer.designsystem.theme.FocusTimerTheme

/**
 * 기본 설정 항목
 */
@Composable
fun SettingsItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailing != null) {
            Spacer(Modifier.size(14.dp))
            trailing()
        }
    }
}

@ThemePreviews
@Composable
fun SettingsItemPreview() {
    FocusTimerTheme {
        SettingsItem(
            title = "설정 항목",
            subtitle = "설정 항목 부제목",
            onClick = {},
            trailing = { Text("Trailing") }
        )
    }
}

/**
 * 스위치가 있는 설정 항목
 */
@Composable
fun SettingsSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        trailing = {
            FocusSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    )
}

@ThemePreviews
@Composable
fun SettingsSwitchItemPreview() {
    FocusTimerTheme {
        SettingsSwitchItem(
            title = "스위치 설정",
            checked = true,
            onCheckedChange = {}
        )
    }
}

/**
 * 클릭 가능한 설정 항목 (화살표 아이콘 포함)
 */
@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    icon: ImageVector? = null
) {
    SettingsItem(
        title = title,
        subtitle = subtitle,
        onClick = onClick,
        modifier = modifier,
        trailing = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )
}

@ThemePreviews
@Composable
fun SettingsClickableItemPreview() {
    FocusTimerTheme {
        SettingsClickableItem(
            title = "클릭 가능한 설정",
            subtitle = "부제목설정 항목 부제목설정 항목 부제목설정 항목 부제목설정 항목 부제목설정 항목 부제목",
            onClick = {},
            value = "값",
            icon = FocusTimerIcons.KeyboardArrowRight
        )
    }
}

/**
 * 설정 섹션 헤더
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@ThemePreviews
@Composable
fun SettingsSectionHeaderPreview() {
    FocusTimerTheme {
        SettingsSectionHeader(title = "섹션 헤더")
    }
}

/**
 * 설정 항목 구분선
 */
@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outlineVariant
    )
}
