package com.jm.focustimer.domain.usecase.statistics

import com.jm.focustimer.domain.model.statistics.AchievementMetrics
import com.jm.focustimer.domain.repository.TimerSessionRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * 성취 지표를 조회하는 UseCase
 */
class GetAchievementMetricsUseCase @Inject constructor(
    private val timerSessionRepository: TimerSessionRepository
) {
    /**
     * 전체 성취 지표를 계산합니다
     *
     * @return 성취 지표
     */
    suspend operator fun invoke(): AchievementMetrics {
        // 전체 세션 수와 완료된 세션 수 조회
        val totalSessions = timerSessionRepository.getTotalSessionCount()
        val completedSessions = timerSessionRepository.getCompletedSessionCount()

        // 집중률 계산 (완료된 세션 / 전체 세션)
        val focusRate = if (totalSessions > 0) {
            completedSessions.toDouble() / totalSessions.toDouble()
        } else {
            0.0
        }

        // 연속 집중 일수 계산
        val consecutiveFocusDays = calculateConsecutiveFocusDays()

        // 최장 집중 시간 조회 (하루 중 가장 긴 집중 시간)
        val longestFocusTimeMillis = getLongestDailyFocusTime()
        val longestFocusTime = longestFocusTimeMillis.milliseconds

        // 평균 세션 길이 조회
        val averageSessionLengthMillis = getAverageSessionLength()
        val averageSessionLength = averageSessionLengthMillis.milliseconds

        // 전체 집중 시간 조회
        val totalFocusTimeMillis = getTotalFocusTime()
        val totalFocusTime = totalFocusTimeMillis.milliseconds

        return AchievementMetrics(
            focusRate = focusRate,
            consecutiveFocusDays = consecutiveFocusDays,
            longestFocusTime = longestFocusTime,
            averageSessionLength = averageSessionLength,
            totalSessions = totalSessions,
            completedSessions = completedSessions,
            totalFocusTime = totalFocusTime
        )
    }

    /**
     * 연속 집중 일수를 계산합니다
     * 오늘부터 역순으로 확인하여 완료된 세션이 있는 날을 카운트
     */
    private suspend fun calculateConsecutiveFocusDays(): Int {
        // 완료된 세션이 있는 날짜들을 조회 (최신순)
        val completedDates = getCompletedSessionDates()

        if (completedDates.isEmpty()) {
            return 0
        }

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDates = completedDates.map { LocalDate.parse(it, dateFormatter) }

        var consecutiveDays = 0
        var currentDate = LocalDate.now()

        // 오늘부터 역순으로 확인
        for (date in parsedDates) {
            // 날짜가 연속되는지 확인
            if (date == currentDate || date == currentDate.minusDays(1)) {
                consecutiveDays++
                currentDate = date.minusDays(1)
            } else {
                // 연속되지 않으면 중단
                break
            }
        }

        return consecutiveDays
    }

    /**
     * 완료된 세션이 있는 날짜 목록을 조회합니다
     */
    private suspend fun getCompletedSessionDates(): List<String> {
        return try {
            timerSessionRepository.getCompletedSessionDates()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 일별 최대 집중 시간을 조회합니다
     */
    private suspend fun getLongestDailyFocusTime(): Long {
        return try {
            timerSessionRepository.getLongestDailyFocusTime() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 평균 세션 길이를 조회합니다
     */
    private suspend fun getAverageSessionLength(): Long {
        return try {
            timerSessionRepository.getAverageSessionLength() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 전체 집중 시간을 조회합니다
     */
    private suspend fun getTotalFocusTime(): Long {
        return try {
            timerSessionRepository.getTotalFocusTime() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
