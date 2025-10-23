package com.jm.focustimer.data.repository

import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource
import com.jm.focustimer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration

/**
 * SettingsRepository의 기본 구현체
 * DataStore를 통해 설정 데이터를 관리합니다.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsPreferencesDataSource
) : SettingsRepository {

    override val isDarkTheme: Flow<Boolean>
        get() = settingsDataSource.isDarkThemeFlow

    override val isNotificationVibrate: Flow<Boolean>
        get() = settingsDataSource.isNotificationVibrateFlow

    override val isTickSound: Flow<Boolean>
        get() = settingsDataSource.isTickSoundFlow

    override val defaultSessionDuration: Flow<Duration>
        get() = settingsDataSource.defaultSessionDurationFlow

    override val isRememberLastSession: Flow<Boolean>
        get() = settingsDataSource.isRememberLastSessionFlow

    override val defaultPresetId: Flow<Int?>
        get() = settingsDataSource.defaultPresetIdFlow

    override suspend fun updateDarkTheme(isDarkTheme: Boolean) {
        settingsDataSource.updateIsDarkTheme(isDarkTheme)
    }

    override suspend fun updateNotificationVibrate(isVibrate: Boolean) {
        settingsDataSource.updateIsNotificationVibrate(isVibrate)
    }

    override suspend fun updateTickSound(isTickSound: Boolean) {
        settingsDataSource.updateIsTickSound(isTickSound)
    }

    override suspend fun updateDefaultSessionDuration(duration: Duration) {
        settingsDataSource.updateDefaultSessionDuration(duration)
    }

    override suspend fun updateRememberLastSession(isRememberLastSession: Boolean) {
        settingsDataSource.updateIsRememberLastSession(isRememberLastSession)
    }

    override suspend fun updateDefaultPresetId(presetId: Int?) {
        settingsDataSource.updateDefaultPresetId(presetId)
    }
}
