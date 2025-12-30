package com.jm.focustimer.data.repository

import com.jm.focustimer.common.model.NotificationSoundType
import com.jm.focustimer.common.model.ThemeMode
import com.jm.focustimer.common.model.TimeUnit
import com.jm.focustimer.core.datastore.api.SettingsPreferencesDataSource
import com.jm.focustimer.domain.repository.SettingsRepository
import com.jm.logutil.LogUtil
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * SettingsRepository의 기본 구현체
 * DataStore를 통해 설정 데이터를 관리합니다.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataSource: SettingsPreferencesDataSource
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode>
        get() = settingsDataSource.themeModeFlow

    override val notificationSoundType: Flow<NotificationSoundType>
        get() = settingsDataSource.notificationSoundTypeFlow

    override val isNotificationVibrate: Flow<Boolean>
        get() = settingsDataSource.isNotificationVibrateFlow

    override val isTickSound: Flow<Boolean>
        get() = settingsDataSource.isTickSoundFlow

    override val defaultSessionDuration: Flow<Duration>
        get() = settingsDataSource.defaultSessionDurationFlow

    override val isRememberLastSession: Flow<Boolean>
        get() = settingsDataSource.isRememberLastSessionFlow

    override val timeUnit: Flow<TimeUnit>
        get() = settingsDataSource.timeUnitFlow

    override val isScreenOn: Flow<Boolean>
        get() = settingsDataSource.isScreenOnFlow

    override val isHapticFeedback: Flow<Boolean>
        get() = settingsDataSource.isHapticFeedbackFlow

    override val isMinimizedControls: Flow<Boolean>
        get() = settingsDataSource.isMinimizedControlsFlow

    override val defaultPresetId: Flow<Int?>
        get() = settingsDataSource.defaultPresetIdFlow

    override val isPulseAnimationEnabled: Flow<Boolean>
        get() = settingsDataSource.isPulseAnimationEnabledFlow

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        LogUtil.d("테마 설정 변경: $themeMode")
        settingsDataSource.updateThemeMode(themeMode)
    }

    override suspend fun updateNotificationSoundType(soundType: NotificationSoundType) {
        require(soundType != NotificationSoundType.CUSTOM) {
            "CUSTOM 사운드 타입은 직접 선택할 수 없습니다"
        }
        settingsDataSource.updateNotificationSoundType(soundType)
    }

    override suspend fun updateNotificationVibrate(isVibrate: Boolean) {
        settingsDataSource.updateIsNotificationVibrate(isVibrate)
    }

    override suspend fun updateTickSound(isTickSound: Boolean) {
        settingsDataSource.updateIsTickSound(isTickSound)
    }

    override suspend fun updateDefaultSessionDuration(duration: Duration) {
        require(duration >= 1.minutes && duration <= 60.minutes) {
            "세션 시간은 1분 이상 60분 이하여야 합니다"
        }
        settingsDataSource.updateDefaultSessionDuration(duration)
    }

    override suspend fun updateRememberLastSession(isRememberLastSession: Boolean) {
        settingsDataSource.updateIsRememberLastSession(isRememberLastSession)
    }

    override suspend fun updateTimeUnit(timeUnit: TimeUnit) {
        settingsDataSource.updateTimeUnit(timeUnit)
    }

    override suspend fun updateScreenOn(isScreenOn: Boolean) {
        settingsDataSource.updateIsScreenOn(isScreenOn)
    }

    override suspend fun updateHapticFeedback(isHapticFeedback: Boolean) {
        settingsDataSource.updateIsHapticFeedback(isHapticFeedback)
    }

    override suspend fun updateMinimizedControls(isMinimizedControls: Boolean) {
        settingsDataSource.updateIsMinimizedControls(isMinimizedControls)
    }

    override suspend fun updateDefaultPresetId(presetId: Int?) {
        if (presetId != null) {
            LogUtil.d("기본 프리셋 설정: presetId=$presetId")
        } else {
            LogUtil.d("기본 프리셋 해제")
        }
        settingsDataSource.updateDefaultPresetId(presetId)
    }

    override suspend fun updatePulseAnimationEnabled(isEnabled: Boolean) {
        settingsDataSource.updateIsPulseAnimationEnabled(isEnabled)
    }
}