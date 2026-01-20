package com.jm.harufocus.timer.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.jm.harufocus.common.TimerServiceAction
import com.jm.harufocus.timer.service.TimerService

/**
 * 타이머 서비스에 액션을 전송하는 확장 함수
 *
 * @param action 서비스에 전달할 액션 (TimerServiceAction 참조)
 * @param durationMillis 타이머 시작 시 전달할 지속 시간 (밀리초), null이면 전달하지 않음
 */
fun Context.sendTimerServiceAction(
    action: String,
    durationMillis: Long? = null
) {
    val intent = Intent(this, TimerService::class.java).apply {
        this.action = action
        durationMillis?.let {
            putExtra(TimerServiceAction.EXTRA_DURATION, it)
        }
    }
    ContextCompat.startForegroundService(this, intent)
}
