package com.jm.focustimer.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jm.focustimer.setting.SettingScreen
import com.jm.focustimer.stats.StatsScreen
import com.jm.focustimer.timer.TimerScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 앱 전체 네비게이션 구조 정의
 */
@Composable
fun FocusTimerNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Timer.route
    ) {
        // 타이머 메인 화면
        composable(Screen.Timer.route) {
            TimerScreen(
                snackbarHostState = snackbarHostState,
                onSettingsClick = {
                    navController.navigate(Screen.Setting.route)
                },
                onStatsClick = {
                    navController.navigate(Screen.Stats.route)
                }
            )
        }

        // 설정 화면
        composable(Screen.Setting.route) {
            SettingScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 통계 화면
        composable(Screen.Stats.route) {
            StatsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 통계 화면 클릭 처리
 *
 * FIXME(2025-12-15): Stats 화면 구현 완료 후 실제 네비게이션으로 대체 필요
 * 현재는 임시로 스낵바 메시지만 표시
 */
private fun handleStatsClick(
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    scope.launch {
        snackbarHostState.showSnackbar("통계 화면 구현 예정")
    }
}