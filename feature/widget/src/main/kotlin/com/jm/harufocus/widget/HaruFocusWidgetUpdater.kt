package com.jm.harufocus.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.util.CrashReporter
import com.jm.logutil.LogUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

/**
 * 하루 몰입 Widget Updater
 *
 * 타이머 서비스에서 위젯 상태를 업데이트하기 위한 헬퍼 클래스
 */
@Singleton
class HaruFocusWidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val stateManager: HaruFocusWidgetStateManager
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 마지막 위젯 업데이트 시간 (throttle 용)
    private var lastWidgetUpdateTime = 0L

    // 위젯 업데이트 간격 (5초)
    private val widgetUpdateInterval = 5000L

    /**
     * 타이머가 시작됨
     */
    fun onTimerStarted(remainingTime: Duration, preset: Preset? = null) {
        LogUtil.d("onTimerStarted: remainingTime=$remainingTime, preset=${preset?.name}")
        scope.launch {
            stateManager.setRunning(
                remainingTime = remainingTime,
                presetColorIndex = preset?.colorIndex
            )
            updateWidget()
        }
    }

    /**
     * 타이머 시간이 설정됨 (Idle 상태에서 시간 변경 시)
     *
     * 앱에서 시간을 설정하면 위젯의 Idle 상태 시간도 동기화합니다.
     */
    fun onTimeSet(duration: Duration, preset: Preset? = null) {
        LogUtil.d("onTimeSet: duration=$duration, preset=${preset?.name}")
        scope.launch {
            stateManager.setIdleWithDuration(
                duration = duration,
                preset = preset
            )
            updateWidget()
        }
    }

    /**
     * 타이머가 일시정지됨
     */
    fun onTimerPaused(remainingTime: Duration, preset: Preset? = null) {
        LogUtil.d("onTimerPaused: remainingTime=$remainingTime, preset=${preset?.name}")
        scope.launch {
            stateManager.setPaused(
                remainingTime = remainingTime,
                presetColorIndex = preset?.colorIndex
            )
            updateWidget()
        }
    }

    /**
     * 타이머가 재개됨
     */
    fun onTimerResumed(remainingTime: Duration, preset: Preset? = null) {
        LogUtil.d("onTimerResumed: remainingTime=$remainingTime, preset=${preset?.name}")
        scope.launch {
            stateManager.setRunning(
                remainingTime = remainingTime,
                presetColorIndex = preset?.colorIndex
            )
            updateWidget()
        }
    }

    /**
     * 타이머가 완료됨 (남은 시간이 0이 된 순간)
     */
    fun onTimerCompleted(preset: Preset? = null) {
        LogUtil.d("onTimerCompleted: preset=${preset?.name}")
        scope.launch {
            stateManager.setCompleted(presetColorIndex = preset?.colorIndex)
            updateWidget()
        }
    }

    /**
     * 타이머가 초과 시간 상태로 진입
     */
    fun onTimerOvertime(overtime: Duration, preset: Preset? = null) {
        LogUtil.d("onTimerOvertime: overtime=$overtime, preset=${preset?.name}")
        scope.launch {
            stateManager.setOvertime(overtime = overtime, presetColorIndex = preset?.colorIndex)
            updateWidget()
        }
    }

    /**
     * 타이머가 중지됨 (리셋)
     */
    fun onTimerStopped(preset: Preset? = null) {
        LogUtil.d("onTimerStopped: preset=${preset?.name}")
        scope.launch {
            stateManager.setIdle(preset = preset)
            updateWidget()
        }
    }

    /**
     * 타이머 틱 (진행 중 업데이트)
     *
     * 매초마다 위젯을 업데이트하는 것은 부담이 될 수 있으므로
     * throttle을 적용하여 5초마다 한 번씩만 업데이트합니다.
     */
    fun onTimerTick(
        remainingTime: Duration,
        overtime: Duration = Duration.ZERO,
        presetColorIndex: Int = 0
    ) {
        val currentTime = System.currentTimeMillis()

//        // throttle: 마지막 업데이트로부터 일정 시간이 지났을 때만 업데이트
//        if (currentTime - lastWidgetUpdateTime < widgetUpdateInterval) {
//            // 상태는 업데이트하지만 위젯 UI는 업데이트하지 않음
//            scope.launch {
//                stateManager.setRunning(remainingTime, presetColorIndex)
//            }
//            return
//        }

        lastWidgetUpdateTime = currentTime

        scope.launch {
            stateManager.setRunning(
                remainingTime = remainingTime,
                overtime = overtime,
                presetColorIndex = presetColorIndex
            )
            updateWidget()
        }
    }

    /**
     * 위젯 UI 업데이트
     *
     * Glance 위젯을 강제로 다시 렌더링합니다.
     */
    private suspend fun updateWidget() {
        LogUtil.d("updateWidget: Refreshing all widgets")
        try {
            HaruFocusWidget().updateAll(context)
            LogUtil.d("updateWidget: Success")
        } catch (e: Exception) {
            LogUtil.e("updateWidget: Failed", e)
            CrashReporter.recordException(e, "위젯 업데이트 실패")
        }
    }
}
