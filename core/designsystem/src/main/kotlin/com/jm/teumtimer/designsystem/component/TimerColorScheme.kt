package com.jm.teumtimer.designsystem.component

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
     * 민트 그린 테마 (기본)
     */
    val MintLight = TimerColorScheme(
        progressColor = Color(0xFF10B981), // emerald-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFD1D5DB), // gray-300
        labelColor = Color(0xFF6B7280) // gray-500
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
     * 피치 오렌지 테마
     */
    val PeachLight = TimerColorScheme(
        progressColor = Color(0xFFF59E0B), // amber-500
        knobColor = Color(0xFFFFFFFF),
        tickColor = Color(0xFFFDE68A), // amber-200
        labelColor = Color(0xFFA78BFA) // violet-400
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
     * 네온 그린 테마 (기본)
     */
    val MintDark = TimerColorScheme(
        progressColor = Color(0xFF34D399), // emerald-400
        knobColor = Color(0xFF1A231F),
        tickColor = Color(0xFF374151), // gray-700
        labelColor = Color(0xFF9CA3AF) // gray-400
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
     * 선셋 오렌지 테마
     */
    val PeachDark = TimerColorScheme(
        progressColor = Color(0xFFFBBF24), // amber-400
        knobColor = Color(0xFF1F2937), // gray-800
        tickColor = Color(0xFF78350F), // amber-900
        labelColor = Color(0xFFFDE68A) // amber-200
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
        MintLight,
        CoralLight,
        LavenderLight,
        SkyLight,
        PeachLight,
        SageLight
    )

    /**
     * 모든 다크 테마 프리셋 리스트
     */
    val darkPresets = listOf(
        MintDark,
        CoralDark,
        LavenderDark,
        SkyDark,
        PeachDark,
        SageDark
    )
}
