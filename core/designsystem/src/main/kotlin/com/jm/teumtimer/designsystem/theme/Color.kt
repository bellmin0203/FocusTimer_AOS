package com.jm.teumtimer.designsystem.theme

import androidx.compose.ui.graphics.Color

// =================================================================================================
// Brand Colors (브랜드 컬러)
// =================================================================================================

// Primary Colors - 테마별 브랜드 아이덴티티
// Dark Theme (Deep Focus): 깊은 몰입을 위한 민트
val PrimaryMint = Color(0xFF64FFDA) // 다크 모드용 밝은 민트 (Teal Accent 200)
val PrimaryMintContainer = Color(0xFF004D40) // 컨테이너 (Teal 900)

// Light Theme (Clean Focus): 신뢰감을 주는 딥 틸/네이비
val PrimaryDeepTeal = Color(0xFF00695C) // 라이트 모드용 딥 틸 (Teal 800)
val PrimaryDeepTealContainer = Color(0xFFB2DFDB) // 컨테이너 (Teal 100)
val PrimaryDeepTealLight = Color(0xFF4DB6AC) // 부드러운 강조용 (Teal 300)

// Error Colors (에러/경고) - UX 필수 요소
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)

// =================================================================================================
// Dark Theme Palette - Deep Focus (깊은 몰입)
// =================================================================================================

val DarkBackground = Color(0xFF0F172A) // Slate 900 - 깊은 네이비
val DarkSurface = Color(0xFF1E293B) // Slate 800 - 카드/컴포넌트 배경
val DarkSurfaceVariant = Color(0xFF334155) // Slate 700

val DarkOnBackground = Color(0xFFE2E8F0) // Slate 200 - 순수 흰색보다 눈의 피로가 덜한 Off-White
val DarkOnSurface = Color(0xFFE2E8F0)
val DarkOnSurfaceVariant = Color(0xFF94A3B8) // Slate 400 - 부차적인 텍스트

val DarkSecondaryText = Color(0xFF94A3B8) // Deprecated: Use DarkOnSurfaceVariant
val DarkBorder = Color(0xFF334155) // Slate 700 - 경계선
val DarkSurfaceInactive = Color(0xFF1E293B) // 비활성 상태

// Session List (Dark)
val DarkSessionBackground = Color(0xFF020617) // Slate 950 - 더 어두운 배경
val DarkSessionCard = Color(0xFF1E293B) // Slate 800 - 카드 배경
val DarkSessionBorder = Color(0xFF334155) // Slate 700 - 카드 경계선
val DarkSessionTextSecondary = Color(0xFF94A3B8)
val DarkSessionTextTertiary = Color(0xFF64748B)

// =================================================================================================
// Light Theme Palette - Clear Day (선명한 하루)
// =================================================================================================

val LightBackground = Color(0xFFF8FAFC) // Slate 50 - 차분한 쿨 그레이
val LightBackgroundVariant = Color(0xFFF1F5F9) // Slate 100 - 약간 더 어두운 변형
val LightSurface = Color(0xFFFFFFFF) // 카드/컴포넌트 배경
val LightSurfaceVariant = Color(0xFFE2E8F0) // Slate 200

val LightOnBackground = Color(0xFF0F172A) // Slate 900 - 차분한 다크 네이비
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B) // Slate 500 - 부차적인 텍스트

val LightSecondaryText = Color(0xFF64748B) // Deprecated: Use LightOnSurfaceVariant
val LightBorder = Color(0xFFE2E8F0) // Slate 200 - 경계선
val LightBorderVariant = Color(0xFFF1F5F9)
val LightSwitchInactive = Color(0xFFE2E8F0)

// Session List (Light)
val LightSessionBackground = Color(0xFFF8FAFC)
val LightSessionCard = Color(0xFFFFFFFF)
val LightSessionBorder = Color(0xFFE2E8F0)
val LightSessionTextSecondary = Color(0xFF64748B)
val LightSessionTextTertiary = Color(0xFF94A3B8)

// =================================================================================================
// Semantic & Component Colors (기능 및 컴포넌트)
// =================================================================================================

// Focus Icon - Primary 컬러 사용
val FocusIconLightBackground = Color(0xFFB2DFDB) // Teal 100
val FocusIconDarkBackground = Color(0x1A64FFDA)  // Mint 10% 투명도

// Break Icon - 보색 계열로 조화
val BreakIconLight = Color(0xFF5E35B1)           // Deep Purple 600
val BreakIconLightBackground = Color(0xFFEDE7F6) // Deep Purple 50
val BreakIconDark = Color(0xFFB39DDB)            // Deep Purple 200
val BreakIconDarkBackground = Color(0x1AB39DDB)  // Purple 10% 투명도

// Chart & Statistics
val ChartBarLight = Color(0xFFE2E8F0) // Slate 200
val ChartBarDark = Color(0xFF334155) // Slate 700

val StatsCardLightBackground = Color(0xFFFFFFFF)
val StatsCardLightBorder = Color(0xFFE2E8F0)

val StatsCardDarkBackground = Color(0xFF1E293B) // Slate 800
val StatsCardDarkBorder = Color(0xFF334155) // Slate 700

// =================================================================================================
// Legacy Support (레거시 호환성)
// =================================================================================================
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
