package com.jm.harufocus.timer.util

import androidx.compose.runtime.Composable
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.designsystem.component.TimerColorPresets
import com.jm.harufocus.designsystem.component.TimerColorScheme

/**
 * colorIndex에 해당하는 TimerColorScheme을 안전하게 가져옵니다.
 *
 * 잘못된 인덱스(음수, 범위 초과, null)가 전달되면 기본 색상(인덱스 0)을 반환합니다.
 * 테마(Light/Dark)에 따라 적절한 색상 프리셋을 자동으로 선택합니다.
 *
 * @param colorIndex 프리셋 색상 인덱스 (nullable)
 * @return 해당 인덱스의 TimerColorScheme 또는 기본 색상
 */
@Composable
fun getPresetColorScheme(colorIndex: Int?, themeMode: ThemeMode): TimerColorScheme {
    val presets = TimerColorPresets.presetColors(themeMode)
    val safeIndex = (colorIndex ?: 0).coerceIn(0, presets.lastIndex)
    return presets[safeIndex]
}
