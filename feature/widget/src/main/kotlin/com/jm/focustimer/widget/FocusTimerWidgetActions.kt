package com.jm.focustimer.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Widget Actions
 * 
 * 위젯 버튼 클릭 시 실행할 액션들
 */

// Widget Action Constants
object WidgetActions {
    const val ACTION_START_TIMER = "com.jm.focustimer.widget.ACTION_START_TIMER"
    const val ACTION_PAUSE_TIMER = "com.jm.focustimer.widget.ACTION_PAUSE_TIMER"
    const val ACTION_RESUME_TIMER = "com.jm.focustimer.widget.ACTION_RESUME_TIMER"
    const val ACTION_STOP_TIMER = "com.jm.focustimer.widget.ACTION_STOP_TIMER"
    const val ACTION_OPEN_APP = "com.jm.focustimer.widget.ACTION_OPEN_APP"
}

/**
 * 타이머 시작 액션
 */
fun actionStartTimer(context: Context) {
    val intent = Intent(context, FocusTimerWidgetActionReceiver::class.java).apply {
        action = WidgetActions.ACTION_START_TIMER
    }
    context.sendBroadcast(intent)
}

/**
 * 타이머 일시정지 액션
 */
fun actionPauseTimer(context: Context) {
    val intent = Intent(context, FocusTimerWidgetActionReceiver::class.java).apply {
        action = WidgetActions.ACTION_PAUSE_TIMER
    }
    context.sendBroadcast(intent)
}

/**
 * 타이머 재개 액션
 */
fun actionResumeTimer(context: Context) {
    val intent = Intent(context, FocusTimerWidgetActionReceiver::class.java).apply {
        action = WidgetActions.ACTION_RESUME_TIMER
    }
    context.sendBroadcast(intent)
}

/**
 * 타이머 정지 액션
 */
fun actionStopTimer(context: Context) {
    val intent = Intent(context, FocusTimerWidgetActionReceiver::class.java).apply {
        action = WidgetActions.ACTION_STOP_TIMER
    }
    context.sendBroadcast(intent)
}

/**
 * 앱 열기 액션
 */
fun actionOpenApp(context: Context) {
    val intent = Intent(context, FocusTimerWidgetActionReceiver::class.java).apply {
        action = WidgetActions.ACTION_OPEN_APP
    }
    context.sendBroadcast(intent)
}

/**
 * 위젯 업데이트
 */
fun updateWidget(context: Context) {
    CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
        FocusTimerWidget().updateAll(context)
    }
}
