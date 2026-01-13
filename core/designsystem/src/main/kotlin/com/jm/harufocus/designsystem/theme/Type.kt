package com.jm.harufocus.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Spline Sans 폰트 사용 (디자인 시스템에서 사용)
// 프로젝트에 폰트 파일 추가 후 FontFamily를 커스터마이징할 수 있습니다
private val SplineSans = FontFamily.Default // TODO: Spline Sans 폰트 파일 추가 후 변경

// Material 3 Typography 정의
val Typography = Typography(
    // Display - 큰 제목용
    displayLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline - 타이머 숫자 등
    headlineLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp, // 타이머 "25"
        lineHeight = 80.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp, // 통계 카드 숫자 "4h 30m"
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title - 화면 제목, 섹션 제목
    titleLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp, // "Focus Timer", "Sessions" 헤더
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp, // "Presets" 제목
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp, // "Focus", "Short Break" 버튼
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    ),

    // Body - 본문 텍스트
    bodyLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, // 일반 텍스트
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, // 설명 텍스트
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, // 작은 설명 텍스트
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label - 버튼, 탭, 라벨
    labelLarge = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp, // 버튼 텍스트
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp, // 탭 레이블 "Timer", "Statistics"
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SplineSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp, // "minutes", 차트 요일
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)