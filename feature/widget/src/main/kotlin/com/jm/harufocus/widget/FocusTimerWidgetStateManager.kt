package com.jm.harufocus.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * 하루 몰입 Widget State Manager
 *
 * 위젯 상태를 Glance Preferences에 저장하고 관리합니다.
 * 타이머 서비스와 위젯 간의 상태 동기화를 담당합니다.
 */
@Singleton
class FocusTimerWidgetStateManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {
        val KEY_STATUS = stringPreferencesKey("status")
        val KEY_REMAINING_TIME = longPreferencesKey("remaining_time")
        val KEY_OVERTIME = longPreferencesKey("overtime")
        val KEY_PRESET_COLOR_INDEX = stringPreferencesKey("preset_color_index")
    }

    /**
     * 현재 저장된 남은 시간을 반환합니다.
     */
    suspend fun getRemainingTime(): Duration {
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(FocusTimerWidget::class.java)
        if (glanceIds.isEmpty()) return FocusTimerWidgetState.DEFAULT_REMAINING_TIME.milliseconds

        // 첫 번째 위젯의 상태를 기준으로 함
        val glanceId = glanceIds.first()
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        return (prefs[KEY_REMAINING_TIME] ?: FocusTimerWidgetState.DEFAULT_REMAINING_TIME).milliseconds
    }

    /**
     * 남은 시간을 조절합니다 (1분 ~ 59분 제한).
     */
    suspend fun updateRemainingTime(delta: Duration) {
        updateAllWidgets { prefs ->
            val currentMillis = prefs[KEY_REMAINING_TIME] ?: FocusTimerWidgetState.DEFAULT_REMAINING_TIME
            val currentDuration = if (currentMillis == 0L) 25.minutes else currentMillis.milliseconds
            val newTime = (currentDuration + delta).coerceIn(1.minutes, 59.minutes)
            
            prefs[KEY_REMAINING_TIME] = newTime.inWholeMilliseconds
        }
    }

    /**
     * 위젯 상태를 Idle로 설정
     */
    suspend fun setIdle(presetColorIndex: Int? = null) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = FocusTimerWidgetState.IDLE
            // Idle 상태로 돌아갈 때 시간을 초기화하지 않고 마지막 시간을 유지하거나 기본값으로 설정할 수 있음
            // 여기서는 0으로 초기화되던 것을 유지하되, Interactor에서 초기화 로직을 담당하도록 할 수 있음
            // 기존 로직: prefs[KEY_REMAINING_TIME] = FocusTimerWidgetState.DEFAULT_REMAINING_TIME (0)
            // 변경: 타이머가 종료/정지되어 Idle로 갈 때 0으로 만들면 화면에 00:00이 뜸.
            // 사용성을 위해 Idle 상태에서도 기본 시간(예: 25분)이나 마지막 설정 시간을 보여주는 게 좋음.
            // 일단 기존 동작(0으로 초기화)을 유지하되, 필요 시 수정.
            prefs[KEY_REMAINING_TIME] = FocusTimerWidgetState.DEFAULT_REMAINING_TIME
            prefs[KEY_PRESET_COLOR_INDEX] = presetColorIndex.toString()
            prefs.remove(KEY_OVERTIME)
        }
    }

    /**
     * 위젯 상태를 Running으로 설정
     */
    suspend fun setRunning(
        remainingTime: Duration,
        overtime: Duration = Duration.ZERO,
        presetColorIndex: Int? = null
    ) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = FocusTimerWidgetState.RUNNING
            prefs[KEY_REMAINING_TIME] = remainingTime.inWholeMilliseconds
            prefs[KEY_PRESET_COLOR_INDEX] = presetColorIndex.toString()

            if (overtime > Duration.ZERO) {
                prefs[KEY_OVERTIME] = overtime.inWholeMilliseconds
            }
        }
    }

    /**
     * 위젯 상태를 Paused로 설정
     */
    suspend fun setPaused(remainingTime: Duration, presetColorIndex: Int? = null) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = FocusTimerWidgetState.PAUSED
            prefs[KEY_REMAINING_TIME] = remainingTime.inWholeMilliseconds
            prefs[KEY_PRESET_COLOR_INDEX] = presetColorIndex.toString()
        }
    }

    /**
     * 위젯 상태를 Completed로 설정
     */
    suspend fun setCompleted(overtime: Duration, presetColorIndex: Int? = null) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = FocusTimerWidgetState.COMPLETED
            prefs[KEY_OVERTIME] = overtime.inWholeMilliseconds
            prefs[KEY_PRESET_COLOR_INDEX] = presetColorIndex.toString()
        }
    }

    /**
     * 모든 위젯의 상태를 업데이트
     */
    private suspend fun updateAllWidgets(
        updateBlock: suspend (MutablePreferences) -> Unit
    ) {
        val glanceIds = GlanceAppWidgetManager(context)
            .getGlanceIds(FocusTimerWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId
            ) { prefs ->
                val mutablePrefs = prefs.toMutablePreferences()
                updateBlock(mutablePrefs)
                mutablePrefs
            }
        }
    }
}
