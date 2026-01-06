package com.jm.teumtimer

import android.app.Application
import com.jm.logutil.LogUtil
import com.jm.teumtimer.timer.service.TimerNotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusTimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        LogUtil.init(context = this, tag = "FocusTimerApp")
        
        // 타이머 알림 채널 생성
        TimerNotificationHelper(this).createNotificationChannel()
    }
}