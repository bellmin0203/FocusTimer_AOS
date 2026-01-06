package com.jm.teumtimer.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Focus Timer 앱에서 사용되는 Shape 정의
val Shapes = Shapes(
    // 작은 요소 (스위치, 작은 칩)
    extraSmall = RoundedCornerShape(8.dp),

    // 버튼, 작은 카드
    small = RoundedCornerShape(12.dp),

    // 일반 카드, 입력 필드
    medium = RoundedCornerShape(16.dp),

    // 세션 카드, 큰 버튼
    large = RoundedCornerShape(20.dp),

    // 하단 시트, 다이얼로그
    extraLarge = RoundedCornerShape(24.dp)
)
