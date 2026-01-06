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

// 다크 테마 ColorScheme - Midnight Gap (심야의 틈)
val DarkColorScheme = darkColorScheme(
    // Primary - 어둠 속에서 빛나는 밝은 옐로우
    primary = PrimaryYellow,
    onPrimary = DarkBackground, // 옐로우 위의 텍스트는 어두운 배경색
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PrimaryYellow,

    // Secondary - 보조 컬러
    secondary = DarkOnSurfaceVariant,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkOnSurfaceVariant,

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
    onSurfaceVariant = DarkOnSurfaceVariant,

    // Outline & Border
    outline = DarkBorder,
    outlineVariant = DarkSurfaceInactive,

    // Error (Material3 표준 적용)
    error = DarkError,
    onError = DarkOnError,
)

// 라이트 테마 ColorScheme - Morning Ray (새벽의 빛)
val LightColorScheme = lightColorScheme(
    // Primary - 새벽의 따뜻한 햇살 (오렌지)
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeContainer,
    onPrimaryContainer = PrimaryOrange, // 컨테이너 위 텍스트는 짙은 색 사용

    // Secondary - 보조 컬러
    secondary = LightOnSurfaceVariant,
    onSecondary = Color.White,
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightOnSurfaceVariant,

    // Tertiary - 3차 컬러 (Break 아이콘용)
    tertiary = BreakIconLight,
    onTertiary = Color.White,
    tertiaryContainer = BreakIconLightBackground,
    onTertiaryContainer = BreakIconLight,

    // Background & Surface
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface, // 화이트 카드
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,

    // Outline & Border
    outline = LightBorder,
    outlineVariant = LightBorderVariant,

    // Error (Material3 표준 적용)
    error = LightError,
    onError = LightOnError,
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