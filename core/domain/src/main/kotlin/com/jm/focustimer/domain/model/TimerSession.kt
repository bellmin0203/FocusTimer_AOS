package com.jm.focustimer.domain.model

import java.time.Instant
import kotlin.time.Duration

/**
 * 타이머 세션 정보를 담는 데이터 클래스
 *
 * @param id 세션의 고유 식별자 (Primary key)
 * @param presetId 사용된 프리셋의 ID
 * @param startTime 세션 시작 시간
 * @param endTime 세션 종료 시간 (nullable - 진행 중인 세션의 경우 null)
 * @param duration 세션 지속 시간
 * @param completed 세션 완료 여부
 * @param overrunTime 초과 시간 (nullable - 초과하지 않은 경우 null)
 */
data class TimerSession(
    val id: Int,
    val presetId: Int,
    val startTime: Instant,
    val endTime: Instant?,
    val duration: Duration,
    val completed: Boolean,
    val overrunTime: Duration?
)