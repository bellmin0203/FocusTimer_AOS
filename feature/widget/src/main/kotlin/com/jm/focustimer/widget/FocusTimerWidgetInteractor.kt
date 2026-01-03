package com.jm.focustimer.widget

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Focus Timer Widget Interactor
 * 
 * 위젯과 타이머 서비스 간의 통신을 담당하는 클래스
 */
@Singleton
class FocusTimerWidgetInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    /**
     * 타이머 시작
     * 
     * 기본 25분 타이머를 시작합니다.
     * TODO: 프리셋이나 설정에서 기본 시간을 가져오도록 개선
     */
    fun startTimer() {
        val defaultDuration = 25.minutes
        startTimerWithDuration(defaultDuration)
    }
    
    /**
     * 특정 시간으로 타이머 시작
     */
    fun startTimerWithDuration(duration: Duration) {
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                "com.jm.focustimer.timer.service.TimerService"
            )
            action = "com.jm.focustimer.action.START"
            putExtra("extra_duration", duration.inWholeMilliseconds)
        }
        
        try {
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            // 서비스 시작 실패 처리
            e.printStackTrace()
        }
    }
    
    /**
     * 타이머 일시정지
     */
    fun pauseTimer() {
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                "com.jm.focustimer.timer.service.TimerService"
            )
            action = "com.jm.focustimer.action.PAUSE"
        }
        
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 타이머 재개
     */
    fun resumeTimer() {
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                "com.jm.focustimer.timer.service.TimerService"
            )
            action = "com.jm.focustimer.action.RESUME"
        }
        
        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 타이머 정지
     */
    fun stopTimer() {
        val serviceIntent = Intent().apply {
            setClassName(
                context.packageName,
                "com.jm.focustimer.timer.service.TimerService"
            )
            action = "com.jm.focustimer.action.STOP"
        }

        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 앱 열기
     * 
     * 메인 액티비티를 실행하여 앱으로 진입합니다.
     */
    fun openApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}
