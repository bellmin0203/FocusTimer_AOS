package com.jm.harufocus.timer.effect

import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.jm.harufocus.util.KeepScreenOnManager
import com.jm.harufocus.util.findActivity
import com.jm.logutil.LogUtil

/**
 * 화면 켜짐 유지를 관리하는 Composable Effect
 *
 * @param shouldKeepScreenOn 화면을 켜진 상태로 유지해야 하는지 여부
 */
@Composable
fun KeepScreenOnEffect(shouldKeepScreenOn: Boolean) {
    val context = LocalContext.current
    val window = context.findActivity()?.window

    DisposableEffect(shouldKeepScreenOn) {
        val manager = KeepScreenOnManager(window)

        if (shouldKeepScreenOn) {
            manager.enableKeepScreenOn()
        } else {
            manager.disableKeepScreenOn()
        }

        onDispose {
            manager.release()
        }
    }
}

/**
 * 화면 방향을 강제로 제어하는 Effect
 * 시스템 설정을 무시하고 지정된 orientation을 따르도록 함
 *
 * @param orientation ActivityInfo의 화면 방향 상수 (예: SCREEN_ORIENTATION_SENSOR)
 */
@Composable
fun ScreenOrientationEffect(orientation: Int) {
    val context = LocalContext.current

    DisposableEffect(orientation) {
        val activity = context.findActivity()
        val originalOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        // 요청된 방향으로 설정 (시스템 설정 무시)
        activity?.requestedOrientation = orientation

        onDispose {
            // 화면을 벗어날 때 원래 방향 설정으로 복구
            activity?.requestedOrientation = originalOrientation
        }
    }
}

/**
 * 시스템바(상태바, 네비게이션바) 가시성을 제어하는 Effect
 *
 * @param shouldHide true이면 시스템바를 숨기고, false이면 표시
 */
@Composable
fun SystemBarsVisibilityEffect(shouldHide: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(shouldHide) {
        val activity = context.findActivity()
        val windowInsetsController = activity?.window?.let {
            WindowCompat.getInsetsController(it, view)
        }

        if (shouldHide) {
            windowInsetsController?.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            LogUtil.d("시스템바 숨김")
        } else {
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
            LogUtil.d("시스템바 표시")
        }

        onDispose {
            windowInsetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
