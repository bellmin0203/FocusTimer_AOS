package com.jm.focustimer.timer.model

import kotlin.time.Duration

/**
 * 타이머 화면에서 발생하는 사용자 액션을 나타내는 Intent
 *
 * MVI 패턴에서 View -> ViewModel로 전달되는 사용자 의도를 표현합니다.
 */
sealed interface TimerIntent {

    /**
     * 타이머 시간 설정
     * @param totalTime 설정할 시간
     */
    data class SetTime(val totalTime: Duration) : TimerIntent

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

    /**
     * 프리셋 선택 - 프리셋의 시간으로 타이머 설정
     * @param presetId 선택한 프리셋 ID
     */
    data class SelectPreset(val presetId: Int) : TimerIntent

    /**
     * 현재 시간을 프리셋으로 저장
     * @param name 프리셋 이름
     */
    data class SaveAsPreset(val name: String) : TimerIntent

    /**
     * 프리셋 삭제
     * @param presetId 삭제할 프리셋 ID
     */
    data class DeletePreset(val presetId: Int) : TimerIntent

    /**
     * 프리셋 수정
     * @param presetId 수정할 프리셋 ID
     * @param name 새 이름
     * @param duration 새 시간
     */
    data class UpdatePreset(val presetId: Int, val name: String, val duration: Duration) :
        TimerIntent
}
