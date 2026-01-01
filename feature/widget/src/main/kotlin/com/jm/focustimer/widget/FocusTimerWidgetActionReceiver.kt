package com.jm.focustimer.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Focus Timer Widget Action Receiver
 * 
 * 위젯 버튼 클릭 이벤트를 처리하는 BroadcastReceiver
 */
@AndroidEntryPoint
class FocusTimerWidgetActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var widgetInteractor: FocusTimerWidgetInteractor
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WidgetActions.ACTION_START_TIMER -> {
                widgetInteractor.startTimer()
            }
            WidgetActions.ACTION_PAUSE_TIMER -> {
                widgetInteractor.pauseTimer()
            }
            WidgetActions.ACTION_RESUME_TIMER -> {
                widgetInteractor.resumeTimer()
            }
            WidgetActions.ACTION_OPEN_APP -> {
                widgetInteractor.openApp(context)
            }
        }
        
        // 위젯 업데이트
        updateWidget(context)
    }
}
