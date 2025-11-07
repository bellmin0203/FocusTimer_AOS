package com.jm.focustimer.timer.model

import kotlin.time.Duration

/**
 * 타이머 진행 중 발생하는 이벤트
 */
sealed class TimerEvent {
    /**
     * 매 틱마다 발생 (1초마다)
     * @param remainingTime 남은 시간
     */
    data class Tick(val remainingTime: Duration) : TimerEvent()

    /**
     * 타이머 완료
     */
    data object Completed : TimerEvent()

    /**
     * 리마인더 알림 (n분 또는 n초 남았을 때)
     * @param remainingTime 남은 시간
     */
    data class Reminder(val remainingTime: Duration) : TimerEvent()
}
