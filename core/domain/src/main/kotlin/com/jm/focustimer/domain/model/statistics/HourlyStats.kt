package com.jm.focustimer.domain.model.statistics

import kotlin.time.Duration

/**
 * 시간대별 통계 데이터
 *
 * @param hour 시간 (0-23)
 * @param focusTime 해당 시간대의 총 집중 시간
 * @param sessionCount 완료된 세션 수
 */
data class HourlyStats(
    val hour: Int,
    val focusTime: Duration,
    val sessionCount: Int
)
