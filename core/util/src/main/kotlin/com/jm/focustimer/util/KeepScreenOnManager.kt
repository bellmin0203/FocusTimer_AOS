package com.jm.focustimer.util

import android.view.Window
import android.view.WindowManager
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