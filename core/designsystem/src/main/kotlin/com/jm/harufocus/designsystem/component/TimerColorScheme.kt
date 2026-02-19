package com.jm.harufocus.designsystem.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.designsystem.theme.BrandNavy
import com.jm.harufocus.designsystem.theme.BrandPurple
import com.jm.harufocus.designsystem.theme.BrandYellow
import com.jm.harufocus.designsystem.theme.DarkBorder
import com.jm.harufocus.designsystem.theme.LightPrimaryContainer

/**
 * 타이머 컴포넌트의 컬러 스킴
 *
 * @property progressColor 프로그레스 원 색상
 * @property knobColor 중앙 knob 색상
 * @property tickColor 눈금 색상
 * @property labelColor 라벨 텍스트 색상
 */
data class TimerColorScheme(
    val progressColor: Color,
    val knobColor: Color,
    val tickColor: Color,
    val labelColor: Color
)

/**
 * 타이머 컬러 프리셋
 */
object TimerColorPresets {
    // Light Theme Presets

    /**
     * 하루(Haru) 시그니처 테마 (Navy & Purple)
     * 브랜드 아이덴티티를 살려 깊은 Navy를 메인으로 사용
     */
    val HaruLight = TimerColorScheme(
        progressColor = BrandNavy,        // Primary (#252A4E)
        knobColor = Color.White,
        tickColor = LightPrimaryContainer, // 연한 Navy
        labelColor = BrandNavy
    )

    /**
     * 코랄 핑크 테마
     */
    val CoralLight = TimerColorScheme(
        progressColor = Color(0xFFF43F5E), // rose-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFFFE4E6), // rose-100
        labelColor = Color(0xFFE11D48) // rose-600
    )

    /**
     * 퍼플 테마 (브랜드 컬러 Purple 활용)
     */
    val PurpleLight = TimerColorScheme(
        progressColor = BrandPurple,      // Secondary (#5A4A72)
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFF3E8FF), // purple-100
        labelColor = BrandPurple
    )

    /**
     * 스카이 블루 테마
     */
    val SkyLight = TimerColorScheme(
        progressColor = Color(0xFF0EA5E9), // sky-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFE0F2FE), // sky-100
        labelColor = Color(0xFF0284C7) // sky-600
    )

    /**
     * 슬레이트 그레이 테마
     */
    val SlateLight = TimerColorScheme(
        progressColor = Color(0xFF475569), // slate-600
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFF1F5F9), // slate-100
        labelColor = Color(0xFF334155) // slate-700
    )

    /**
     * 포레스트 그린 테마 (자연의 색)
     */
    val GreenLight = TimerColorScheme(
        progressColor = Color(0xFF059669), // emerald-600
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFD1FAE5), // emerald-100
        labelColor = Color(0xFF047857) // emerald-700
    )

    // Dark Theme Presets

    /**
     * 하루(Haru) 시그니처 테마 (Yellow on Navy)
     * 다크 모드에서는 네이비 배경 위 옐로우 포인트로 브랜드 강조
     */
    val HaruDark = TimerColorScheme(
        progressColor = BrandYellow,      // Primary (#FAD79C)
        knobColor = Color.White,
        tickColor = DarkBorder,
        labelColor = BrandYellow
    )

    /**
     * 핫 핑크 테마
     */
    val CoralDark = TimerColorScheme(
        progressColor = Color(0xFFFB7185), // rose-400
        knobColor = Color(0xFF1A231F),
        tickColor = Color(0xFF881337), // rose-900
        labelColor = Color(0xFFFDA4AF) // rose-300
    )

    /**
     * 퍼플 테마
     */
    val PurpleDark = TimerColorScheme(
        progressColor = Color(0xFFA78BFA), // violet-400
        knobColor = Color(0xFF1A231F),
        tickColor = Color(0xFF4C1D95), // violet-900
        labelColor = Color(0xFFDDD6FE) // violet-200
    )

    /**
     * 아쿠아 블루 테마
     */
    val SkyDark = TimerColorScheme(
        progressColor = Color(0xFF38BDF8), // sky-400
        knobColor = Color(0xFF0F172A), // slate-900
        tickColor = Color(0xFF0C4A6E), // sky-900
        labelColor = Color(0xFFBAE6FD) // sky-200
    )

    /**
     * 미드나잇 슬레이트 테마
     */
    val SlateDark = TimerColorScheme(
        progressColor = Color(0xFF94A3B8), // slate-400
        knobColor = Color(0xFF1E293B), // slate-800
        tickColor = Color(0xFF334155), // slate-700
        labelColor = Color(0xFFCBD5E1) // slate-300
    )

    /**
     * 에메랄드 그린 테마
     */
    val GreenDark = TimerColorScheme(
        progressColor = Color(0xFF34D399), // emerald-400
        knobColor = Color(0xFF064E3B), // emerald-900
        tickColor = Color(0xFF065F46), // emerald-800
        labelColor = Color(0xFF6EE7B7) // emerald-300
    )

    /**
     * 모든 라이트 테마 프리셋 리스트
     */
    val lightPresets = listOf(
        HaruLight,
        CoralLight,
        PurpleLight,
        SkyLight,
        SlateLight,
        GreenLight
    )

    /**
     * 모든 다크 테마 프리셋 리스트
     */
    val darkPresets = listOf(
        HaruDark,
        CoralDark,
        PurpleDark,
        SkyDark,
        SlateDark,
        GreenDark
    )

    @Composable
    fun presetColors(themeMode: ThemeMode = ThemeMode.SYSTEM): List<TimerColorScheme> {
        val useDarkTheme = when (themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
        return if (useDarkTheme) darkPresets else lightPresets
    }
}
