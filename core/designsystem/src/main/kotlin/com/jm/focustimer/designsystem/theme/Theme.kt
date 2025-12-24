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
    // Primary - 브랜드 메인 컬러 (다크 모드에서는 밝은 Green 유지)
    primary = PrimaryGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PrimaryGreen,

    // Secondary - 보조 컬러
    secondary = DarkOnSurfaceVariant, // 업데이트: Deprecated된 DarkSecondaryText 대신 사용
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
    onSurfaceVariant = DarkOnSurfaceVariant, // 업데이트: 텍스트 가독성 최적화

    // Outline & Border
    outline = DarkBorder,
    outlineVariant = DarkSurfaceInactive,

    // Error (Material3 표준 적용)
    error = DarkError,
    onError = DarkOnError,
)

// 라이트 테마 ColorScheme
private val LightColorScheme = lightColorScheme(
    // Primary - 브랜드 메인 컬러 (라이트 모드 가독성 최적화)
    primary = PrimaryGreenLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenContainer,
    onPrimaryContainer = PrimaryGreenLight, // 컨테이너 위 텍스트는 짙은 색 사용

    // Secondary - 보조 컬러
    secondary = LightOnSurfaceVariant, // 업데이트: Deprecated된 LightSecondaryText 대신 사용
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
    surface = LightBackground, // Surface는 보통 Background와 같거나 약간 다름
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant, // 업데이트: 텍스트 가독성 최적화

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