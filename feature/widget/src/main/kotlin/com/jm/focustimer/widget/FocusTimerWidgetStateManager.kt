package com.jm.focustimer.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

/**
 * Focus Timer Widget State Manager
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
     * 위젯 상태를 Idle로 설정
     */
    suspend fun setIdle(presetColorIndex: Int? = null) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = FocusTimerWidgetState.IDLE
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
