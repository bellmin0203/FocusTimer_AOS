package com.jm.focustimer.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.jm.logutil.LogUtil

/**
 * 화면 켜짐 유지 제어를 담당하는 Helper
 * 시스템 플래그 관리 로직을 캡슐화합니다
 */
class KeepScreenOnManager(
    private val window: Window?
) {
    private var isScreenOnActive = false

    /**
     * 화면 켜짐 유지를 활성화합니다
     */
    fun enableKeepScreenOn() {
        if (!isScreenOnActive) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            isScreenOnActive = true
            LogUtil.d("화면 켜짐 유지 활성화")
        }
    }

    /**
     * 화면 켜짐 유지를 비활성화합니다
     */
    fun disableKeepScreenOn() {
        if (isScreenOnActive) {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            isScreenOnActive = false
            LogUtil.d("화면 켜짐 유지 비활성화")
        }
    }

    /**
     * 리소스 정리
     */
    fun release() {
        disableKeepScreenOn()
    }
}

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

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}