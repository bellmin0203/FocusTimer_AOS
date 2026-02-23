package com.jm.harufocus.core.database.util

import com.jm.harufocus.core.database.model.TimerSessionEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/**
 * Utility to generate dummy timer sessions for statistics/debug screens.
 */
object DummyDataGenerator {
    private const val SESSION_COMPLETION_RATE = 0.95f
    private const val PARTIAL_COMPLETION_RATE = 0.20f
    private const val PARTIAL_MIN_RATIO = 0.35
    private const val PARTIAL_MAX_RATIO = 0.85

    private data class CompletionState(
        val completed: Boolean,
        val isPartial: Boolean,
    )

    /**
     * Generates dummy sessions for the last [daysBefore] days (inclusive).
     */
    fun generateDummySessions(
        daysBefore: Int = 30,
        sessionsPerDay: IntRange = 2..6,
    ): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        val today = LocalDate.now()

        repeat(daysBefore + 1) { dayIndex ->
            val targetDate = today.minusDays(dayIndex.toLong())
            sessions += generateDailySessionsForDate(targetDate, sessionsPerDay)
        }

        return sessions.sortedBy { it.startTime }
    }

    private fun generateDailySessionsForDate(
        date: LocalDate,
        sessionsPerDayRange: IntRange,
    ): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()

        val sessionCount = if (Random.nextFloat() < 0.9f) {
            Random.nextInt(sessionsPerDayRange.first, sessionsPerDayRange.last + 1)
        } else {
            0
        }

        if (sessionCount == 0) return sessions

        val focusTimePeriods = listOf(
            9..11,
            14..16,
            19..21,
        )

        repeat(sessionCount) {
            val session = generateSingleSession(
                date = date,
                focusTimePeriods = focusTimePeriods,
            )

            if (session.duration <= 60.minutes.inWholeMilliseconds) {
                sessions += session
            }
        }

        return sessions
    }

    private fun generateSingleSession(
        date: LocalDate,
        focusTimePeriods: List<IntRange>,
    ): TimerSessionEntity {
        val selectedPeriod = focusTimePeriods[Random.nextInt(focusTimePeriods.size)]
        val hour = Random.nextInt(selectedPeriod.first, selectedPeriod.last + 1)
        val minute = Random.nextInt(0, 60)

        val startDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
        val startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant()

        val possibleDurations = listOf(
            5.minutes,
            10.minutes,
            15.minutes,
            20.minutes,
            25.minutes,
            30.minutes,
            35.minutes,
            40.minutes,
            45.minutes,
        )

        val targetDuration = possibleDurations[Random.nextInt(possibleDurations.size)]
        val completionState = generateCompletionState()

        val actualDuration = if (completionState.isPartial) {
            createPartialDuration(targetDuration)
        } else {
            targetDuration
        }

        val endTime = if (completionState.completed) {
            startTime.plusMillis(actualDuration.inWholeMilliseconds)
        } else {
            null
        }

        val overrunTime = if (completionState.completed && !completionState.isPartial && Random.nextFloat() < 0.1f) {
            Random.nextLong(1, 10).minutes
        } else {
            null
        }

        return TimerSessionEntity(
            id = 0,
            presetId = Random.nextInt(1, 4),
            startTime = startTime.toEpochMilli(),
            endTime = endTime?.toEpochMilli(),
            duration = actualDuration.inWholeMilliseconds,
            completed = completionState.completed,
            overrunTime = overrunTime?.inWholeMilliseconds,
            isPartial = completionState.isPartial,
        )
    }

    /**
     * Generates sessions with a readable hour-pattern for chart testing.
     */
    fun generatePatternedDummySessions(): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        val today = LocalDate.now()

        val todaySessions = listOf(
            createSessionAt(today, 9, 0, 25.minutes),
            createSessionAt(today, 9, 30, 25.minutes),
            createSessionAt(today, 10, 15, 30.minutes),
            createSessionAt(today, 14, 0, 45.minutes),
            createSessionAt(today, 15, 0, 25.minutes),
            createSessionAt(today, 15, 30, 30.minutes, isPartial = true),
            createSessionAt(today, 19, 0, 50.minutes),
            createSessionAt(today, 20, 0, 25.minutes, isPartial = true),
        )
        sessions += todaySessions

        val yesterday = today.minusDays(1)
        val yesterdaySessions = listOf(
            createSessionAt(yesterday, 8, 30, 30.minutes),
            createSessionAt(yesterday, 13, 0, 25.minutes),
            createSessionAt(yesterday, 16, 30, 40.minutes, isPartial = true),
            createSessionAt(yesterday, 21, 0, 35.minutes),
        )
        sessions += yesterdaySessions

        return sessions
    }

    private fun createSessionAt(
        date: LocalDate,
        hour: Int,
        minute: Int,
        duration: Duration,
        isPartial: Boolean = false,
    ): TimerSessionEntity {
        val startDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
        val startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant()

        val actualDuration = if (isPartial) createPartialDuration(duration) else duration
        val endTime = startTime.plusMillis(actualDuration.inWholeMilliseconds)

        return TimerSessionEntity(
            id = 0,
            presetId = Random.nextInt(1, 4),
            startTime = startTime.toEpochMilli(),
            endTime = endTime.toEpochMilli(),
            duration = actualDuration.inWholeMilliseconds,
            completed = true,
            overrunTime = null,
            isPartial = isPartial,
        )
    }

    /**
     * Generates weekly dummy sessions for trend testing.
     */
    fun generateWeeklyDummySessions(): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        val today = LocalDate.now()

        repeat(7) { dayIndex ->
            val targetDate = today.minusDays(dayIndex.toLong())
            val dailyFocusTime = when (dayIndex) {
                0 -> 180.minutes
                1 -> 150.minutes
                2 -> 120.minutes
                3 -> 200.minutes
                4 -> 90.minutes
                5 -> 0.minutes
                6 -> 240.minutes
                else -> 120.minutes
            }

            if (dailyFocusTime > 0.minutes) {
                sessions += generateSessionsForTotalDuration(targetDate, dailyFocusTime)
            }
        }

        return sessions
    }

    private fun generateSessionsForTotalDuration(
        date: LocalDate,
        totalDuration: Duration,
    ): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        var remainingDuration = totalDuration

        val sessionLengths = listOf(25.minutes, 30.minutes, 45.minutes, 50.minutes)
        var hour = 9

        while (remainingDuration > 0.minutes && hour <= 21) {
            val sessionLength = sessionLengths.random()
            val requestedLength = minOf(sessionLength, remainingDuration)
            val isPartial = Random.nextFloat() < PARTIAL_COMPLETION_RATE

            val session = createSessionAt(
                date = date,
                hour = hour,
                minute = 0,
                duration = requestedLength,
                isPartial = isPartial,
            )
            sessions += session

            remainingDuration -= session.duration.milliseconds
            hour += 2
        }

        return sessions
    }

    private fun generateCompletionState(): CompletionState {
        val completed = Random.nextFloat() < SESSION_COMPLETION_RATE
        val isPartial = completed && Random.nextFloat() < PARTIAL_COMPLETION_RATE

        return CompletionState(
            completed = completed,
            isPartial = isPartial,
        )
    }

    private fun createPartialDuration(duration: Duration): Duration {
        val ratio = Random.nextDouble(PARTIAL_MIN_RATIO, PARTIAL_MAX_RATIO)
        val partialDurationMillis = (duration.inWholeMilliseconds * ratio).toLong()
            .coerceAtLeast(1.minutes.inWholeMilliseconds)
            .coerceAtMost(duration.inWholeMilliseconds)

        return partialDurationMillis.milliseconds
    }
}
