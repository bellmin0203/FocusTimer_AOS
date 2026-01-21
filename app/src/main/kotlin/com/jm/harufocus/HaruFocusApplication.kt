package com.jm.harufocus

import android.app.Application
import com.jm.harufocus.timer.service.TimerNotificationHelper
import com.jm.harufocus.util.AnalyticsHelper
import com.jm.harufocus.util.CrashReporter
import com.jm.harufocus.widget.HaruFocusWidgetUpdater
import com.jm.logutil.LogUtil
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class HaruFocusApplication : Application() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetUpdaterEntryPoint {
        fun widgetUpdater(): HaruFocusWidgetUpdater
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AnalyticsEntryPoint {
        fun analyticsHelper(): AnalyticsHelper
    }

    override fun onCreate() {
        super.onCreate()

        LogUtil.init(context = this, tag = "HaruFocusApp")
        CrashReporter.init(isEnabled = !BuildConfig.DEBUG)

        // Analytics는 Release 빌드에서만 활성화
        initializeAnalytics()

        // 타이머 알림 채널 생성 및 위젯 상태 초기화 (메인 스레드 차단 방지)
        CoroutineScope(Dispatchers.Default).launch {
            TimerNotificationHelper(this@HaruFocusApplication).createNotificationChannel()

            // 앱이 시작될 때 위젯 상태를 초기화 (Idle)
            // 앱이 재설치되거나 강제 종료된 경우, 타이머 서비스가 실행 중이지 않으므로
            // 위젯 상태도 Idle로 초기화해야 합니다.
            resetWidgetStateToIdle()
        }
    }

    /**
     * Analytics 초기화
     *
     * Release 빌드에서만 Analytics 수집을 활성화합니다.
     * Debug 빌드에서는 개발 중 테스트 데이터가 실제 분석 데이터에
     * 섞이는 것을 방지하기 위해 비활성화합니다.
     */
    private fun initializeAnalytics() {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                this,
                AnalyticsEntryPoint::class.java
            )
            val analyticsHelper = entryPoint.analyticsHelper()
            val isEnabled = !BuildConfig.DEBUG
            analyticsHelper.setAnalyticsCollectionEnabled(isEnabled)
            LogUtil.d("Analytics 초기화 완료: enabled=$isEnabled")
        } catch (e: Exception) {
            LogUtil.e("Analytics 초기화 실패", e)
            CrashReporter.recordException(e, "Analytics 초기화 실패")
        }
    }

    /**
     * 위젯 상태를 Idle로 초기화
     *
     * 앱이 시작될 때 타이머 서비스가 실행 중이지 않으면 위젯 상태가
     * 이전 상태(RUNNING 등)로 남아있을 수 있습니다.
     * 이를 방지하기 위해 앱 시작 시 위젯 상태를 Idle로 초기화합니다.
     */
    private fun resetWidgetStateToIdle() {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                this,
                WidgetUpdaterEntryPoint::class.java
            )
            val widgetUpdater = entryPoint.widgetUpdater()
            widgetUpdater.onTimerStopped()
            LogUtil.d("위젯 상태를 Idle로 초기화 완료")
        } catch (e: Exception) {
            LogUtil.e("위젯 상태 초기화 실패", e)
            CrashReporter.recordException(e, "위젯 상태 초기화 실패")
        }
    }
}