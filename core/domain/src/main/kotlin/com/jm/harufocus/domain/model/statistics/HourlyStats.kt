package com.jm.harufocus.domain.model.statistics

import kotlin.time.Duration

/**
 * 시간대별 통계 데이터
 *
 * @param hour 시간 (0-23)
 * @param focusTime 해당 시간대의 총 집중 시간
 * @param fullCompletedTime 완전 완료된 세션의 총 시간
 * @param partialTime 부분 완료된 세션의 총 시간
 * @param sessionCount 완료된 세션 수
 * @param fullCompletedCount 완전 완료된 세션 수
 * @param partialCount 부분 완료된 세션 수
 */
data class HourlyStats(
    val hour: Int,
    val focusTime: Duration,
    val fullCompletedTime: Duration = Duration.ZERO,
    val partialTime: Duration = Duration.ZERO,
    val sessionCount: Int,
    val fullCompletedCount: Int = 0,
    val partialCount: Int = 0
)
