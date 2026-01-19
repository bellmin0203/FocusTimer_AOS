package com.jm.harufocus.util

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Firebase Crashlytics 래퍼 유틸리티
 *
 * 앱 전체에서 일관된 방식으로 크래시 리포팅을 수행합니다.
 * 초기화 시 isEnabled를 false로 설정하면 로깅이 비활성화됩니다.
 *
 * 사용 예시:
 * ```
 * // Application에서 초기화
 * CrashReporter.init(isEnabled = !BuildConfig.DEBUG)
 *
 * // 비치명적 예외 기록
 * CrashReporter.recordException(exception)
 *
 * // 컨텍스트 정보와 함께 기록
 * CrashReporter.recordException(exception, "타이머 시작 실패")
 *
 * // 사용자 행동 로그 (Breadcrumb)
 * CrashReporter.log("타이머 시작: duration=25분")
 *
 * // 커스텀 키-값 설정
 * CrashReporter.setCustomKey("timer_status", "running")
 * ```
 */
object CrashReporter {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    private var isEnabled: Boolean = false

    /**
     * CrashReporter를 초기화합니다.
     * Application.onCreate()에서 호출해야 합니다.
     *
     * @param isEnabled 크래시 리포팅 활성화 여부 (일반적으로 !BuildConfig.DEBUG)
     */
    fun init(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        crashlytics.isCrashlyticsCollectionEnabled = isEnabled
    }

    /**
     * 비치명적 예외를 Crashlytics에 기록합니다.
     *
     * @param throwable 기록할 예외
     * @param message 추가 컨텍스트 메시지 (선택)
     */
    fun recordException(throwable: Throwable, message: String? = null) {
        if (!isEnabled) return

        message?.let { crashlytics.log(it) }
        crashlytics.recordException(throwable)
    }

    /**
     * 로그 메시지를 Crashlytics에 기록합니다.
     * 크래시 발생 시 이 로그들이 Breadcrumb으로 표시됩니다.
     *
     * @param message 기록할 메시지
     */
    fun log(message: String) {
        if (!isEnabled) return

        crashlytics.log(message)
    }

    /**
     * 커스텀 키-값을 설정합니다.
     * 크래시 분석 시 디버깅에 유용한 정보를 제공합니다.
     *
     * @param key 키 이름
     * @param value 값 (String)
     */
    fun setCustomKey(key: String, value: String) {
        if (!isEnabled) return

        crashlytics.setCustomKey(key, value)
    }

    /**
     * 커스텀 키-값을 설정합니다.
     *
     * @param key 키 이름
     * @param value 값 (Int)
     */
    fun setCustomKey(key: String, value: Int) {
        if (!isEnabled) return

        crashlytics.setCustomKey(key, value)
    }

    /**
     * 커스텀 키-값을 설정합니다.
     *
     * @param key 키 이름
     * @param value 값 (Long)
     */
    fun setCustomKey(key: String, value: Long) {
        if (!isEnabled) return

        crashlytics.setCustomKey(key, value)
    }

    /**
     * 커스텀 키-값을 설정합니다.
     *
     * @param key 키 이름
     * @param value 값 (Boolean)
     */
    fun setCustomKey(key: String, value: Boolean) {
        if (!isEnabled) return

        crashlytics.setCustomKey(key, value)
    }

    /**
     * 현재 화면 정보를 설정합니다.
     *
     * @param screenName 화면 이름
     */
    fun setCurrentScreen(screenName: String) {
        setCustomKey(KEY_CURRENT_SCREEN, screenName)
    }

    /**
     * 타이머 상태를 설정합니다.
     *
     * @param status 타이머 상태 문자열
     */
    fun setTimerStatus(status: String) {
        setCustomKey(KEY_TIMER_STATUS, status)
    }

    /**
     * 선택된 프리셋 정보를 설정합니다.
     *
     * @param presetId 프리셋 ID
     * @param presetName 프리셋 이름
     */
    fun setSelectedPreset(presetId: Long, presetName: String) {
        setCustomKey(KEY_PRESET_ID, presetId)
        setCustomKey(KEY_PRESET_NAME, presetName)
    }

    /**
     * 사용자 식별자를 설정합니다.
     * 개인정보가 아닌 익명 ID를 사용해야 합니다.
     *
     * @param userId 사용자 식별자
     */
    fun setUserId(userId: String) {
        if (!isEnabled) return

        crashlytics.setUserId(userId)
    }

    // 커스텀 키 상수
    private const val KEY_CURRENT_SCREEN = "current_screen"
    private const val KEY_TIMER_STATUS = "timer_status"
    private const val KEY_PRESET_ID = "preset_id"
    private const val KEY_PRESET_NAME = "preset_name"
}
