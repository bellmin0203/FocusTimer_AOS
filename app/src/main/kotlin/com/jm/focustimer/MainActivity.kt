package com.jm.focustimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.timer.TimerScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            FocusTimerTheme {
                TimerScreen(
                    onSettingsClick = {
                        // TODO: 설정 화면으로 네비게이션
                        scope.launch {
                            snackbarHostState.showSnackbar("설정 화면으로 이동 (구현 예정)")
                        }
                    },
                    onStatsClick = {
                        // TODO: 통계 화면으로 네비게이션
                        scope.launch {
                            snackbarHostState.showSnackbar("통계 화면으로 이동 (구현 예정)")
                        }
                    }
                )
            }
        }
    }
}
