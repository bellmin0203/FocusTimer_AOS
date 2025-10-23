package com.jm.focustimer.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * 앱 설정 관리를 위한 Repository 인터페이스
 */
interface SettingsRepository {

    /**
     * 다크 테마 활성화 여부 Flow
     */
    val isDarkTheme: Flow<Boolean>

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
     * 기본 프리셋 ID를 업데이트합니다.
     * @param presetId 설정할 프리셋 ID (null인 경우 기본값 제거)
     */
    suspend fun updateDefaultPresetId(presetId: Int?)
}