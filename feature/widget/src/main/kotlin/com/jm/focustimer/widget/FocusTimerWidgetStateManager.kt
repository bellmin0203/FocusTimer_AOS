package com.jm.focustimer.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

/**
 * Focus Timer Widget State Manager
 * 
 * 위젯 상태를 DataStore에 저장하고 관리합니다.
 * 타이머 서비스와 위젯 간의 상태 동기화를 담당합니다.
 */
@Singleton
class FocusTimerWidgetStateManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    
    companion object {
        private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "focus_timer_widget"
        )
        
        private val KEY_STATUS = stringPreferencesKey("status")
        private val KEY_REMAINING_TIME = stringPreferencesKey("remaining_time")
        private val KEY_OVERTIME = stringPreferencesKey("overtime")
    }
    
    /**
     * 위젯 상태 Flow
     */
    val widgetState: Flow<FocusTimerWidgetState> = context.widgetDataStore.data
        .map { preferences ->
            val status = preferences[KEY_STATUS] ?: "idle"
            val remainingTime = preferences[KEY_REMAINING_TIME] ?: "00:00"
            val overtime = preferences[KEY_OVERTIME] ?: "+00:00"
            
            when (status) {
                "running" -> FocusTimerWidgetState.Running(remainingTime)
                "paused" -> FocusTimerWidgetState.Paused(remainingTime)
                "completed" -> FocusTimerWidgetState.Completed(overtime)
                else -> FocusTimerWidgetState.Idle
            }
        }
    
    /**
     * 위젯 상태를 Idle로 설정
     */
    suspend fun setIdle() {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_STATUS] = "idle"
            preferences[KEY_REMAINING_TIME] = "00:00"
            preferences[KEY_OVERTIME] = "+00:00"
        }
    }
    
    /**
     * 위젯 상태를 Running으로 설정
     */
    suspend fun setRunning(remainingTime: Duration) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_STATUS] = "running"
            preferences[KEY_REMAINING_TIME] = formatDuration(remainingTime)
        }
    }
    
    /**
     * 위젯 상태를 Paused로 설정
     */
    suspend fun setPaused(remainingTime: Duration) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_STATUS] = "paused"
            preferences[KEY_REMAINING_TIME] = formatDuration(remainingTime)
        }
    }
    
    /**
     * 위젯 상태를 Completed로 설정
     */
    suspend fun setCompleted(overtime: Duration) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_STATUS] = "completed"
            preferences[KEY_OVERTIME] = formatDuration(overtime, includeSign = true)
        }
    }
    
    /**
     * Duration을 "MM:SS" 형식의 문자열로 변환
     */
    private fun formatDuration(duration: Duration, includeSign: Boolean = false): String {
        val totalSeconds = duration.inWholeSeconds
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
