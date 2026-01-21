package com.jm.harufocus.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.jm.logutil.LogUtil
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics 래퍼 유틸리티
 *
 * 앱 전체에서 일관된 방식으로 Analytics 이벤트를 로깅합니다.
 *
 * 사용 예시:
 * ```
 * // 화면 조회 로깅
 * analyticsHelper.logScreenView("timer_screen", "TimerScreen")
 *
 * // 타이머 이벤트 로깅
 * analyticsHelper.logTimerStarted(25, preset?.id, preset?.name, "preset")
 * ```
 */
@Singleton
class AnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    private var isEnabled: Boolean = true

    /**
     * Analytics 수집 활성화/비활성화
     */
    fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        isEnabled = enabled
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
        LogUtil.d("Analytics collection enabled: $enabled")
    }

    // ==================== 화면 조회 이벤트 ====================

    /**
     * 화면 조회 이벤트 로깅
     */
    fun logScreenView(screenName: String, screenClass: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        LogUtil.d("Screen view: $screenName ($screenClass)")
    }

    // ==================== 타이머 이벤트 ====================

    /**
     * 타이머 시작 이벤트
     */
    fun logTimerStarted(
        durationMinutes: Long,
        presetId: Int?,
        presetName: String?,
        inputMethod: String
    ) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIMER_STARTED) {
            param(Param.DURATION_MINUTES, durationMinutes)
            presetId?.let { param(Param.PRESET_ID, it.toLong()) }
            presetName?.let { param(Param.PRESET_NAME, it) }
            param(Param.INPUT_METHOD, inputMethod)
        }
        LogUtil.d("Timer started: duration=$durationMinutes min, preset=$presetName, method=$inputMethod")
    }

    /**
     * 타이머 일시정지 이벤트
     */
    fun logTimerPaused(remainingMinutes: Long, elapsedMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIMER_PAUSED) {
            param(Param.REMAINING_MINUTES, remainingMinutes)
            param(Param.ELAPSED_MINUTES, elapsedMinutes)
        }
        LogUtil.d("Timer paused: remaining=$remainingMinutes min, elapsed=$elapsedMinutes min")
    }

    /**
     * 타이머 재개 이벤트
     */
    fun logTimerResumed(remainingMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIMER_RESUMED) {
            param(Param.REMAINING_MINUTES, remainingMinutes)
        }
        LogUtil.d("Timer resumed: remaining=$remainingMinutes min")
    }

    /**
     * 타이머 강제 중지 이벤트
     */
    fun logTimerStopped(remainingMinutes: Long, elapsedMinutes: Long, completionRate: Int) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIMER_STOPPED) {
            param(Param.REMAINING_MINUTES, remainingMinutes)
            param(Param.ELAPSED_MINUTES, elapsedMinutes)
            param(Param.COMPLETION_RATE, completionRate.toLong())
        }
        LogUtil.d("Timer stopped: remaining=$remainingMinutes min, elapsed=$elapsedMinutes min, rate=$completionRate%")
    }

    /**
     * 타이머 완료 이벤트
     */
    fun logTimerCompleted(
        durationMinutes: Long,
        presetId: Int?,
        overtimeSeconds: Long
    ) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIMER_COMPLETED) {
            param(Param.DURATION_MINUTES, durationMinutes)
            presetId?.let { param(Param.PRESET_ID, it.toLong()) }
            param(Param.OVERTIME_SECONDS, overtimeSeconds)
        }
        LogUtil.d("Timer completed: duration=$durationMinutes min, overtime=$overtimeSeconds sec")
    }

    /**
     * 초과 시간 진입 이벤트
     */
    fun logTimerOvertime(durationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIMER_OVERTIME) {
            param(Param.DURATION_MINUTES, durationMinutes)
        }
        LogUtil.d("Timer overtime: original duration=$durationMinutes min")
    }

    // ==================== 프리셋 이벤트 ====================

    /**
     * 프리셋 생성 이벤트
     */
    fun logPresetCreated(
        presetName: String,
        durationMinutes: Long,
        colorIndex: Int,
        presetCount: Int
    ) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.PRESET_CREATED) {
            param(Param.PRESET_NAME, presetName)
            param(Param.DURATION_MINUTES, durationMinutes)
            param(Param.COLOR_INDEX, colorIndex.toLong())
            param(Param.PRESET_COUNT, presetCount.toLong())
        }
        LogUtil.d("Preset created: name=$presetName, duration=$durationMinutes min")
    }

    /**
     * 프리셋 선택 이벤트
     */
    fun logPresetSelected(presetId: Int, presetName: String, durationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.PRESET_SELECTED) {
            param(Param.PRESET_ID, presetId.toLong())
            param(Param.PRESET_NAME, presetName)
            param(Param.DURATION_MINUTES, durationMinutes)
        }
        LogUtil.d("Preset selected: id=$presetId, name=$presetName")
    }

    /**
     * 프리셋 수정 이벤트
     */
    fun logPresetUpdated(presetId: Int, oldDurationMinutes: Long, newDurationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.PRESET_UPDATED) {
            param(Param.PRESET_ID, presetId.toLong())
            param(Param.OLD_DURATION, oldDurationMinutes)
            param(Param.NEW_DURATION, newDurationMinutes)
        }
        LogUtil.d("Preset updated: id=$presetId, $oldDurationMinutes -> $newDurationMinutes min")
    }

    /**
     * 프리셋 삭제 이벤트
     */
    fun logPresetDeleted(presetId: Int, durationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.PRESET_DELETED) {
            param(Param.PRESET_ID, presetId.toLong())
            param(Param.DURATION_MINUTES, durationMinutes)
        }
        LogUtil.d("Preset deleted: id=$presetId, duration=$durationMinutes min")
    }

    // ==================== 통계 화면 이벤트 ====================

    /**
     * 통계 기간 변경 이벤트
     */
    fun logStatsPeriodChanged(period: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.STATS_PERIOD_CHANGED) {
            param(Param.PERIOD, period)
        }
        LogUtil.d("Stats period changed: $period")
    }

    /**
     * 통계 네비게이션 이벤트
     */
    fun logStatsNavigation(direction: String, period: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.STATS_NAVIGATION) {
            param(Param.DIRECTION, direction)
            param(Param.PERIOD, period)
        }
        LogUtil.d("Stats navigation: $direction ($period)")
    }

    /**
     * 성취 시트 열림 이벤트
     */
    fun logAchievementSheetOpened() {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.ACHIEVEMENT_SHEET_OPENED, null)
        LogUtil.d("Achievement sheet opened")
    }

    // ==================== 설정 이벤트 ====================

    /**
     * 설정 변경 이벤트
     */
    fun logSettingChanged(settingName: String, oldValue: String, newValue: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.SETTING_CHANGED) {
            param(Param.SETTING_NAME, settingName)
            param(Param.OLD_VALUE, oldValue)
            param(Param.NEW_VALUE, newValue)
        }
        LogUtil.d("Setting changed: $settingName = $oldValue -> $newValue")
    }

    /**
     * 테마 변경 이벤트
     */
    fun logThemeChanged(theme: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.THEME_CHANGED) {
            param(Param.THEME, theme)
        }
        LogUtil.d("Theme changed: $theme")
    }

    /**
     * 기본 세션 시간 변경 이벤트
     */
    fun logDefaultDurationChanged(durationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.DEFAULT_DURATION_CHANGED) {
            param(Param.DURATION_MINUTES, durationMinutes)
        }
        LogUtil.d("Default duration changed: $durationMinutes min")
    }

    /**
     * 기본 프리셋 변경 이벤트
     */
    fun logDefaultPresetChanged(presetId: Int?, presetName: String?) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.DEFAULT_PRESET_CHANGED) {
            presetId?.let { param(Param.PRESET_ID, it.toLong()) }
            presetName?.let { param(Param.PRESET_NAME, it) }
        }
        LogUtil.d("Default preset changed: id=$presetId, name=$presetName")
    }

    /**
     * 개인정보처리방침 클릭 이벤트
     */
    fun logPrivacyPolicyClicked() {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.PRIVACY_POLICY_CLICKED, null)
        LogUtil.d("Privacy policy clicked")
    }

    /**
     * 오픈소스 라이선스 클릭 이벤트
     */
    fun logOpenSourceLicensesClicked() {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.OPEN_SOURCE_LICENSES_CLICKED, null)
        LogUtil.d("Open source licenses clicked")
    }

    /**
     * 앱 평가 클릭 이벤트
     */
    fun logRateAppClicked() {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.RATE_APP_CLICKED, null)
        LogUtil.d("Rate app clicked")
    }

    // ==================== 위젯 이벤트 ====================

    /**
     * 위젯 추가 이벤트
     */
    fun logWidgetAdded(widgetSize: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.WIDGET_ADDED) {
            param(Param.WIDGET_SIZE, widgetSize)
        }
        LogUtil.d("Widget added: size=$widgetSize")
    }

    /**
     * 위젯에서 타이머 시작 이벤트
     */
    fun logWidgetTimerStarted(durationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.WIDGET_TIMER_STARTED) {
            param(Param.DURATION_MINUTES, durationMinutes)
        }
        LogUtil.d("Widget timer started: duration=$durationMinutes min")
    }

    /**
     * 위젯에서 타이머 일시정지 이벤트
     */
    fun logWidgetTimerPaused(remainingMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.WIDGET_TIMER_PAUSED) {
            param(Param.REMAINING_MINUTES, remainingMinutes)
        }
        LogUtil.d("Widget timer paused: remaining=$remainingMinutes min")
    }

    /**
     * 위젯에서 앱 진입 이벤트
     */
    fun logWidgetAppLaunched(timerStatus: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.WIDGET_APP_LAUNCHED) {
            param(Param.TIMER_STATUS, timerStatus)
        }
        LogUtil.d("Widget app launched: status=$timerStatus")
    }

    // ==================== UI 인터랙션 이벤트 ====================

    /**
     * 드로어 열림 이벤트
     */
    fun logDrawerOpened() {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.DRAWER_OPENED, null)
        LogUtil.d("Drawer opened")
    }

    /**
     * 시간 입력 방식 사용 이벤트
     */
    fun logTimeInputMethodUsed(method: String) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.TIME_INPUT_METHOD_USED) {
            param(Param.METHOD, method)
        }
        LogUtil.d("Time input method used: $method")
    }

    // ==================== 전환 이벤트 ====================

    /**
     * 첫 번째 세션 완료 이벤트 (신규 사용자)
     */
    fun logFirstSessionCompleted(durationMinutes: Long) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.FIRST_SESSION_COMPLETED) {
            param(Param.DURATION_MINUTES, durationMinutes)
        }
        LogUtil.d("First session completed: duration=$durationMinutes min")
    }

    /**
     * 인앱 리뷰 다이얼로그 표시 이벤트
     */
    fun logInAppReviewShown() {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.IN_APP_REVIEW_SHOWN, null)
        LogUtil.d("In-app review shown")
    }

    /**
     * 연속 N일 집중 달성 이벤트
     */
    fun logConsecutiveDaysAchieved(days: Int) {
        if (!isEnabled) return

        firebaseAnalytics.logEvent(Event.CONSECUTIVE_DAYS_ACHIEVED) {
            param(Param.CONSECUTIVE_DAYS, days.toLong())
        }
        LogUtil.d("Consecutive days achieved: $days")
    }

    // ==================== 사용자 속성 ====================

    /**
     * 사용자 속성 설정
     */
    fun setUserProperty(name: String, value: String) {
        if (!isEnabled) return

        firebaseAnalytics.setUserProperty(name, value)
        LogUtil.d("User property set: $name = $value")
    }

    /**
     * 선호 테마 설정
     */
    fun setPreferredTheme(theme: String) {
        setUserProperty(UserProperty.PREFERRED_THEME, theme)
    }

    /**
     * 누적 완료 세션 수 설정
     */
    fun setTotalSessionsCompleted(count: Int) {
        val value = when {
            count == 0 -> "0"
            count < 10 -> "1-9"
            count < 50 -> "10-49"
            count < 100 -> "50-99"
            else -> "100+"
        }
        setUserProperty(UserProperty.TOTAL_SESSIONS_COMPLETED, value)
    }

    /**
     * 프리셋 개수 설정
     */
    fun setPresetCount(count: Int) {
        val value = when {
            count == 0 -> "0"
            count <= 2 -> "1-2"
            count <= 4 -> "3-4"
            else -> "5"
        }
        setUserProperty(UserProperty.PRESET_COUNT, value)
    }

    /**
     * 위젯 사용자 여부 설정
     */
    fun setWidgetUser(isWidgetUser: Boolean) {
        setUserProperty(UserProperty.WIDGET_USER, isWidgetUser.toString())
    }

    /**
     * 연속 집중 일수 설정
     */
    fun setConsecutiveFocusDays(days: Int) {
        val value = when {
            days == 0 -> "0"
            days <= 3 -> "1-3"
            days <= 7 -> "4-7"
            else -> "8+"
        }
        setUserProperty(UserProperty.CONSECUTIVE_FOCUS_DAYS, value)
    }

    // ==================== 상수 정의 ====================

    /**
     * 커스텀 이벤트 이름
     */
    object Event {
        // 타이머 이벤트
        const val TIMER_STARTED = "timer_started"
        const val TIMER_PAUSED = "timer_paused"
        const val TIMER_RESUMED = "timer_resumed"
        const val TIMER_STOPPED = "timer_stopped"
        const val TIMER_COMPLETED = "timer_completed"
        const val TIMER_OVERTIME = "timer_overtime"

        // 프리셋 이벤트
        const val PRESET_CREATED = "preset_created"
        const val PRESET_SELECTED = "preset_selected"
        const val PRESET_UPDATED = "preset_updated"
        const val PRESET_DELETED = "preset_deleted"

        // 통계 이벤트
        const val STATS_PERIOD_CHANGED = "stats_period_changed"
        const val STATS_NAVIGATION = "stats_navigation"
        const val ACHIEVEMENT_SHEET_OPENED = "achievement_sheet_opened"

        // 설정 이벤트
        const val SETTING_CHANGED = "setting_changed"
        const val THEME_CHANGED = "theme_changed"
        const val DEFAULT_DURATION_CHANGED = "default_duration_changed"
        const val DEFAULT_PRESET_CHANGED = "default_preset_changed"
        const val PRIVACY_POLICY_CLICKED = "privacy_policy_clicked"
        const val OPEN_SOURCE_LICENSES_CLICKED = "open_source_licenses_clicked"
        const val RATE_APP_CLICKED = "rate_app_clicked"

        // 위젯 이벤트
        const val WIDGET_ADDED = "widget_added"
        const val WIDGET_TIMER_STARTED = "widget_timer_started"
        const val WIDGET_TIMER_PAUSED = "widget_timer_paused"
        const val WIDGET_APP_LAUNCHED = "widget_app_launched"

        // UI 인터랙션 이벤트
        const val DRAWER_OPENED = "drawer_opened"
        const val TIME_INPUT_METHOD_USED = "time_input_method_used"

        // 전환 이벤트
        const val FIRST_SESSION_COMPLETED = "first_session_completed"
        const val IN_APP_REVIEW_SHOWN = "in_app_review_shown"
        const val CONSECUTIVE_DAYS_ACHIEVED = "consecutive_days_achieved"
    }

    /**
     * 이벤트 매개변수 이름
     */
    object Param {
        const val DURATION_MINUTES = "duration_minutes"
        const val REMAINING_MINUTES = "remaining_minutes"
        const val ELAPSED_MINUTES = "elapsed_minutes"
        const val OVERTIME_SECONDS = "overtime_seconds"
        const val COMPLETION_RATE = "completion_rate"

        const val PRESET_ID = "preset_id"
        const val PRESET_NAME = "preset_name"
        const val PRESET_COUNT = "preset_count"
        const val COLOR_INDEX = "color_index"
        const val OLD_DURATION = "old_duration"
        const val NEW_DURATION = "new_duration"

        const val INPUT_METHOD = "input_method"
        const val METHOD = "method"
        const val PERIOD = "period"
        const val DIRECTION = "direction"
        const val TIMER_STATUS = "timer_status"
        const val WIDGET_SIZE = "widget_size"

        const val SETTING_NAME = "setting_name"
        const val OLD_VALUE = "old_value"
        const val NEW_VALUE = "new_value"
        const val THEME = "theme"

        const val CONSECUTIVE_DAYS = "consecutive_days"
    }

    /**
     * 사용자 속성 이름
     */
    object UserProperty {
        const val PREFERRED_THEME = "preferred_theme"
        const val TOTAL_SESSIONS_COMPLETED = "total_sessions_completed"
        const val PRESET_COUNT = "preset_count"
        const val WIDGET_USER = "widget_user"
        const val CONSECUTIVE_FOCUS_DAYS = "consecutive_focus_days"
    }

    /**
     * 시간 입력 방식 상수
     */
    object InputMethod {
        const val DRAG = "drag"
        const val DIRECT_INPUT = "direct_input"
        const val PRESET = "preset"
        const val PICKER = "picker"
    }

    /**
     * 통계 기간 상수
     */
    object StatsPeriod {
        const val DAILY = "daily"
        const val WEEKLY = "weekly"
        const val MONTHLY = "monthly"
    }

    /**
     * 네비게이션 방향 상수
     */
    object Direction {
        const val PREVIOUS = "previous"
        const val NEXT = "next"
        const val TODAY = "today"
    }
}
