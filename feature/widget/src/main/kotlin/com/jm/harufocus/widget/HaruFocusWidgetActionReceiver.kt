package com.jm.harufocus.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jm.logutil.LogUtil
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
        LogUtil.d("onReceive: action=${intent.action}")

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.Main)

        scope.launch {
            try {
                when (intent.action) {
                    WidgetActions.ACTION_START_TIMER -> {
                        LogUtil.d("Action: START_TIMER")
                        widgetInteractor.startTimer()
                    }
                    WidgetActions.ACTION_PAUSE_TIMER -> {
                        LogUtil.d("Action: PAUSE_TIMER")
                        widgetInteractor.pauseTimer()
                    }
                    WidgetActions.ACTION_RESUME_TIMER -> {
                        LogUtil.d("Action: RESUME_TIMER")
                        widgetInteractor.resumeTimer()
                    }
                    WidgetActions.ACTION_STOP_TIMER -> {
                        LogUtil.d("Action: STOP_TIMER")
                        widgetInteractor.stopTimer()
                    }
                    WidgetActions.ACTION_COMPLETE_TIMER -> {
                        LogUtil.d("Action: COMPLETE_TIMER")
                        widgetInteractor.completeTimer()
                    }
                    WidgetActions.ACTION_INCREASE_TIME -> {
                        LogUtil.d("Action: INCREASE_TIME")
                        widgetInteractor.increaseTime()
                    }
                    WidgetActions.ACTION_DECREASE_TIME -> {
                        LogUtil.d("Action: DECREASE_TIME")
                        widgetInteractor.decreaseTime()
                    }
                    WidgetActions.ACTION_OPEN_APP -> {
                        LogUtil.d("Action: OPEN_APP")
                        widgetInteractor.openApp(context)
                    }
                    else -> {
                        LogUtil.w("Unknown action: ${intent.action}")
                    }
                }
                
                // 위젯 업데이트
                LogUtil.d("Updating widget after action")
                updateWidget(context)
            } catch (e: Exception) {
                LogUtil.e("Error processing widget action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
