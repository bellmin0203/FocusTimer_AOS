package com.jm.harufocus.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.jm.logutil.LogUtil

/**
 * 하루 몰입 Widget Receiver
 * 
 * Glance 위젯의 BroadcastReceiver
 */
class HaruFocusWidgetReceiver : GlanceAppWidgetReceiver() {
    
    override val glanceAppWidget: GlanceAppWidget
        get() = HaruFocusWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        LogUtil.d("onUpdate Broadcast Received")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        LogUtil.d("onReceive Action: ${intent.action}")
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // 위젯이 처음 추가될 때 호출
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // 모든 위젯이 제거될 때 호출
    }
}
