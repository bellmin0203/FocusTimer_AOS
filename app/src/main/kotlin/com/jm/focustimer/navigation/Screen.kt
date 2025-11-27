package com.jm.focustimer.navigation

/**
 * 앱 내 모든 화면 라우트를 정의
 * sealed class로 타입 안전성 보장
 */
sealed class Screen(val route: String) {
    /**
     * 타이머 메인 화면
     */
    data object Timer : Screen("timer")

    /**
     * 설정 화면
     */
    data object Setting : Screen("setting")

    /**
     * 통계 화면 (향후 구현 예정)
     */
    data object Stats : Screen("stats")
}
