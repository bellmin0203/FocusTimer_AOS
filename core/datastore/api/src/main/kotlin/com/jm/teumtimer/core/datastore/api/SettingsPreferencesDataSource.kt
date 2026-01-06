package com.jm.teumtimer.core.datastore.api

import com.jm.teumtimer.common.model.NotificationSoundType
import com.jm.teumtimer.common.model.ThemeMode
import com.jm.teumtimer.common.model.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface SettingsPreferencesDataSource {
    val themeModeFlow: Flow<ThemeMode>

    val notificationSoundTypeFlow: Flow<NotificationSoundType>

    val isNotificationVibrateFlow: Flow<Boolean>

    val isTickSoundFlow: Flow<Boolean>

    val defaultSessionDurationFlow: Flow<Duration>

    val isRememberLastSessionFlow: Flow<Boolean>

    val timeUnitFlow: Flow<TimeUnit>

    val isScreenOnFlow: Flow<Boolean>

    val isHapticFeedbackFlow: Flow<Boolean>

    val isMinimizedControlsFlow: Flow<Boolean>

    /**
     * 기본 프리셋 ID Flow
     * 설정되지 않은 경우 null 반환
     */
    val defaultPresetIdFlow: Flow<Int?>

    val isPulseAnimationEnabledFlow: Flow<Boolean>

    suspend fun updateThemeMode(themeMode: ThemeMode)

    suspend fun updateNotificationSoundType(soundType: NotificationSoundType)

    suspend fun updateIsNotificationVibrate(isVibrate: Boolean)

    suspend fun updateIsTickSound(isTickSound: Boolean)

    suspend fun updateDefaultSessionDuration(duration: Duration)

    suspend fun updateIsRememberLastSession(isRememberLastSession: Boolean)

    suspend fun updateTimeUnit(timeUnit: TimeUnit)

    suspend fun updateIsScreenOn(isScreenOn: Boolean)

    suspend fun updateIsHapticFeedback(isHapticFeedback: Boolean)

    suspend fun updateIsMinimizedControls(isMinimizedControls: Boolean)

    /**
     * 기본 프리셋 ID를 업데이트합니다.
     * @param presetId 설정할 프리셋 ID (null인 경우 기본값 제거)
     */
    suspend fun updateDefaultPresetId(presetId: Int?)

    suspend fun updateIsPulseAnimationEnabled(isEnabled: Boolean)

    companion object {
        val DEFAULT_THEME_MODE = ThemeMode.SYSTEM
        const val DEFAULT_IS_NOTIFICATION_VIBRATE = false
        const val DEFAULT_IS_TICK_SOUND = false
        val DEFAULT_SESSION_DURATION = 15.minutes
        const val DEFAULT_IS_REMEMBER_LAST_SESSION = true
        val DEFAULT_TIME_UNIT = TimeUnit.MINUTE
        const val DEFAULT_IS_SCREEN_ON = true
        const val DEFAULT_IS_HAPTIC_FEEDBACK = false
        const val DEFAULT_IS_MINIMIZED_CONTROLS = true
        const val DEFAULT_IS_PULSE_ANIMATION_ENABLED = true
    }
}
