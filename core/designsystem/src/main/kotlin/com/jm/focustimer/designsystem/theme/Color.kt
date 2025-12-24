package com.jm.focustimer.designsystem.theme

import androidx.compose.ui.graphics.Color

// =================================================================================================
// Brand Colors (브랜드 컬러)
// =================================================================================================

// Primary Color - 브랜드의 메인 아이덴티티
// Note: 0xFF38E07B는 다크 모드에서 시인성이 좋으나, 라이트 모드 흰 배경 위에서는 명암비(Contrast)가 낮습니다.
val PrimaryGreen = Color(0xFF38E07B)

// Accessibility Optimized - 라이트 모드에서 텍스트/아이콘 가독성을 위한 짙은 녹색 (WCAG AA 준수)
val PrimaryGreenLight = Color(0xFF006C45)
val PrimaryGreenContainer = Color(0xFF66FE99) // 덜 강조되는 요소 배경용

// Error Colors (에러/경고) - UX 필수 요소
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)

// =================================================================================================
// Dark Theme Palette (다크 테마)
// =================================================================================================

val DarkBackground = Color(0xFF111714) // Deep Green-Black for immersive feeling
val DarkSurface = Color(0xFF1A231F)
val DarkSurfaceVariant = Color(0xFF1C2620)

val DarkOnBackground = Color(0xFFE2E3DE) // 순수 흰색(FFFFFF)보다 눈의 피로가 덜한 Off-White 권장
val DarkOnSurface = Color(0xFFE2E3DE)
val DarkOnSurfaceVariant = Color(0xFFC0C9C3)

val DarkSecondaryText = Color(0xFF9EB7A8) // Deprecated: Use DarkOnSurfaceVariant
val DarkBorder = Color(0xFF3D5245)
val DarkSurfaceInactive = Color(0xFF29382F)

// Session List (Dark)
val DarkSessionBackground = Color(0xFF0D1411) // gray-950 base tinted
val DarkSessionCard = Color(0xFF1A1F1C)       // gray-900 base tinted
val DarkSessionBorder = Color(0xFF29312C)     // gray-800 base tinted
val DarkSessionTextSecondary = Color(0xFF9CA3AF)
val DarkSessionTextTertiary = Color(0xFF6B7280)

// =================================================================================================
// Light Theme Palette (라이트 테마)
// =================================================================================================

val LightBackground = Color(0xFFFFFFFF) // Clean White
val LightBackgroundVariant = Color(0xFFF7F9F8) // Cool Mint-Grey tint
val LightSurface = Color(0xFFF0F3F2)
val LightSurfaceVariant = Color(0xFFE0E5E2)

val LightOnBackground = Color(0xFF111714) // Soft Black
val LightOnSurface = Color(0xFF111714)
val LightOnSurfaceVariant = Color(0xFF404944)

val LightSecondaryText = Color(0xFF6A7C72) // Deprecated: Use LightOnSurfaceVariant
val LightBorder = Color(0xFFD8E0DA)
val LightBorderVariant = Color(0xFFE3EBE6)
val LightSwitchInactive = Color(0xFFE3EBE6)

// Session List (Light)
val LightSessionBackground = Color(0xFFFFFFFF)
val LightSessionCard = Color(0xFFFAFAFA)
val LightSessionBorder = Color(0xFFE5E7EB)
val LightSessionTextSecondary = Color(0xFF6B7280)
val LightSessionTextTertiary = Color(0xFF9CA3AF)

// =================================================================================================
// Semantic & Component Colors (기능 및 컴포넌트)
// =================================================================================================

// Focus Icon
val FocusIconLightBackground = Color(0xFFDCFCE7) // primary-100
val FocusIconDarkBackground = Color(0x1A22C55E)  // primary-500 with 10% opacity

// Break Icon
val BreakIconLight = Color(0xFF2563EB)           // blue-600
val BreakIconLightBackground = Color(0xFFDBEAFE) // blue-100
val BreakIconDark = Color(0xFF60A5FA)            // blue-400
val BreakIconDarkBackground = Color(0x1A3B82F6)  // blue-500 with 10% opacity

// Chart & Statistics
val ChartBarLight = Color(0xFFE5E7EB)
val ChartBarDark = Color(0xFF29382F)

val StatsCardLightBackground = Color(0xFFF9FAFB)
val StatsCardLightBorder = Color(0xFFE5E7EB)

val StatsCardDarkBackground = Color(0xFF1C2620)
val StatsCardDarkBorder = Color(0xFF3D5245)

// =================================================================================================
// Legacy Support (레거시 호환성)
// =================================================================================================
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)