package com.jm.harufocus.core.database.util

import com.jm.harufocus.core.database.model.TimerSessionEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

/**
 * 통계 화면 테스트용 더미 데이터 생성 유틸리티 클래스
 */
object DummyDataGenerator {

    /**
     * 지정된 기간 동안의 더미 세션 데이터를 생성합니다.
     * 
     * @param daysBefore 며칠 전까지의 데이터를 생성할지 (0 = 오늘만, 7 = 최근 1주일)
     * @param sessionsPerDay 하루당 생성할 세션 개수 범위 (실제로는 랜덤하게 조정됨)
     * @return 생성된 더미 세션 리스트
     */
    fun generateDummySessions(
        daysBefore: Int = 30, // 기본적으로 최근 30일
        sessionsPerDay: IntRange = 2..6 // 하루에 2~6개 세션
    ): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        val today = LocalDate.now()
        
        repeat(daysBefore + 1) { dayIndex ->
            val targetDate = today.minusDays(dayIndex.toLong())
            val dailySessions = generateDailySessionsForDate(targetDate, sessionsPerDay)
            sessions.addAll(dailySessions)
        }
        
        return sessions.sortedBy { it.startTime } // 시간순으로 정렬
    }

    /**
     * 특정 날짜에 대한 더미 세션들을 생성합니다.
     */
    private fun generateDailySessionsForDate(
        date: LocalDate,
        sessionsPerDayRange: IntRange
    ): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        
        // 하루에 생성할 세션 개수를 랜덤하게 결정 (0~90% 확률로 세션 있음)
        val sessionCount = if (Random.nextFloat() < 0.9f) {
            Random.nextInt(sessionsPerDayRange.first, sessionsPerDayRange.last + 1)
        } else {
            0 // 10% 확률로 해당 날짜에는 세션이 없음
        }
        
        if (sessionCount == 0) return sessions
        
        // 하루 중 집중하기 좋은 시간대들을 정의
        val focusTimePeriods = listOf(
            9..11,   // 오전 집중 시간
            14..16,  // 오후 집중 시간  
            19..21   // 저녁 집중 시간
        )
        
        repeat(sessionCount) { sessionIndex ->
            val session = generateSingleSession(
                date = date,
                sessionIndex = sessionIndex,
                focusTimePeriods = focusTimePeriods
            )
            if (session.duration <= 60.minutes.inWholeMilliseconds) sessions.add(session)
        }
        
        return sessions
    }

    /**
     * 단일 세션을 생성합니다.
     */
    private fun generateSingleSession(
        date: LocalDate,
        sessionIndex: Int,
        focusTimePeriods: List<IntRange>
    ): TimerSessionEntity {
        // 집중 시간대 중 하나를 선택
        val selectedPeriod = focusTimePeriods[Random.nextInt(focusTimePeriods.size)]
        val hour = Random.nextInt(selectedPeriod.first, selectedPeriod.last + 1)
        val minute = Random.nextInt(0, 60)
        
        val startDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
        val startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant()
        
        // 세션 길이 결정 (15분~90분, 포모도로 타이머를 고려해 25분 배수가 많게)
        val possibleDurations = listOf(
            5.minutes, 10.minutes, 15.minutes, // 짧은 세션
            20.minutes, 25.minutes, 30.minutes, // 중간 세션
            35.minutes, 40.minutes, 45.minutes  // 긴 세션
        )
        
        val duration = possibleDurations[Random.nextInt(possibleDurations.size)]
        val endTime = startTime.plusMillis(duration.inWholeMilliseconds)
        
        // 95% 확률로 완료됨
        val completed = Random.nextFloat() < 0.95f
        
        // 완료된 세션의 10% 확률로 초과 시간 있음
        val overrunTime = if (completed && Random.nextFloat() < 0.1f) {
            Random.nextLong(1, 10).minutes // 1~10분 초과
        } else {
            null
        }
        
        return TimerSessionEntity(
            id = 0, // 자동 생성됨
            presetId = Random.nextInt(1, 4), // 1~3 중 하나의 프리셋 ID
            startTime = startTime.toEpochMilli(),
            endTime = if (completed) endTime.toEpochMilli() else null,
            duration = duration.inWholeMilliseconds,
            completed = completed,
            overrunTime = overrunTime?.inWholeMilliseconds
        )
    }

    /**
     * 특별한 패턴의 더미 데이터를 생성합니다 (차트 테스트용).
     * 특정 시간대에 집중된 세션들을 생성해 차트가 잘 보이도록 합니다.
     */
    fun generatePatternedDummySessions(): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        val today = LocalDate.now()
        
        // 오늘 데이터 - 뚜렷한 패턴으로 생성
        val todaySessions = listOf(
            // 오전 9-11시에 집중적으로 세션
            createSessionAt(today, 9, 0, 25.minutes),
            createSessionAt(today, 9, 30, 25.minutes),
            createSessionAt(today, 10, 15, 30.minutes),
            
            // 오후 2-4시에 집중적으로 세션  
            createSessionAt(today, 14, 0, 45.minutes),
            createSessionAt(today, 15, 0, 25.minutes),
            createSessionAt(today, 15, 30, 30.minutes),
            
            // 저녁 7-8시에 세션
            createSessionAt(today, 19, 0, 50.minutes),
            createSessionAt(today, 20, 0, 25.minutes)
        )
        sessions.addAll(todaySessions)
        
        // 어제 데이터 - 다른 패턴
        val yesterday = today.minusDays(1)
        val yesterdaySessions = listOf(
            createSessionAt(yesterday, 8, 30, 30.minutes),
            createSessionAt(yesterday, 13, 0, 25.minutes),  
            createSessionAt(yesterday, 16, 30, 40.minutes),
            createSessionAt(yesterday, 21, 0, 35.minutes)
        )
        sessions.addAll(yesterdaySessions)
        
        return sessions
    }

    /**
     * 특정 시간에 세션을 생성하는 헬퍼 함수
     */
    private fun createSessionAt(
        date: LocalDate,
        hour: Int,
        minute: Int,
        duration: kotlin.time.Duration
    ): TimerSessionEntity {
        val startDateTime = LocalDateTime.of(date, LocalTime.of(hour, minute))
        val startTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant()
        val endTime = startTime.plusMillis(duration.inWholeMilliseconds)
        
        return TimerSessionEntity(
            id = 0,
            presetId = Random.nextInt(1, 4),
            startTime = startTime.toEpochMilli(),
            endTime = endTime.toEpochMilli(),
            duration = duration.inWholeMilliseconds,
            completed = true,
            overrunTime = null
        )
    }

    /**
     * 주간 통계 테스트용 더미 데이터 생성
     */
    fun generateWeeklyDummySessions(): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        val today = LocalDate.now()
        
        // 최근 7일간 데이터 생성
        repeat(7) { dayIndex ->
            val targetDate = today.minusDays(dayIndex.toLong())
            val dailyFocusTime = when (dayIndex) {
                0 -> 180.minutes // 오늘: 3시간
                1 -> 150.minutes // 어제: 2.5시간  
                2 -> 120.minutes // 그제: 2시간
                3 -> 200.minutes // 3일전: 3시간 20분
                4 -> 90.minutes  // 4일전: 1.5시간
                5 -> 0.minutes   // 5일전: 휴식일
                6 -> 240.minutes // 6일전: 4시간
                else -> 120.minutes
            }
            
            if (dailyFocusTime > 0.minutes) {
                val dailySessions = generateSessionsForTotalDuration(targetDate, dailyFocusTime)
                sessions.addAll(dailySessions)
            }
        }
        
        return sessions
    }

    /**
     * 특정 총 시간이 되도록 세션들을 생성합니다.
     */
    private fun generateSessionsForTotalDuration(
        date: LocalDate,
        totalDuration: kotlin.time.Duration
    ): List<TimerSessionEntity> {
        val sessions = mutableListOf<TimerSessionEntity>()
        var remainingDuration = totalDuration
        
        val sessionLengths = listOf(25.minutes, 30.minutes, 45.minutes, 50.minutes)
        var hour = 9
        
        while (remainingDuration > 0.minutes && hour <= 21) {
            val sessionLength = sessionLengths.random()
            val actualLength = minOf(sessionLength, remainingDuration)
            
            val session = createSessionAt(date, hour, 0, actualLength)
            sessions.add(session)
            
            remainingDuration -= actualLength
            hour += 2 // 2시간 간격으로 세션 배치
        }
        
        return sessions
    }
}