package com.jm.focustimer.core.datastore.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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

    override val defaultPresetIdFlow: Flow<Int?> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_PRESET_ID]
    }

    override suspend fun updateIsDarkTheme(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_DARK_THEME] = isDarkTheme
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
        private val KEY_IS_NOTIFICATION_VIBRATE = booleanPreferencesKey("is_notification_vibrate")
        private val KEY_IS_TICK_SOUND = booleanPreferencesKey("is_tick_sound")
        private val KEY_DEFAULT_SESSION_DURATION = longPreferencesKey("default_session_duration")
        private val KEY_IS_REMEMBER_LAST_SESSION = booleanPreferencesKey("is_remember_last_session")
        private val KEY_DEFAULT_PRESET_ID = intPreferencesKey("default_preset_id")

    }
}