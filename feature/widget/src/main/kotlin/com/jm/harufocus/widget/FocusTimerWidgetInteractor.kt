package com.jm.harufocus.widget

import android.content.Context
import android.content.Intent
import com.jm.harufocus.common.TimerServiceAction
import com.jm.harufocus.domain.repository.PresetRepository
import com.jm.harufocus.domain.repository.SettingsRepository
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
class FocusTimerWidgetInteractor @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val presetRepository: PresetRepository,
    private val widgetStateManager: FocusTimerWidgetStateManager
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
        
        val duration = if (currentWidgetTime > Duration.ZERO) {
            currentWidgetTime
        } else {
            val defaultPresetId = settingsRepository.defaultPresetId.firstOrNull()
            
            val fetchedDuration = if (defaultPresetId != null) {
                presetRepository.getPresetById(defaultPresetId)?.duration
            } else {
                null
            } ?: settingsRepository.defaultSessionDuration.first()
            
            // 가져온 기본값을 위젯 상태에도 반영하여 UI 동기화
            widgetStateManager.updateRemainingTime(fetchedDuration)
            fetchedDuration
        }

        startTimerWithDuration(duration)
    }

    /**
     * 타이머 시간 증가 (1분)
     */
    suspend fun increaseTime() {
        widgetStateManager.updateRemainingTime(1.minutes)
        // 위젯 업데이트 요청은 Receiver에서 처리
    }

    /**
     * 타이머 시간 감소 (1분)
     */
    suspend fun decreaseTime() {
        widgetStateManager.updateRemainingTime((-1).minutes)
    }
    
    /**
     * 특정 시간으로 타이머 시작
     */
    fun startTimerWithDuration(duration: Duration) {
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
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_PAUSE
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
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_RESUME
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
                TimerServiceAction.SERVICE_CLASS_NAME
            )
            action = TimerServiceAction.ACTION_STOP
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