package com.jm.harufocus.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jm.harufocus.designsystem.theme.HaruFocusTheme

/**
 * 하루 몰입 스위치
 * 설정 화면의 토글 스위치
 */
@Composable
fun FocusSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.background,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.background,
            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@ThemePreviews
@Composable
fun HaruFocusSwitchPreview() {
    HaruFocusTheme {
        FocusSwitch(checked = true, onCheckedChange = {})
    }
}
