package com.jm.focustimer.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Focus Timer Widget Receiver
 * 
 * Glance 위젯의 BroadcastReceiver
 */
class FocusTimerWidgetReceiver : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget
        get() = FocusTimerWidget()
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // 위젯이 처음 추가될 때 호출
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // 모든 위젯이 제거될 때 호출
    }
}
