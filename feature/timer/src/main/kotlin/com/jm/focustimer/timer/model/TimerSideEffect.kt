package com.jm.focustimer.timer.model

import kotlin.time.Duration

sealed interface TimerSideEffect {
    /**
     * 에러 메시지 표시 (Snackbar)
     */
    data class ShowError(val message: String) : TimerSideEffect
    data class ShowSnackbar(val message: String) : TimerSideEffect

    /**
     * 타이머 완료 알림
     */
    data object ShowTimerCompleted : TimerSideEffect

    /**
     * 햅틱 피드백 (진동)
     */
    data class HapticFeedback(val pattern: HapticPattern) : TimerSideEffect

    /**
     * 리마인더 알림
     * @param remainingTime 남은 시간 (초)
     */
    data class ShowReminder(val remainingTime: Duration) : TimerSideEffect
}

enum class HapticPattern {
    /** 짧은 틱 (1초마다) */
    TICK,

    /** 강한 진동 (타이머 완료) */
    COMPLETED,

    /** 중간 진동 (리마인더) */
    REMINDER
}