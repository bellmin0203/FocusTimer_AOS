package com.jm.focustimer.widget

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
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
    @ApplicationContext private val context: Context
) {

    companion object {
        private val KEY_STATUS = stringPreferencesKey("status")
        private val KEY_REMAINING_TIME = stringPreferencesKey("remaining_time")
        private val KEY_OVERTIME = stringPreferencesKey("overtime")
    }

    /**
     * 위젯 상태를 Idle로 설정
     */
    suspend fun setIdle() {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = "idle"
            prefs[KEY_REMAINING_TIME] = "00:00"
            prefs[KEY_OVERTIME] = "+00:00"
        }
    }

    /**
     * 위젯 상태를 Running으로 설정
     */
    suspend fun setRunning(remainingTime: Duration) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = "running"
            prefs[KEY_REMAINING_TIME] = formatDuration(remainingTime)
        }
    }

    /**
     * 위젯 상태를 Paused로 설정
     */
    suspend fun setPaused(remainingTime: Duration) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = "paused"
            prefs[KEY_REMAINING_TIME] = formatDuration(remainingTime)
        }
    }

    /**
     * 위젯 상태를 Completed로 설정
     */
    suspend fun setCompleted(overtime: Duration) {
        updateAllWidgets { prefs ->
            prefs[KEY_STATUS] = "completed"
            prefs[KEY_OVERTIME] = formatDuration(overtime, includeSign = true)
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

    /**
     * Duration을 "MM:SS" 형식의 문자열로 변환
     */
    private fun formatDuration(duration: Duration, includeSign: Boolean = false): String {
        val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        val formatted = String.format("%02d:%02d", minutes, seconds)
        return if (includeSign && duration > Duration.ZERO) {
            "+$formatted"
        } else {
            formatted
        }
    }
}
