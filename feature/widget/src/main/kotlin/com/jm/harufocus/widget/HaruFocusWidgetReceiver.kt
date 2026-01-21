package com.jm.harufocus.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.jm.harufocus.util.AnalyticsHelper
import com.jm.logutil.LogUtil
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 하루 몰입 Widget Receiver
 * 
 * Glance 위젯의 BroadcastReceiver
 */
class HaruFocusWidgetReceiver : GlanceAppWidgetReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AnalyticsEntryPoint {
        fun analyticsHelper(): AnalyticsHelper
    }

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
        LogUtil.d("Widget enabled (first widget added)")

        // Analytics 이벤트 로깅
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                AnalyticsEntryPoint::class.java
            )
            entryPoint.analyticsHelper().logWidgetAdded("default")
            entryPoint.analyticsHelper().setWidgetUser(true)
        } catch (e: Exception) {
            LogUtil.e("Failed to log widget added event", e)
        }
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // 모든 위젯이 제거될 때 호출
        LogUtil.d("Widget disabled (all widgets removed)")

        // Analytics 사용자 속성 업데이트
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                AnalyticsEntryPoint::class.java
            )
            entryPoint.analyticsHelper().setWidgetUser(false)
        } catch (e: Exception) {
            LogUtil.e("Failed to update widget user property", e)
        }
    }
}
