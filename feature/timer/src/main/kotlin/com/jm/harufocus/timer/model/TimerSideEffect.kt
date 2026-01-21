package com.jm.harufocus.timer.model

import androidx.annotation.StringRes
import com.jm.harufocus.ui.util.UiText
import kotlin.time.Duration

sealed interface TimerSideEffect {
    /**
     * 에러 메시지 표시 (Snackbar)
     */
    data class ShowError(val errorType: TimerError) : TimerSideEffect
    data class ShowSnackbar(
        @param:StringRes val msgResId: Int,
        val msgArgs: List<Any> = emptyList()
    ) : TimerSideEffect {
        fun toUiText(): UiText = UiText.StringResource(msgResId, msgArgs)
    }

    /**
     * 타이머 완료 알림
     */
    data object ShowTimerCompleted : TimerSideEffect

    /**
     * In-App Review 요청
     */
    data object RequestInAppReview : TimerSideEffect

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