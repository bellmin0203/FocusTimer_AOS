package com.jm.harufocus.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jm.harufocus.setting.SettingScreen
import com.jm.harufocus.stats.StatsScreen
import com.jm.harufocus.timer.TimerScreen

/**
 * 앱 전체 네비게이션 구조 정의
 */
@Composable
fun HaruFocusNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
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
