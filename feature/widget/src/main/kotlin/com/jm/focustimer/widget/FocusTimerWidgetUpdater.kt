package com.jm.focustimer.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

/**
 * Focus Timer Widget Updater
 * 
 * 타이머 서비스에서 위젯 상태를 업데이트하기 위한 헬퍼 클래스
 */
@Singleton
class FocusTimerWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateManager: FocusTimerWidgetStateManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    /**
     * 타이머가 시작됨
     */
    fun onTimerStarted(remainingTime: Duration) {
        scope.launch {
            stateManager.setRunning(remainingTime)
            updateWidget()
        }
    }
    
    /**
     * 타이머가 일시정지됨
     */
    fun onTimerPaused(remainingTime: Duration) {
        scope.launch {
            stateManager.setPaused(remainingTime)
            updateWidget()
        }
    }
    
    /**
     * 타이머가 재개됨
     */
    fun onTimerResumed(remainingTime: Duration) {
        scope.launch {
            stateManager.setRunning(remainingTime)
            updateWidget()
        }
    }
    
    /**
     * 타이머가 완료됨
     */
    fun onTimerCompleted(overtime: Duration = Duration.ZERO) {
        scope.launch {
            stateManager.setCompleted(overtime)
            updateWidget()
        }
    }
    
    /**
     * 타이머가 중지됨 (리셋)
     */
    fun onTimerStopped() {
        scope.launch {
            stateManager.setIdle()
            updateWidget()
        }
    }
    
    /**
     * 타이머 틱 (진행 중 업데이트)
     */
    fun onTimerTick(remainingTime: Duration) {
        scope.launch {
            stateManager.setRunning(remainingTime)
            // 매초마다 위젯을 업데이트하는 것은 부담이 될 수 있으므로
            // 필요시 throttle 적용 고려
            updateWidget()
        }
    }
    
    /**
     * 위젯 UI 업데이트
     */
    private suspend fun updateWidget() {
        FocusTimerWidget().updateAll(context)
    }
}
