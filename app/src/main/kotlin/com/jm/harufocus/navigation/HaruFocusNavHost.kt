package com.jm.harufocus.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jm.harufocus.setting.SettingScreen
import com.jm.harufocus.stats.StatsScreen
import com.jm.harufocus.timer.TimerScreen
import com.jm.harufocus.util.CrashReporter

/**
 * 앱 전체 네비게이션 구조 정의
 */
@Composable
fun HaruFocusNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    onRequestInAppReview: () -> Unit = {}
) {
    // 현재 화면을 Crashlytics에 기록
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            entry.destination.route?.let { route ->
                CrashReporter.setCurrentScreen(route)
            }
        }
    }

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
                },
                onRequestInAppReview = onRequestInAppReview
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
