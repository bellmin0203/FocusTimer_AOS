package com.jm.harufocus

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jm.harufocus.timer.service.TimerNotificationHelper
import com.jm.logutil.LogUtil
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class HaruFocusApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        LogUtil.init(context = this, tag = "HaruFocusApp")
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // 타이머 알림 채널 생성 (메인 스레드 차단 방지)
        CoroutineScope(Dispatchers.Default).launch {
            TimerNotificationHelper(this@HaruFocusApplication).createNotificationChannel()
        }
    }
}