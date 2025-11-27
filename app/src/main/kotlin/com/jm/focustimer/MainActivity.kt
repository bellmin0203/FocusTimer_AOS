package com.jm.focustimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.navigation.FocusTimerNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusTimerApp()
        }
    }
}

/**
 * FocusTimer 앱의 메인 컴포저블
 * 네비게이션과 전역 상태를 관리
 */
@Composable
private fun FocusTimerApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    FocusTimerTheme {
        FocusTimerNavHost(
            navController = navController,
            snackbarHostState = snackbarHostState,
            scope = scope
        )
    }
}