package com.jm.harufocus.widget

import android.content.Context
import android.content.Intent
import com.jm.harufocus.common.TimerServiceAction
import com.jm.harufocus.domain.repository.PresetRepository
import com.jm.harufocus.domain.repository.SettingsRepository
import com.jm.harufocus.util.CrashReporter
import com.jm.logutil.LogUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 하루 몰입 Widget Interactor
 * 
 * 위젯과 타이머 서비스 간의 통신을 담당하는 클래스
 */
@Singleton
class HaruFocusWidgetInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val presetRepository: PresetRepository,
    private val widgetStateManager: HaruFocusWidgetStateManager
) {
    
    /**
     * 타이머 시작
     * 
     * 1. 위젯에 설정된 시간이 있다면 그 시간으로 시작
     * 2. 없다면 기본 프리셋이나 설정된 시간으로 시작
     */
    suspend fun startTimer() {
        // 위젯에 현재 설정된 시간 확인 (사용자가 조절했을 수 있음)
        val currentWidgetTime = widgetStateManager.getRemainingTime()
        LogUtil.d("startTimer: currentWidgetTime=$currentWidgetTime")

        val duration = if (currentWidgetTime > Duration.ZERO) {
            LogUtil.d("startTimer: Using widget time")
            currentWidgetTime
        } else {
            val defaultPresetId = settingsRepository.defaultPresetId.firstOrNull()
            LogUtil.d("startTimer: defaultPresetId=$defaultPresetId")

            val fetchedDuration = if (defaultPresetId != null) {
                presetRepository.getPresetById(defaultPresetId)?.duration
            } else {
                null
            } ?: settingsRepository.defaultSessionDuration.first()
            
            LogUtil.d("startTimer: fetchedDuration=$fetchedDuration")

            // 가져온 기본값을 위젯 상태에도 반영하여 UI 동기화
            widgetStateManager.updateRemainingTime(fetchedDuration)
            fetchedDuration
        }

        LogUtil.d("startTimer: Starting with duration=$duration")
        startTimerWithDuration(duration)
    }

    /**
     * 타이머 시간 증가 (1분)
     */
    suspend fun increaseTime() {
        LogUtil.d("increaseTime: +1 minute")
        widgetStateManager.updateRemainingTime(1.minutes)
        // 위젯 업데이트 요청은 Receiver에서 처리
    }

    /**
     * 타이머 시간 감소 (1분)
     */
    suspend fun decreaseTime() {
        LogUtil.d("decreaseTime: -1 minute")
        widgetStateManager.updateRemainingTime((-1).minutes)
    }
    
    /**
     * 특정 시간으로 타이머 시작
     */
    fun startTimerWithDuration(duration: Duration) {
        LogUtil.d("startTimerWithDuration: duration=$duration")
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_START
            putExtra(TimerServiceAction.EXTRA_DURATION, duration.inWholeMilliseconds)
        }
        
        try {
            context.startForegroundService(serviceIntent)
            LogUtil.d("startTimerWithDuration: Service started successfully")
        } catch (e: Exception) {
            LogUtil.e("startTimerWithDuration: Failed to start service", e)
            CrashReporter.recordException(e, "Widget: 타이머 서비스 시작 실패")
        }
    }
    
    /**
     * 타이머 일시정지
     */
    fun pauseTimer() {
        LogUtil.d("pauseTimer")
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_PAUSE
        }
        
        try {
            context.startService(serviceIntent)
            LogUtil.d("pauseTimer: Service command sent")
        } catch (e: Exception) {
            LogUtil.e("pauseTimer: Failed", e)
            CrashReporter.recordException(e, "Widget: 타이머 일시정지 실패")
        }
    }
    
    /**
     * 타이머 재개
     */
    fun resumeTimer() {
        LogUtil.d("resumeTimer")
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_RESUME
        }
        
        try {
            context.startService(serviceIntent)
            LogUtil.d("resumeTimer: Service command sent")
        } catch (e: Exception) {
            LogUtil.e("resumeTimer: Failed", e)
            CrashReporter.recordException(e, "Widget: 타이머 재개 실패")
        }
    }

    /**
     * 타이머 정지
     */
    fun stopTimer() {
        LogUtil.d("stopTimer")
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_STOP
        }

        try {
            context.startService(serviceIntent)
            LogUtil.d("stopTimer: Service command sent")
        } catch (e: Exception) {
            LogUtil.e("stopTimer: Failed", e)
            CrashReporter.recordException(e, "Widget: 타이머 정지 실패")
        }
    }

    /**
     * 타이머 완료 (초과 시간 있는 경우 세션 업데이트 후 리셋)
     */
    fun completeTimer() {
        LogUtil.d("completeTimer")
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_COMPLETE
        }

        try {
            context.startService(serviceIntent)
            LogUtil.d("completeTimer: Service command sent")
        } catch (e: Exception) {
            LogUtil.e("completeTimer: Failed", e)
            CrashReporter.recordException(e, "Widget: 타이머 완료 실패")
        }
    }

    /**
     * 앱 열기
     * 
     * 메인 액티비티를 실행하여 앱으로 진입합니다.
     */
    fun openApp(context: Context) {
        LogUtil.d("openApp")
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
            LogUtil.d("openApp: App launched")
        } ?: LogUtil.w("openApp: Launch intent not found")
    }
}