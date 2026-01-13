package com.jm.harufocus.timer.model

import com.jm.harufocus.domain.model.preset.Preset
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
    data class Start(val reminderThresholds: List<Duration> = emptyList()) : TimerIntent

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
     * 타이머 완료 확인 (초과 시간 추적 종료 및 초기화)
     */
    object Complete : TimerIntent

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
     * @param duration 프리셋 시간
     * @param colorIndex 타이머 컬러 인덱스
     */
    data class SaveAsPreset(
        val name: String,
        val duration: Duration,
        val colorIndex: Int
    ) : TimerIntent

    /**
     * 프리셋 삭제
     * @param presetId 삭제할 프리셋 ID
     */
    data class DeletePreset(val presetId: Int) : TimerIntent

    /**
     * 프리셋 수정
     * @param preset 프리셋
     */
    data class UpdatePreset(
        val preset: Preset
    ) : TimerIntent
}
