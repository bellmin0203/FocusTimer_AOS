package com.jm.focustimer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jm.focustimer.core.database.model.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * 타이머 세션 데이터베이스 접근을 위한 DAO 인터페이스
 */
@Dao
interface TimerSessionDao {

    /**
     * 새로운 타이머 세션을 추가합니다.
     * @param session 추가할 세션
     * @return 생성된 세션의 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TimerSessionEntity): Long

    /**
     * 여러 타이머 세션을 한번에 추가합니다.
     * @param sessions 추가할 세션들
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<TimerSessionEntity>)

    /**
     * 타이머 세션을 업데이트합니다.
     * @param session 업데이트할 세션
     */
    @Update
    suspend fun updateSession(session: TimerSessionEntity)

    /**
     * 타이머 세션을 삭제합니다.
     * @param session 삭제할 세션
     */
    @Delete
    suspend fun deleteSession(session: TimerSessionEntity)

    /**
     * ID로 특정 타이머 세션을 조회합니다.
     * @param id 조회할 세션 ID
     * @return 세션 엔티티 (없으면 null)
     */
    @Query("SELECT * FROM timer_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): TimerSessionEntity?

    /**
     * 모든 타이머 세션을 조회합니다 (최신순).
     * @return 모든 세션의 Flow
     */
    @Query("SELECT * FROM timer_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<TimerSessionEntity>>

    /**
     * 특정 프리셋의 모든 세션을 조회합니다.
     * @param presetId 프리셋 ID
     * @return 해당 프리셋의 세션들
     */
    @Query("SELECT * FROM timer_sessions WHERE preset_id = :presetId ORDER BY start_time DESC")
    fun getSessionsByPresetId(presetId: Int): Flow<List<TimerSessionEntity>>

    /**
     * 완료된 세션만 조회합니다.
     * @return 완료된 세션들의 Flow
     */
    @Query("SELECT * FROM timer_sessions WHERE completed = 1 ORDER BY start_time DESC")
    fun getCompletedSessions(): Flow<List<TimerSessionEntity>>

    /**
     * 미완료된 세션만 조회합니다.
     * @return 미완료된 세션들의 Flow
     */
    @Query("SELECT * FROM timer_sessions WHERE completed = 0 ORDER BY start_time DESC")
    fun getIncompleteSessions(): Flow<List<TimerSessionEntity>>

    /**
     * 특정 기간의 세션을 조회합니다.
     * @param startTime 시작 시간 (타임스탬프)
     * @param endTime 종료 시간 (타임스탬프)
     * @return 해당 기간의 세션들
     */
    @Query("SELECT * FROM timer_sessions WHERE start_time BETWEEN :startTime AND :endTime ORDER BY start_time DESC")
    fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<TimerSessionEntity>>

    /**
     * 최근 N개의 세션을 조회합니다.
     * @param limit 조회할 세션 개수
     * @return 최근 세션들
     */
    @Query("SELECT * FROM timer_sessions ORDER BY start_time DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<TimerSessionEntity>>

    /**
     * 총 세션 수를 조회합니다.
     * @return 전체 세션 개수
     */
    @Query("SELECT COUNT(*) FROM timer_sessions")
    suspend fun getTotalSessionCount(): Int

    /**
     * 완료된 세션 수를 조회합니다.
     * @return 완료된 세션 개수
     */
    @Query("SELECT COUNT(*) FROM timer_sessions WHERE completed = 1")
    suspend fun getCompletedSessionCount(): Int

    /**
     * 특정 프리셋의 세션 수를 조회합니다.
     * @param presetId 프리셋 ID
     * @return 해당 프리셋의 세션 개수
     */
    @Query("SELECT COUNT(*) FROM timer_sessions WHERE preset_id = :presetId")
    suspend fun getSessionCountByPresetId(presetId: Int): Int

    /**
     * 모든 세션을 삭제합니다.
     */
    @Query("DELETE FROM timer_sessions")
    suspend fun deleteAllSessions()

    /**
     * 특정 프리셋의 모든 세션을 삭제합니다.
     * @param presetId 프리셋 ID
     */
    @Query("DELETE FROM timer_sessions WHERE preset_id = :presetId")
    suspend fun deleteSessionsByPresetId(presetId: Int)

    /**
     * 특정 기간의 완료된 세션만 조회합니다.
     * @param startTime 시작 시간 (타임스탬프)
     * @param endTime 종료 시간 (타임스탬프)
     * @return 해당 기간의 완료된 세션들
     */
    @Query("SELECT * FROM timer_sessions WHERE completed = 1 AND start_time BETWEEN :startTime AND :endTime ORDER BY start_time ASC")
    fun getCompletedSessionsBetween(
        startTime: Long,
        endTime: Long
    ): Flow<List<TimerSessionEntity>>

    /**
     * 특정 날짜의 총 집중 시간을 조회합니다.
     * @param startTime 시작 시간 (타임스탬프)
     * @param endTime 종료 시간 (타임스탬프)
     * @return 총 집중 시간 (밀리초)
     */
    @Query("SELECT SUM(duration) FROM timer_sessions WHERE completed = 1 AND start_time BETWEEN :startTime AND :endTime")
    suspend fun getTotalFocusTimeBetween(startTime: Long, endTime: Long): Long?

    /**
     * 특정 날짜의 완료된 세션 수를 조회합니다.
     * @param startTime 시작 시간 (타임스탬프)
     * @param endTime 종료 시간 (타임스탬프)
     * @return 완료된 세션 개수
     */
    @Query("SELECT COUNT(*) FROM timer_sessions WHERE completed = 1 AND start_time BETWEEN :startTime AND :endTime")
    suspend fun getCompletedSessionCountBetween(startTime: Long, endTime: Long): Int

    /**
     * 전체 세션의 평균 집중 시간을 조회합니다 (완료된 세션만).
     * @return 평균 집중 시간 (밀리초)
     */
    @Query("SELECT AVG(duration) FROM timer_sessions WHERE completed = 1")
    suspend fun getAverageSessionLength(): Long?

    /**
     * 전체 완료된 세션의 총 집중 시간을 조회합니다.
     * @return 총 집중 시간 (밀리초)
     */
    @Query("SELECT SUM(duration) FROM timer_sessions WHERE completed = 1")
    suspend fun getTotalFocusTime(): Long?

    /**
     * 날짜별로 그룹화된 완료된 세션의 시작 시간을 조회합니다 (연속 일수 계산용).
     * @return 날짜별 세션 시작 시간 리스트
     */
    @Query("""
        SELECT DISTINCT DATE(start_time / 1000, 'unixepoch', 'localtime') as date 
        FROM timer_sessions 
        WHERE completed = 1 
        ORDER BY date DESC
        """)
    suspend fun getCompletedSessionDates(): List<String>

    /**
     * 일별 최대 집중 시간을 조회합니다.
     * @return 일별 최대 집중 시간 (밀리초)
     */
    @Query("""
        SELECT MAX(daily_total) 
        FROM (
            SELECT DATE(start_time / 1000, 'unixepoch', 'localtime') as date, SUM(duration) as daily_total 
            FROM timer_sessions 
            WHERE completed = 1 
            GROUP BY date
        )
        """)
    suspend fun getLongestDailyFocusTime(): Long?
}