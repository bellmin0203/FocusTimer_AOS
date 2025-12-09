package com.jm.focustimer.domain.repository

import com.jm.focustimer.common.model.NotificationSoundType
import com.jm.focustimer.common.model.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 앱 설정 관리를 위한 Repository 인터페이스
 */
interface SettingsRepository {

    /**
     * 다크 테마 활성화 여부 Flow
     */
    val isDarkTheme: Flow<Boolean>

    /**
     * 알림 소리 타입 Flow
     */
    val notificationSoundType: Flow<NotificationSoundType>

    /**
     * 알림 진동 활성화 여부 Flow
     */
    val isNotificationVibrate: Flow<Boolean>

    /**
     * 틱 소리 활성화 여부 Flow
     */
    val isTickSound: Flow<Boolean>

    /**
     * 기본 세션 지속 시간 Flow
     */
    val defaultSessionDuration: Flow<Duration>

    /**
     * 마지막 세션 기억 여부 Flow
     */
    val isRememberLastSession: Flow<Boolean>

    /**
     * 시간 단위 Flow
     */
    val timeUnit: Flow<TimeUnit>

    /**
     * 화면 상시 켜짐 여부 Flow
     */
    val isScreenOn: Flow<Boolean>

    /**
     * 햅틱 피드백 활성화 여부 Flow
     */
    val isHapticFeedback: Flow<Boolean>

    /**
     * 최소화된 컨트롤 표시 여부 Flow
     */
    val isMinimizedControls: Flow<Boolean>

    /**
     * 기본 프리셋 ID Flow
     * 설정되지 않은 경우 null 반환
     */
    val defaultPresetId: Flow<Int?>

    /**
     * 다크 테마 설정을 업데이트합니다.
     * @param isDarkTheme 다크 테마 활성화 여부
     */
    suspend fun updateDarkTheme(isDarkTheme: Boolean)

    /**
     * 알림 소리 타입을 업데이트합니다.
     * @param soundType 알림 소리 타입
     */
    suspend fun updateNotificationSoundType(soundType: NotificationSoundType)

    /**
     * 알림 진동 설정을 업데이트합니다.
     * @param isVibrate 진동 활성화 여부
     */
    suspend fun updateNotificationVibrate(isVibrate: Boolean)

    /**
     * 틱 소리 설정을 업데이트합니다.
     * @param isTickSound 틱 소리 활성화 여부
     */
    suspend fun updateTickSound(isTickSound: Boolean)

    /**
     * 기본 세션 지속 시간을 업데이트합니다.
     * @param duration 세션 지속 시간
     */
    suspend fun updateDefaultSessionDuration(duration: Duration)

    /**
     * 마지막 세션 기억 설정을 업데이트합니다.
     * @param isRememberLastSession 마지막 세션 기억 여부
     */
    suspend fun updateRememberLastSession(isRememberLastSession: Boolean)

    /**
     * 시간 단위를 업데이트합니다.
     * @param timeUnit 시간 단위
     */
    suspend fun updateTimeUnit(timeUnit: TimeUnit)

    /**
     * 화면 상시 켜짐 설정을 업데이트합니다.
     * @param isScreenOn 화면 상시 켜짐 여부
     */
    suspend fun updateScreenOn(isScreenOn: Boolean)

    /**
     * 햅틱 피드백 설정을 업데이트합니다.
     * @param isHapticFeedback 햅틱 피드백 활성화 여부
     */
    suspend fun updateHapticFeedback(isHapticFeedback: Boolean)

    /**
     * 최소화된 컨트롤 표시 설정을 업데이트합니다.
     * @param isMinimizedControls 최소화된 컨트롤 표시 여부
     */
    suspend fun updateMinimizedControls(isMinimizedControls: Boolean)

    /**
     * 기본 프리셋 ID를 업데이트합니다.
     * @param presetId 설정할 프리셋 ID (null인 경우 기본값 제거)
     */
    suspend fun updateDefaultPresetId(presetId: Int?)

    companion object {
        const val DEFAULT_IS_DARK_THEME = false
        const val DEFAULT_IS_NOTIFICATION_VIBRATE = false
        const val DEFAULT_IS_TICK_SOUND = false
        val DEFAULT_SESSION_DURATION = 15.minutes
        const val DEFAULT_IS_REMEMBER_LAST_SESSION = true
        val DEFAULT_TIME_UNIT = TimeUnit.MINUTE
        const val DEFAULT_IS_SCREEN_ON = false
        const val DEFAULT_IS_HAPTIC_FEEDBACK = false
        const val DEFAULT_IS_MINIMIZED_CONTROLS = false
    }
}
