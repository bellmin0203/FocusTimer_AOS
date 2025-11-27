package com.jm.focustimer.core.datastore.impl


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jm.focustimer.common.model.NotificationSoundType
import com.jm.focustimer.common.model.TimeUnit
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class DefaultSettingsPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsPreferencesDataSource {

    override val isDarkThemeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_DARK_THEME] ?: false
    }

    override val notificationSoundTypeFlow: Flow<NotificationSoundType> =
        dataStore.data.map { preferences ->
            val soundTypeString =
                preferences[KEY_NOTIFICATION_SOUND_TYPE] ?: NotificationSoundType.DEFAULT.name
            NotificationSoundType.fromString(soundTypeString)
    }

    override val isNotificationVibrateFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_NOTIFICATION_VIBRATE] ?: false
    }
    override val isTickSoundFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_TICK_SOUND] ?: false
    }
    override val defaultSessionDurationFlow: Flow<Duration> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_SESSION_DURATION]?.minutes ?: 15.minutes
    }
    override val isRememberLastSessionFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_REMEMBER_LAST_SESSION] ?: true
    }

    override val timeUnitFlow: Flow<TimeUnit> = dataStore.data.map { preferences ->
        TimeUnit.fromString(preferences[KEY_TIME_UNIT] ?: TimeUnit.MINUTE.name)
    }

    override val isScreenOnFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_SCREEN_ON] ?: false
    }

    override val isRemainingTimeDisplayFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_REMAINING_TIME_DISPLAY] ?: false
    }

    override val isHapticFeedbackFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_HAPTIC_FEEDBACK] ?: false
    }

    override val isMinimizedControlsFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_MINIMIZED_CONTROLS] ?: false
    }

    override val defaultPresetIdFlow: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_PRESET_ID]
    }

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_THEME] = isDarkTheme
        }
    }

    override suspend fun updateNotificationSoundType(soundType: NotificationSoundType) {
        dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_SOUND_TYPE] = soundType.name
        }
    }

    override suspend fun updateIsNotificationVibrate(isVibrate: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_NOTIFICATION_VIBRATE] = isVibrate
        }
    }

    override suspend fun updateIsTickSound(isTickSound: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_TICK_SOUND] = isTickSound
        }
    }

    override suspend fun updateDefaultSessionDuration(duration: Duration) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_SESSION_DURATION] = duration.inWholeMilliseconds
        }
    }

    override suspend fun updateIsRememberLastSession(isRememberLastSession: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_REMEMBER_LAST_SESSION] = isRememberLastSession
        }
    }

    override suspend fun updateTimeUnit(timeUnit: TimeUnit) {
        dataStore.edit { preferences ->
            preferences[KEY_TIME_UNIT] = timeUnit.name
        }
    }

    override suspend fun updateIsScreenOn(isScreenOn: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_SCREEN_ON] = isScreenOn
        }
    }


    override suspend fun updateIsRemainingTimeDisplay(isRemainingTimeDisplay: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_REMAINING_TIME_DISPLAY] = isRemainingTimeDisplay
        }
    }

    override suspend fun updateIsHapticFeedback(isHapticFeedback: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_HAPTIC_FEEDBACK] = isHapticFeedback
        }
    }

    override suspend fun updateIsMinimizedControls(isMinimizedControls: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_MINIMIZED_CONTROLS] = isMinimizedControls
        }
    }

    override suspend fun updateDefaultPresetId(presetId: Int?) {
        dataStore.edit { preferences ->
            if (presetId != null) {
                preferences[KEY_DEFAULT_PRESET_ID] = presetId
            } else {
                preferences.remove(KEY_DEFAULT_PRESET_ID)
            }
        }
    }

    companion object {
        private val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val KEY_NOTIFICATION_SOUND_TYPE = stringPreferencesKey("notification_sound_type")
        private val KEY_IS_NOTIFICATION_VIBRATE = booleanPreferencesKey("is_notification_vibrate")
        private val KEY_IS_TICK_SOUND = booleanPreferencesKey("is_tick_sound")
        private val KEY_DEFAULT_SESSION_DURATION = longPreferencesKey("default_session_duration")
        private val KEY_IS_REMEMBER_LAST_SESSION = booleanPreferencesKey("is_remember_last_session")
        private val KEY_TIME_UNIT = stringPreferencesKey("time_unit")
        private val KEY_IS_SCREEN_ON = booleanPreferencesKey("is_screen_on")
        private val KEY_IS_REMAINING_TIME_DISPLAY = booleanPreferencesKey("is_remaining_time_display")
        private val KEY_IS_HAPTIC_FEEDBACK = booleanPreferencesKey("is_haptic_feedback")
        private val KEY_IS_MINIMIZED_CONTROLS = booleanPreferencesKey("is_minimized_controls")
        private val KEY_DEFAULT_PRESET_ID = intPreferencesKey("default_preset_id")

    }
}