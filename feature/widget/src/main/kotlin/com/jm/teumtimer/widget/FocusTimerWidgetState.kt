package com.jm.teumtimer.widget

import kotlin.time.Duration

/**
 * Focus Timer Widget 상태
 *
 * 위젯에 표시될 타이머 상태를 정의합니다.
 */
sealed interface FocusTimerWidgetState {

    companion object {
        const val RUNNING = "running"
        const val PAUSED = "paused"
        const val COMPLETED = "completed"
        const val IDLE = "idle"
        const val DEFAULT_REMAINING_TIME = 0L
    }

    /**
     * 대기 상태
     */
    data object Idle : FocusTimerWidgetState

    /**
     * 실행 중 상태
     *
     * @param remainingTime 남은 시간 (포맷된 문자열, 예: "15:30")
     */
    data class Running(
        val remainingTime: Duration
    ) : FocusTimerWidgetState

    /**
     * 일시정지 상태
     *
     * @param remainingTime 남은 시간 (포맷된 문자열, 예: "15:30")
     */
    data class Paused(
        val remainingTime: Duration
    ) : FocusTimerWidgetState

    /**
     * 완료 상태
     *
     * @param overtime 초과 시간 (포맷된 문자열, 예: "+02:30")
     */
    data class Completed(
        val overtime: Duration
    ) : FocusTimerWidgetState
}
