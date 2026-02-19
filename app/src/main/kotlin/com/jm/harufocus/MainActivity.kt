package com.jm.harufocus

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.data.review.InAppReviewManager
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.domain.repository.TimerSessionRepository
import com.jm.harufocus.domain.repository.UserPreferencesRepository
import com.jm.harufocus.navigation.HaruFocusNavHost
import com.jm.harufocus.navigation.Screen
import com.jm.logutil.LogUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var timerSessionRepository: TimerSessionRepository

    @Inject
    lateinit var inAppReviewManager: InAppReviewManager

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var isTutorialCompleted by mutableStateOf<Boolean?>(null)

        // 초기 데이터 로딩 및 온보딩 여부 결정
        lifecycleScope.launch {
            val storedValue = userPreferencesRepository.isTutorialCompleted.first()
            if (storedValue != null) {
                isTutorialCompleted = storedValue
            } else {
                // 값이 없으면(신규 설치 또는 업데이트), 기존 세션 기록을 확인하여 기존 사용자인지 판단
                // IO 작업이므로 Main Thread에서 실행되지 않도록 주의해야 하나,
                // Room의 suspend function은 Dispatcher를 처리함.
                val sessionCount = try {
                    timerSessionRepository.getTotalSessionCount()
                } catch (e: Exception) {
                    0
                }

                if (sessionCount > 0) {
                    // 기존 사용자: 튜토리얼 완료 처리 및 스킵
                    userPreferencesRepository.updateIsTutorialCompleted(true)
                    isTutorialCompleted = true
                } else {
                    // 신규 사용자: 튜토리얼 표시
                    isTutorialCompleted = false
                }
            }
        }

        // 데이터가 로드될 때까지 스플래시 유지
        splashScreen.setKeepOnScreenCondition {
            isTutorialCompleted == null
        }

        // 스플래시 화면을 빠르게(200ms) 페이드 아웃하여 자연스럽게 사라지게 함
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            )
            fadeOut.duration = 200L
            fadeOut.doOnEnd { splashScreenView.remove() }
            fadeOut.start()
        }

        enableEdgeToEdge()
        setContent {
            // isTutorialCompleted가 null이면(로딩 중) 아무것도 그리지 않음 (스플래시가 덮고 있음)
            // 하지만 Compose 트리는 구성되어야 하므로 안전하게 처리
            if (isTutorialCompleted == null) return@setContent

            val themeMode by settingsRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM
            )

            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val startDestination = if (isTutorialCompleted == true) {
                Screen.Timer.route
            } else {
                Screen.Onboarding.route
            }

            HaruFocusTheme(
                darkTheme = isDarkTheme
            ) {
                HaruFocusApp(
                    startDestination = startDestination,
                    onRequestInAppReview = { launchInAppReview() }
                )
            }
        }
    }

    /**
     * In-App Review 플로우를 실행합니다.
     */
    private fun launchInAppReview() {
        kotlinx.coroutines.MainScope().launch {
            inAppReviewManager.launchReviewFlow(this@MainActivity)
                .onSuccess {
                    LogUtil.d("In-App Review 플로우 완료")
                }
                .onFailure { e ->
                    LogUtil.e("In-App Review 플로우 실패", e)
                }
        }
    }
}

/**
 * HaruFocus 앱의 메인 컴포저블
 * 네비게이션과 전역 상태를 관리
 */
@Composable
private fun HaruFocusApp(
    startDestination: String,
    onRequestInAppReview: () -> Unit = {}
) {
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

    HaruFocusNavHost(
        navController = navController,
        snackbarHostState = snackbarHostState,
        startDestination = startDestination,
        onRequestInAppReview = onRequestInAppReview
    )
}
