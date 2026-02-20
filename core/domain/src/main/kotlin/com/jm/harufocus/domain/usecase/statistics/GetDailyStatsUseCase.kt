package com.jm.harufocus.domain.usecase.statistics

import com.jm.harufocus.domain.model.statistics.DailyStats
import com.jm.harufocus.domain.model.statistics.HourlyStats
import com.jm.harufocus.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * 일일 통계를 조회하는 UseCase
 */
class GetDailyStatsUseCase @Inject constructor(
    private val timerSessionRepository: TimerSessionRepository
) {
    /**
     * 특정 날짜의 일일 통계를 조회합니다
     *
     * @param date 조회할 날짜 (기본값: 오늘)
     * @return 일일 통계
     */
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): DailyStats {
        // 해당 날짜의 시작과 끝 타임스탬프 계산
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        // 해당 날짜의 완료된 세션들 조회
        val sessions = timerSessionRepository.getCompletedSessionsBetween(startOfDay, endOfDay).first()

        // 시간대별 집계 (0-23시) - 완전/부분 완료 시간 분리
        val hourlyStatsMap = mutableMapOf<Int, HourlyStats>()
        for (hour in 0..23) {
            hourlyStatsMap[hour] = HourlyStats(
                hour = hour,
                focusTime = 0.milliseconds,
                fullCompletedTime = 0.milliseconds,
                partialTime = 0.milliseconds,
                sessionCount = 0,
                fullCompletedCount = 0,
                partialCount = 0
            )
        }

        // 세션들을 시간대별로 집계
        sessions.forEach { session ->
            val sessionStartHour = LocalDateTime
                .ofInstant(session.startTime, zoneId)
                .hour

            // null 안전 처리: 키가 없을 경우 기본값으로 초기화하여 업데이트
            val currentStats = hourlyStatsMap[sessionStartHour]
                ?: HourlyStats(
                    hour = sessionStartHour,
                    focusTime = 0.milliseconds,
                    fullCompletedTime = 0.milliseconds,
                    partialTime = 0.milliseconds,
                    sessionCount = 0,
                    fullCompletedCount = 0,
                    partialCount = 0
                )

            hourlyStatsMap[sessionStartHour] = currentStats.copy(
                focusTime = currentStats.focusTime + session.duration,
                fullCompletedTime = currentStats.fullCompletedTime + if (session.isPartial) 0.milliseconds else session.duration,
                partialTime = currentStats.partialTime + if (session.isPartial) session.duration else 0.milliseconds,
                sessionCount = currentStats.sessionCount + 1,
                fullCompletedCount = currentStats.fullCompletedCount + if (session.isPartial) 0 else 1,
                partialCount = currentStats.partialCount + if (session.isPartial) 1 else 0
            )
        }

        val hourlyBreakdown = hourlyStatsMap.values.sortedBy { it.hour }

        // 가장 생산적인 시간 계산 (집중 시간이 가장 긴 시간대)
        val mostProductiveHour = hourlyBreakdown
            .filter { it.focusTime > 0.seconds }
            .maxByOrNull { it.focusTime }
            ?.hour

        // 총 집중 시간과 세션 수 계산
        val totalFocusTime = sessions.sumOf { it.duration.inWholeMilliseconds }.milliseconds
        val completedSessions = sessions.size
        val fullCompletedSessions = sessions.count { !it.isPartial }
        val partialSessions = sessions.count { it.isPartial }

        return DailyStats(
            date = date,
            totalFocusTime = totalFocusTime,
            completedSessions = completedSessions,
            fullCompletedSessions = fullCompletedSessions,
            partialSessions = partialSessions,
            hourlyBreakdown = hourlyBreakdown,
            mostProductiveHour = mostProductiveHour
        )
    }
}
