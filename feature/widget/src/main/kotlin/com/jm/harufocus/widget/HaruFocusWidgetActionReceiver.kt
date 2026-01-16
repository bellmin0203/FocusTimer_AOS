package com.jm.harufocus.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 하루 몰입 Widget Action Receiver
 * 
 * 위젯 버튼 클릭 이벤트를 처리하는 BroadcastReceiver
 */
@AndroidEntryPoint
class HaruFocusWidgetActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var widgetInteractor: HaruFocusWidgetInteractor
    
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.Main)

        scope.launch {
            try {
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
                    WidgetActions.ACTION_STOP_TIMER -> {
                        widgetInteractor.stopTimer()
                    }
                    WidgetActions.ACTION_COMPLETE_TIMER -> {
                        widgetInteractor.completeTimer()
                    }
                    WidgetActions.ACTION_INCREASE_TIME -> {
                        widgetInteractor.increaseTime()
                    }
                    WidgetActions.ACTION_DECREASE_TIME -> {
                        widgetInteractor.decreaseTime()
                    }
                    WidgetActions.ACTION_OPEN_APP -> {
                        widgetInteractor.openApp(context)
                    }
                }
                
                // 위젯 업데이트
                updateWidget(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
