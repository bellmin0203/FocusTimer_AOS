package com.jm.focustimer

import android.app.Application
import com.jm.logutil.LogUtil
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FocusTimerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        LogUtil.init(context = this, tag = "FocusTimerApp")
    }
}