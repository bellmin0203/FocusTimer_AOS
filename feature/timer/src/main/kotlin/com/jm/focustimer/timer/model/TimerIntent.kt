package com.jm.focustimer.timer.model

/**
 * 타이머 화면에서 발생하는 사용자 액션을 나타내는 Intent
 *
 * MVI 패턴에서 View -> ViewModel로 전달되는 사용자 의도를 표현합니다.
 */
sealed interface TimerIntent {

    /**
     * 타이머 시간 설정
     * @param timeInMillis 설정할 시간 (밀리초)
     */
    data class SetTime(val timeInMillis: Long) : TimerIntent

    /**
     * 타이머 시작
     */
    object Start : TimerIntent

    /**
     * 타이머 일시정지
     */
    object Pause : TimerIntent

    /**
     * 타이머 재개
     */
    object Resume : TimerIntent

    /**
     * 타이머 정지 및 초기화
     */
    object Stop : TimerIntent

    /**
     * 드래그를 통한 시간 조정
     * @param progress 드래그된 비율
     */
    data class DragProgress(val progress: Float) : TimerIntent
}
