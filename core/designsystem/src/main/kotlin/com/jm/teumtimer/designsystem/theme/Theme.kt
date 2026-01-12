package com.jm.teumtimer.designsystem.theme

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

// 다크 테마 ColorScheme - Deep Focus (깊은 몰입)
val DarkColorScheme = darkColorScheme(
    // Primary - 깊은 몰입을 위한 민트
    primary = PrimaryMint,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryMintContainer,
    onPrimaryContainer = PrimaryMint,

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

// 라이트 테마 ColorScheme - Clear Day (선명한 하루)
val LightColorScheme = lightColorScheme(
    // Primary - 신뢰감을 주는 딥 틸
    primary = PrimaryDeepTeal,
    onPrimary = Color.White,
    primaryContainer = PrimaryDeepTealContainer,
    onPrimaryContainer = PrimaryDeepTeal, // 컨테이너 위 텍스트는 짙은 색 사용

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