package com.jm.focustimer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.repository.SettingsRepository
import com.jm.focustimer.navigation.FocusTimerNavHost
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by settingsRepository.isDarkTheme.collectAsStateWithLifecycle(
                initialValue = isSystemInDarkTheme()
            )

            FocusTimerTheme(
                darkTheme = isDarkTheme
            ) {
                FocusTimerApp()
            }
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

    // 알림 권한 요청 로직
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "알림을 허용해야 타이머 진행 상황을 알려드릴 수 있어요.",
                        withDismissAction = true
                    )
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    FocusTimerNavHost(
        navController = navController,
        snackbarHostState = snackbarHostState,
        scope = scope
    )
}
