package com.jm.harufocus.widget.theme

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import com.jm.harufocus.designsystem.theme.DarkColorScheme
import com.jm.harufocus.designsystem.theme.LightColorScheme

/**
 * HaruFocus 앱의 디자인 시스템을 Glance 위젯에 적용하기 위한 테마 래퍼
 *
 * 앱의 [LightColorScheme]과 [DarkColorScheme]을 [ColorProviders]로 변환하여 사용합니다.
 * 이를 통해 위젯에서도 앱과 동일한 컬러 팔레트를 유지할 수 있습니다.
 */
object HaruFocusGlanceTheme {
    val colors = ColorProviders(
        light = LightColorScheme,
        dark = DarkColorScheme
    )
}

@Composable
fun HaruFocusGlanceTheme(
    content: @Composable () -> Unit
) {
    GlanceTheme(
        colors = HaruFocusGlanceTheme.colors,
        content = content
    )
}
