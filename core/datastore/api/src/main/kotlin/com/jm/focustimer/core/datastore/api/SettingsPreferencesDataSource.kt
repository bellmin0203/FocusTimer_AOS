package com.jm.focustimer.core.datastore.api

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

interface SettingsPreferencesDataSource {
    val isDarkThemeFlow: Flow<Boolean>

    val isNotificationVibrateFlow: Flow<Boolean>

    val isTickSoundFlow: Flow<Boolean>

    val defaultSessionDurationFlow: Flow<Duration>

    val isRememberLastSessionFlow: Flow<Boolean>

    val timeUnitFlow: Flow<String>

    val isScreenOnFlow: Flow<Boolean>

    val isRemainingTimeDisplayFlow: Flow<Boolean>

    val isHapticFeedbackFlow: Flow<Boolean>

    val isMinimizedControlsFlow: Flow<Boolean>

    /**
     * 기본 프리셋 ID Flow
     * 설정되지 않은 경우 null 반환
     */
    val defaultPresetIdFlow: Flow<Int?>

    suspend fun updateIsDarkTheme(isDarkTheme: Boolean)

    suspend fun updateIsNotificationVibrate(isVibrate: Boolean)

    suspend fun updateIsTickSound(isTickSound: Boolean)

    suspend fun updateDefaultSessionDuration(duration: Duration)

    suspend fun updateIsRememberLastSession(isRememberLastSession: Boolean)

    suspend fun updateTimeUnit(timeUnit: String)

    suspend fun updateIsScreenOn(isScreenOn: Boolean)

    suspend fun updateIsRemainingTimeDisplay(isRemainingTimeDisplay: Boolean)

    suspend fun updateIsHapticFeedback(isHapticFeedback: Boolean)

    suspend fun updateIsMinimizedControls(isMinimizedControls: Boolean)

    /**
     * 기본 프리셋 ID를 업데이트합니다.
     * @param presetId 설정할 프리셋 ID (null인 경우 기본값 제거)
     */
    suspend fun updateDefaultPresetId(presetId: Int?)
}