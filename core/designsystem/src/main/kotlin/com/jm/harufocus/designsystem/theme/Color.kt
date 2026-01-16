package com.jm.harufocus.designsystem.theme

import androidx.compose.ui.graphics.Color

// =================================================================================================
// Brand Colors (브랜드 컬러) - HaruFocus Identity
// =================================================================================================

// Core Palette
val BrandNavy = Color(0xFF252A4E)    // #1. Navy: 차분함, 깊은 몰입 (Light Primary / Dark Surface)
val BrandPurple = Color(0xFF5A4A72)  // #2. Purple: 균형, 보조 (Secondary)
val BrandYellow = Color(0xFFFAD79C)  // #3. Yellow: 활력, 강조 (Dark Primary / Light Tertiary)

// Derived Colors for Light Theme
val LightPrimary = BrandNavy
val LightOnPrimary = Color.White
val LightPrimaryContainer = Color(0xFFD6D9F0) // Navy의 틴트 (부드러운 배경)
val LightOnPrimaryContainer = Color(0xFF15182E)

val LightSecondary = BrandPurple
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFEBE5F5) // Purple의 틴트
val LightOnSecondaryContainer = Color(0xFF2E253D)

val LightTertiary = Color(0xFF7A6432) // Yellow의 가독성을 위한 Dark Variant (텍스트용)
val LightOnTertiary = Color.White
val LightTertiaryContainer = BrandYellow
val LightOnTertiaryContainer = Color(0xFF29200D)

// Derived Colors for Dark Theme
val DarkPrimary = BrandYellow
val DarkOnPrimary = Color(0xFF252A4E) // Yellow 위에는 Navy 텍스트가 가독성 좋음
val DarkPrimaryContainer = Color(0xFF5C471F) // Yellow의 Shade
val DarkOnPrimaryContainer = BrandYellow

val DarkSecondary = Color(0xFFCEC2E0) // Purple의 Light Variant
val DarkOnSecondary = Color(0xFF2E253D)
val DarkSecondaryContainer = BrandPurple
val DarkOnSecondaryContainer = Color(0xFFEBE5F5)

// =================================================================================================
// Background & Surface Colors
// =================================================================================================

// Dark Theme Background Strategy
// BrandNavy(#252A4E)를 Surface(카드)로 사용하고, 더 깊은 색을 배경으로 사용
val DarkBackground = Color(0xFF121526) // BrandNavy보다 더 어두운 Deep Navy
val DarkSurface = BrandNavy
val DarkSurfaceVariant = Color(0xFF32385E) // BrandNavy보다 약간 밝은 톤

val DarkOnBackground = Color(0xFFE2E2E6) // Off-White
val DarkOnSurface = Color(0xFFE2E2E6)
val DarkOnSurfaceVariant = Color(0xFFC4C6D0)
val DarkBorder = Color(0xFF454B6E)

// Light Theme Background Strategy
val LightBackground = Color(0xFFF9F9FC) // 쿨 톤의 아주 밝은 그레이/블루
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE7E7F0)

val LightOnBackground = Color(0xFF1A1C29)
val LightOnSurface = Color(0xFF1A1C29)
val LightOnSurfaceVariant = Color(0xFF444759)
val LightBorder = Color(0xFFDCDCE5)

// =================================================================================================
// Error Colors (Material3 Default)
// =================================================================================================
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)

// =================================================================================================
// Semantic & Component Colors (기능 및 컴포넌트)
// =================================================================================================

// Focus Icon
val FocusIconLightBackground = LightPrimaryContainer
val FocusIconDarkBackground = Color(0x33FAD79C) // Yellow 20%

// Break Icon (Purple 계열 활용)
val BreakIconLight = BrandPurple
val BreakIconLightBackground = LightSecondaryContainer
val BreakIconDark = Color(0xFFCEC2E0)
val BreakIconDarkBackground = Color(0x33CEC2E0)
