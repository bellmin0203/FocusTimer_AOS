package com.jm.focustimer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jm.focustimer.core.database.model.StatisticsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 통계 데이터베이스 접근을 위한 DAO 인터페이스
 */
@Dao
interface StatisticsDao {

    /**
     * 새로운 통계를 추가합니다.
     * @param statistics 추가할 통계
     * @return 생성된 통계의 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: StatisticsEntity): Long

    /**
     * 여러 통계를 한번에 추가합니다.
     * @param statistics 추가할 통계들
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: List<StatisticsEntity>)

    /**
     * 통계를 업데이트합니다.
     * @param statistics 업데이트할 통계
     */
    @Update
    suspend fun updateStatistics(statistics: StatisticsEntity)

    /**
     * 통계를 삭제합니다.
     * @param statistics 삭제할 통계
     */
    @Delete
    suspend fun deleteStatistics(statistics: StatisticsEntity)

    /**
     * ID로 특정 통계를 조회합니다.
     * @param id 조회할 통계 ID
     * @return 통계 엔티티 (없으면 null)
     */
    @Query("SELECT * FROM statistics WHERE id = :id")
    suspend fun getStatisticsById(id: Int): StatisticsEntity?

    /**
     * 특정 날짜의 통계를 조회합니다.
     * @param date 조회할 날짜 (epoch day)
     * @return 통계 엔티티 (없으면 null)
     */
    @Query("SELECT * FROM statistics WHERE date = :date")
    suspend fun getStatisticsByDate(date: Long): StatisticsEntity?

    /**
     * 모든 통계를 조회합니다 (최신순).
     * @return 모든 통계의 Flow
     */
    @Query("SELECT * FROM statistics ORDER BY date DESC")
    fun getAllStatistics(): Flow<List<StatisticsEntity>>

    /**
     * 특정 기간의 통계를 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 해당 기간의 통계들
     */
    @Query("SELECT * FROM statistics WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStatisticsBetween(startDate: Long, endDate: Long): Flow<List<StatisticsEntity>>

    /**
     * 최근 N일의 통계를 조회합니다.
     * @param limit 조회할 일수
     * @return 최근 통계들
     */
    @Query("SELECT * FROM statistics ORDER BY date DESC LIMIT :limit")
    fun getRecentStatistics(limit: Int): Flow<List<StatisticsEntity>>

    /**
     * 총 통계 수를 조회합니다.
     * @return 전체 통계 개수
     */
    @Query("SELECT COUNT(*) FROM statistics")
    suspend fun getTotalStatisticsCount(): Int

    /**
     * 특정 기간의 총 집중 시간을 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 총 집중 시간 (밀리초)
     */
    @Query("SELECT SUM(total_focus_time) FROM statistics WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalFocusTimeBetween(startDate: Long, endDate: Long): Long?

    /**
     * 특정 기간의 평균 집중 달성률을 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 평균 집중 달성률
     */
    @Query("SELECT AVG(focus_rate) FROM statistics WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageFocusRateBetween(startDate: Long, endDate: Long): Double?
    
    /**
     * 특정 기간의 평균 세션 시간을 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 평균 세션 시간 (밀리초)
     */
    @Query("SELECT AVG(average_session_length) FROM statistics WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageSessionLengthBetween(startDate: Long, endDate: Long): Long?

    /**
     * 특정 기간의 총 완료 세션 수를 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 총 완료 세션 수
     */
    @Query("SELECT SUM(completed_sessions) FROM statistics WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCompletedSessionsBetween(startDate: Long, endDate: Long): Int?

    /**
     * 모든 통계를 삭제합니다.
     */
    @Query("DELETE FROM statistics")
    suspend fun deleteAllStatistics()

    /**
     * 특정 날짜 이전의 통계를 삭제합니다.
     * @param date 기준 날짜 (epoch day)
     */
    @Query("DELETE FROM statistics WHERE date < :date")
    suspend fun deleteStatisticsBefore(date: Long)
}
