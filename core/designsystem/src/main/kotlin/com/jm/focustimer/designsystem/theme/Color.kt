package com.jm.focustimer.designsystem.theme

import androidx.compose.ui.graphics.Color

// =================================================================================================
// Brand Colors (브랜드 컬러)
// =================================================================================================

// Primary Colors - 테마별 브랜드 아이덴티티
// Dark Theme (Midnight Gap): 어둠 속에서 빛나는 밝은 옐로우
val PrimaryYellow = Color(0xFFFFF176) // 다크 모드용 밝은 옐로우
val PrimaryYellowContainer = Color(0xFFFFF9C4) // 컨테이너/덜 강조되는 요소

// Light Theme (Morning Ray): 새벽의 따뜻한 햇살을 연상시키는 오렌지
val PrimaryOrange = Color(0xFFFF6F3C) // 라이트 모드용 오렌지 (가독성 최적화, WCAG AA 준수)
val PrimaryOrangeContainer = Color(0xFFFFCCBC) // 컨테이너/덜 강조되는 요소
val PrimaryOrangeLight = Color(0xFFFFAB91) // 부드러운 강조용

// Error Colors (에러/경고) - UX 필수 요소
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)

// =================================================================================================
// Dark Theme Palette - Midnight Gap (심야의 틈)
// =================================================================================================

val DarkBackground = Color(0xFF1A1C1E) // 깊은 네이비 - 심야의 고요함
val DarkSurface = Color(0xFF24262A) // 카드/컴포넌트 배경
val DarkSurfaceVariant = Color(0xFF2C2E33)

val DarkOnBackground = Color(0xFFE8E8E8) // 순수 흰색보다 눈의 피로가 덜한 Off-White
val DarkOnSurface = Color(0xFFE8E8E8)
val DarkOnSurfaceVariant = Color(0xFFB0B0B0) // 부차적인 텍스트

val DarkSecondaryText = Color(0xFFB0B0B0) // Deprecated: Use DarkOnSurfaceVariant
val DarkBorder = Color(0xFF3F4147) // 경계선
val DarkSurfaceInactive = Color(0xFF2C2E33) // 비활성 상태

// Session List (Dark)
val DarkSessionBackground = Color(0xFF16181A) // 더 어두운 배경
val DarkSessionCard = Color(0xFF24262A) // 카드 배경
val DarkSessionBorder = Color(0xFF3F4147) // 카드 경계선
val DarkSessionTextSecondary = Color(0xFFB0B0B0)
val DarkSessionTextTertiary = Color(0xFF787878)

// =================================================================================================
// Light Theme Palette - Morning Ray (새벽의 빛)
// =================================================================================================

val LightBackground = Color(0xFFF5F5F5) // 그레이시 화이트 - 부드러운 아침 분위기
val LightBackgroundVariant = Color(0xFFFAFAFA) // 약간 더 밝은 변형
val LightSurface = Color(0xFFFFFFFF) // 카드/컴포넌트 배경
val LightSurfaceVariant = Color(0xFFF0F0F0)

val LightOnBackground = Color(0xFF1A1C1E) // 차분한 다크 그레이
val LightOnSurface = Color(0xFF1A1C1E)
val LightOnSurfaceVariant = Color(0xFF5F6368) // 부차적인 텍스트

val LightSecondaryText = Color(0xFF5F6368) // Deprecated: Use LightOnSurfaceVariant
val LightBorder = Color(0xFFE0E0E0) // 경계선
val LightBorderVariant = Color(0xFFEEEEEE)
val LightSwitchInactive = Color(0xFFEEEEEE)

// Session List (Light)
val LightSessionBackground = Color(0xFFF5F5F5)
val LightSessionCard = Color(0xFFFFFFFF)
val LightSessionBorder = Color(0xFFE0E0E0)
val LightSessionTextSecondary = Color(0xFF5F6368)
val LightSessionTextTertiary = Color(0xFF9E9E9E)

// =================================================================================================
// Semantic & Component Colors (기능 및 컴포넌트)
// =================================================================================================

// Focus Icon - Primary 컬러 사용
val FocusIconLightBackground = Color(0xFFFFE0CC) // 오렌지 계열 연한 배경
val FocusIconDarkBackground = Color(0x1AFFF176)  // 옐로우 10% 투명도

// Break Icon - 보색 계열로 조화
val BreakIconLight = Color(0xFF5E35B1)           // 보라-인디고 (오렌지 보색)
val BreakIconLightBackground = Color(0xFFEDE7F6) // 보라 연한 배경
val BreakIconDark = Color(0xFF80CBC4)            // 민트 (옐로우와 조화)
val BreakIconDarkBackground = Color(0x1A80CBC4)  // 민트 10% 투명도

// Chart & Statistics
val ChartBarLight = Color(0xFFE0E0E0)
val ChartBarDark = Color(0xFF3F4147)

val StatsCardLightBackground = Color(0xFFFFFFFF)
val StatsCardLightBorder = Color(0xFFE0E0E0)

val StatsCardDarkBackground = Color(0xFF24262A)
val StatsCardDarkBorder = Color(0xFF3F4147)

// =================================================================================================
// Legacy Support (레거시 호환성)
// =================================================================================================
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)