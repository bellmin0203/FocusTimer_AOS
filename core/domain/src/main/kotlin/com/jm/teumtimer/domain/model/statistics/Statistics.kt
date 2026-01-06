package com.jm.teumtimer.domain.model.statistics

import java.time.LocalDate
import kotlin.time.Duration

/**
 * 일별 통계 정보를 담는 데이터 클래스
 *
 * @param id 통계의 고유 식별자 (Primary key)
 * @param date 통계 날짜
 * @param totalFocusTime 총 집중 시간
 * @param completedSessions 완료된 세션 수
 * @param focusRate 집중 달성률 (0.0 ~ 1.0)
 * @param longestFocusTime 가장 긴 집중 시간
 * @param averageSessionLength 평균 세션 길이
 */
data class Statistics(
    val id: Int,
    val date: LocalDate,
    val totalFocusTime: Duration,
    val completedSessions: Int,
    val focusRate: Double,
    val longestFocusTime: Duration,
    val averageSessionLength: Duration
)
