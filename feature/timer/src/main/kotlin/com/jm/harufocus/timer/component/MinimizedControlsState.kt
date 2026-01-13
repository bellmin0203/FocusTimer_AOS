package com.jm.harufocus.timer.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jm.logutil.LogUtil

/**
 * 최소화된 컨트롤의 UI 가시성 상태를 관리하는 클래스
 */
@Stable
class MinimizedControlsState(
    private val autoHideDelayMillis: Long = 2000L
) {
    var isControlsVisible by mutableStateOf(true)
        private set

    private var lastInteractionTime by mutableLongStateOf(System.currentTimeMillis())

    /**
     * 사용자 상호작용을 기록하고 UI를 표시합니다
     */
    fun recordInteraction() {
        isControlsVisible = true
        lastInteractionTime = System.currentTimeMillis()
    }

    /**
     * UI를 숨깁니다
     */
    fun hideControls() {
        isControlsVisible = false
        LogUtil.d("컨트롤 UI 숨김")
    }

    /**
     * UI를 표시합니다
     */
    fun showControls() {
        isControlsVisible = true
        LogUtil.d("컨트롤 UI 표시")
    }

    /**
     * 자동 숨김 딜레이가 경과했는지 확인합니다
     */
    fun shouldAutoHide(): Boolean {
        return System.currentTimeMillis() - lastInteractionTime >= autoHideDelayMillis
    }

    companion object {
        const val UI_AUTO_HIDE_DELAY_MILLIS = 2000L
    }
}

@Composable
fun rememberMinimizedControlsState(
    autoHideDelayMillis: Long = MinimizedControlsState.UI_AUTO_HIDE_DELAY_MILLIS
): MinimizedControlsState {
    return remember { MinimizedControlsState(autoHideDelayMillis) }
}