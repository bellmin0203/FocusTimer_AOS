package com.jm.focustimer.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// 다크 테마 ColorScheme
private val DarkColorScheme = darkColorScheme(
    // Primary - 브랜드 메인 컬러
    primary = PrimaryGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PrimaryGreen,

    // Secondary - 보조 컬러
    secondary = DarkSecondaryText,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkSecondaryText,

    // Tertiary - 3차 컬러 (Break 아이콘용)
    tertiary = BreakIconDark,
    onTertiary = DarkBackground,
    tertiaryContainer = BreakIconDarkBackground,
    onTertiaryContainer = BreakIconDark,

    // Background & Surface
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkSecondaryText,

    // Outline & Border
    outline = DarkBorder,
    outlineVariant = DarkSurfaceInactive,

    // Error (Material3 기본값 유지)
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
)

// 라이트 테마 ColorScheme
private val LightColorScheme = lightColorScheme(
    // Primary - 브랜드 메인 컬러
    primary = PrimaryGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = PrimaryGreen,

    // Secondary - 보조 컬러
    secondary = LightSecondaryText,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightSecondaryText,

    // Tertiary - 3차 컬러 (Break 아이콘용)
    tertiary = BreakIconLight,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = BreakIconLightBackground,
    onTertiaryContainer = BreakIconLight,

    // Background & Surface
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightBackground,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurface,
    onSurfaceVariant = LightSecondaryText,

    // Outline & Border
    outline = LightBorder,
    outlineVariant = LightBorderVariant,

    // Error (Material3 기본값 유지)
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
)

@Composable
fun FocusTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 디자인 시스템과 맞지 않으므로 기본값을 false로 설정
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}