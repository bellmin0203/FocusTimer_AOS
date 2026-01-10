package com.jm.teumtimer.designsystem.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
     * 틈 타이머 시그니처 테마 (오렌지)
     */
    val TeumLight = TimerColorScheme(
        progressColor = Color(0xFFFF6F3C), // PrimaryOrange
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFFFCCBC), // PrimaryOrangeContainer
        labelColor = Color(0xFFFF6F3C) // PrimaryOrange
    )

    /**
     * 코랄 핑크 테마
     */
    val CoralLight = TimerColorScheme(
        progressColor = Color(0xFFF43F5E), // rose-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFE5E7EB), // gray-200
        labelColor = Color(0xFF9CA3AF) // gray-400
    )

    /**
     * 라벤더 퍼플 테마
     */
    val LavenderLight = TimerColorScheme(
        progressColor = Color(0xFF8B5CF6), // violet-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFE5E7EB), // gray-200
        labelColor = Color(0xFF9CA3AF) // gray-400
    )

    /**
     * 스카이 블루 테마
     */
    val SkyLight = TimerColorScheme(
        progressColor = Color(0xFF0EA5E9), // sky-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFE0E7FF), // indigo-100
        labelColor = Color(0xFF94A3B8) // slate-400
    )

    /**
     * 슬레이트 그레이 테마 (구 Peach)
     */
    val SlateLight = TimerColorScheme(
        progressColor = Color(0xFF475569), // slate-600
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFE2E8F0), // slate-200
        labelColor = Color(0xFF64748B) // slate-500
    )

    /**
     * 세이지 그린 테마
     */
    val SageLight = TimerColorScheme(
        progressColor = Color(0xFF059669), // emerald-600
        knobColor = Color(0xFFFFFBEB), // amber-50
        tickColor = Color(0xFFD1FAE5), // emerald-100
        labelColor = Color(0xFF78716C) // stone-500
    )

    // Dark Theme Presets

    /**
     * 틈 타이머 시그니처 테마 (옐로우)
     */
    val TeumDark = TimerColorScheme(
        progressColor = Color(0xFFFFF176), // PrimaryYellow
        knobColor = Color(0xFF1A1C1E), // DarkBackground
        tickColor = Color(0xFF3F4147), // DarkBorder
        labelColor = Color(0xFFFFF176) // PrimaryYellow
    )

    /**
     * 핫 핑크 테마
     */
    val CoralDark = TimerColorScheme(
        progressColor = Color(0xFFFB7185), // rose-400
        knobColor = Color(0xFF1A231F),
        tickColor = Color(0xFF374151), // gray-700
        labelColor = Color(0xFF9CA3AF) // gray-400
    )

    /**
     * 바이올렛 퍼플 테마
     */
    val LavenderDark = TimerColorScheme(
        progressColor = Color(0xFFA78BFA), // violet-400
        knobColor = Color(0xFF1A231F),
        tickColor = Color(0xFF4C1D95), // violet-900
        labelColor = Color(0xFFC4B5FD) // violet-300
    )

    /**
     * 아쿠아 블루 테마
     */
    val SkyDark = TimerColorScheme(
        progressColor = Color(0xFF38BDF8), // sky-400
        knobColor = Color(0xFF0F172A), // slate-900
        tickColor = Color(0xFF334155), // slate-700
        labelColor = Color(0xFF94A3B8) // slate-400
    )

    /**
     * 미드나잇 슬레이트 테마 (구 Peach)
     */
    val SlateDark = TimerColorScheme(
        progressColor = Color(0xFFCBD5E1), // slate-300
        knobColor = Color(0xFF1E293B), // slate-800
        tickColor = Color(0xFF334155), // slate-700
        labelColor = Color(0xFF94A3B8) // slate-400
    )

    /**
     * 포레스트 그린 테마
     */
    val SageDark = TimerColorScheme(
        progressColor = Color(0xFF10B981), // emerald-500
        knobColor = Color(0xFF1C2620),
        tickColor = Color(0xFF065F46), // emerald-800
        labelColor = Color(0xFF6EE7B7) // emerald-300
    )

    /**
     * 모든 라이트 테마 프리셋 리스트
     */
    val lightPresets = listOf(
        TeumLight,
        CoralLight,
        LavenderLight,
        SkyLight,
        SlateLight,
        SageLight
    )

    /**
     * 모든 다크 테마 프리셋 리스트
     */
    val darkPresets = listOf(
        TeumDark,
        CoralDark,
        LavenderDark,
        SkyDark,
        SlateDark,
        SageDark
    )

    @get:Composable
    val presetColors: List<TimerColorScheme>
        get() = if (isSystemInDarkTheme()) darkPresets else lightPresets
}
