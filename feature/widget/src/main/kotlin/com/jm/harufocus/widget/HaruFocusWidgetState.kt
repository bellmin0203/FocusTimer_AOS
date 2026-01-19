package com.jm.harufocus.widget

import kotlin.time.Duration

/**
 * 하루 몰입 Widget 상태
 *
 * 위젯에 표시될 타이머 상태를 정의합니다.
 */
sealed interface HaruFocusWidgetState {

    companion object {
        const val RUNNING = "running"
        const val PAUSED = "paused"
        const val COMPLETED = "completed"
        const val OVERTIME = "overtime"
        const val IDLE = "idle"
        const val DEFAULT_REMAINING_TIME = 0L
    }

    /**
     * 대기 상태
     */
    data object Idle : HaruFocusWidgetState

    /**
     * 실행 중 상태
     *
     * @param remainingTime 남은 시간 (포맷된 문자열, 예: "15:30")
     */
    data class Running(
        val remainingTime: Duration
    ) : HaruFocusWidgetState

    /**
     * 일시정지 상태
     *
     * @param remainingTime 남은 시간 (포맷된 문자열, 예: "15:30")
     */
    data class Paused(
        val remainingTime: Duration
    ) : HaruFocusWidgetState

    /**
     * 완료 상태 (남은 시간이 0이 된 순간)
     */
    data object Completed : HaruFocusWidgetState

    /**
     * 초과 시간 상태 (완료 후 계속 시간이 흐르는 상태)
     *
     * @param overtime 초과 시간 (포맷된 문자열, 예: "+02:30")
     */
    data class Overtime(
        val overtime: Duration
    ) : HaruFocusWidgetState
}
