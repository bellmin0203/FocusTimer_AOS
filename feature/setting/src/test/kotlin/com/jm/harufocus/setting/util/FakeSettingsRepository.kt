package com.jm.harufocus.setting.util

import com.jm.harufocus.common.model.NotificationSoundType
import com.jm.harufocus.common.model.ThemeMode
import com.jm.harufocus.common.model.TimeUnit
import com.jm.harufocus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * SettingsRepository의 In-Memory Fake 구현체
 * 테스트 시 상태 검증을 용이하게 하기 위해 사용
 */
class FakeSettingsRepository : SettingsRepository {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    override val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _notificationSoundType = MutableStateFlow(NotificationSoundType.DEFAULT)
    override val notificationSoundType: StateFlow<NotificationSoundType> = _notificationSoundType.asStateFlow()

    private val _isNotificationVibrate = MutableStateFlow(true)
    override val isNotificationVibrate: StateFlow<Boolean> = _isNotificationVibrate.asStateFlow()

    private val _isTickSound = MutableStateFlow(false)
    override val isTickSound: StateFlow<Boolean> = _isTickSound.asStateFlow()

    private val _defaultSessionDuration = MutableStateFlow(25.minutes)
    override val defaultSessionDuration: StateFlow<Duration> = _defaultSessionDuration.asStateFlow()

    private val _isRememberLastSession = MutableStateFlow(true)
    override val isRememberLastSession: StateFlow<Boolean> = _isRememberLastSession.asStateFlow()

    private val _timeUnit = MutableStateFlow(TimeUnit.MINUTE)
    override val timeUnit: StateFlow<TimeUnit> = _timeUnit.asStateFlow()

    private val _isScreenOn = MutableStateFlow(true)
    override val isScreenOn: StateFlow<Boolean> = _isScreenOn.asStateFlow()

    private val _isHapticFeedback = MutableStateFlow(true)
    override val isHapticFeedback: StateFlow<Boolean> = _isHapticFeedback.asStateFlow()

    private val _isMinimizedControls = MutableStateFlow(false)
    override val isMinimizedControls: StateFlow<Boolean> = _isMinimizedControls.asStateFlow()

    private val _defaultPresetId = MutableStateFlow<Int?>(null)
    override val defaultPresetId: StateFlow<Int?> = _defaultPresetId.asStateFlow()

    private val _isPulseAnimationEnabled = MutableStateFlow(true)
    override val isPulseAnimationEnabled: StateFlow<Boolean> = _isPulseAnimationEnabled.asStateFlow()

    // 에러 시뮬레이션을 위한 플래그 맵 (Method Name -> Should Fail)
    private val failureFlags = mutableMapOf<String, Boolean>()

    /**
     * 특정 메서드 호출 시 에러를 발생시키도록 설정
     */
    fun setShouldFail(methodName: String, shouldFail: Boolean) {
        failureFlags[methodName] = shouldFail
    }

    private fun checkFailure(methodName: String) {
        if (failureFlags[methodName] == true) {
            throw RuntimeException("Fake Error: $methodName failed")
        }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        checkFailure("updateThemeMode")
        _themeMode.value = themeMode
    }

    override suspend fun updateNotificationSoundType(soundType: NotificationSoundType) {
        checkFailure("updateNotificationSoundType")
        _notificationSoundType.value = soundType
    }

    override suspend fun updateNotificationVibrate(isVibrate: Boolean) {
        checkFailure("updateNotificationVibrate")
        _isNotificationVibrate.value = isVibrate
    }

    override suspend fun updateTickSound(isTickSound: Boolean) {
        checkFailure("updateTickSound")
        _isTickSound.value = isTickSound
    }

    override suspend fun updateDefaultSessionDuration(duration: Duration) {
        checkFailure("updateDefaultSessionDuration")
        _defaultSessionDuration.value = duration
    }

    override suspend fun updateRememberLastSession(isRememberLastSession: Boolean) {
        checkFailure("updateRememberLastSession")
        _isRememberLastSession.value = isRememberLastSession
    }

    override suspend fun updateTimeUnit(timeUnit: TimeUnit) {
        checkFailure("updateTimeUnit")
        _timeUnit.value = timeUnit
    }

    override suspend fun updateScreenOn(isScreenOn: Boolean) {
        checkFailure("updateScreenOn")
        _isScreenOn.value = isScreenOn
    }

    override suspend fun updateHapticFeedback(isHapticFeedback: Boolean) {
        checkFailure("updateHapticFeedback")
        _isHapticFeedback.value = isHapticFeedback
    }

    override suspend fun updateMinimizedControls(isMinimizedControls: Boolean) {
        checkFailure("updateMinimizedControls")
        _isMinimizedControls.value = isMinimizedControls
    }

    override suspend fun updateDefaultPresetId(presetId: Int?) {
        checkFailure("updateDefaultPresetId")
        _defaultPresetId.value = presetId
    }

    override suspend fun updatePulseAnimationEnabled(isEnabled: Boolean) {
        checkFailure("updatePulseAnimationEnabled")
        _isPulseAnimationEnabled.value = isEnabled
    }
}