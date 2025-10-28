package com.jm.focustimer.timer.model

import androidx.compose.runtime.Immutable

/**
 * 타이머 화면의 UI 상태를 나타내는 불변 데이터 클래스
 *
 * @property timeInSeconds 현재 타이머 시간 (초 단위)
 * @property isRunning 타이머가 실행 중인지 여부
 * @property isPaused 타이머가 일시정지 상태인지 여부
 * @property isCompleted 타이머가 완료되었는지 여부
 * @property overtimeInSeconds 타이머 완료 후 경과한 초과 시간 (초 단위)
 * @property progress 타이머 진행률 (0.0 ~ 1.0)
 * @property error 에러 메시지 (에러가 없으면 null)
 */
@Immutable
data class TimerUiState(
    val timeInSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val overtimeInSeconds: Int = 0,
    val progress: Float = 0f,
    val error: String? = null
) {
    /**
     * 타이머가 유휴 상태인지 (시작되지 않은 상태)
     */
    val isIdle: Boolean
        get() = !isRunning && !isPaused && !isCompleted

    /**
     * 타이머가 활성 상태인지 (실행 중이거나 일시정지 상태)
     */
    val isActive: Boolean
        get() = isRunning || isPaused

    /**
     * 표시할 총 시간 (타이머 시간 + 초과 시간)
     */
    val totalTimeInSeconds: Int
        get() = timeInSeconds + overtimeInSeconds

    /**
     * 분 단위 시간
     */
    val minutes: Int
        get() = timeInSeconds / 60

    /**
     * 초 단위 시간 (분을 제외한 나머지)
     */
    val seconds: Int
        get() = timeInSeconds % 60

    /**
     * 시간 형식으로 포맷팅 (MM:SS)
     */
    val formattedTime: String
        get() = String.format("%02d:%02d", minutes, seconds)

    /**
     * 에러가 있는지 여부
     */
    val hasError: Boolean
        get() = error != null
}
