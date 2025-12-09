package com.jm.focustimer.domain.repository

import com.jm.focustimer.domain.model.statistics.Statistics
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * 통계 데이터 관리를 위한 Repository 인터페이스
 */
interface StatisticsRepository {

    /**
     * 새로운 통계를 추가합니다.
     * @param statistics 추가할 통계
     * @return 생성된 통계의 ID
     */
    suspend fun insertStatistics(statistics: Statistics): Long

    /**
     * 여러 통계를 한번에 추가합니다.
     * @param statistics 추가할 통계들
     */
    suspend fun insertStatistics(statistics: List<Statistics>)

    /**
     * 통계를 업데이트합니다.
     * @param statistics 업데이트할 통계
     */
    suspend fun updateStatistics(statistics: Statistics)

    /**
     * 통계를 삭제합니다.
     * @param statistics 삭제할 통계
     */
    suspend fun deleteStatistics(statistics: Statistics)

    /**
     * ID로 특정 통계를 조회합니다.
     * @param id 조회할 통계 ID
     * @return 통계 (없으면 null)
     */
    suspend fun getStatisticsById(id: Int): Statistics?

    /**
     * 특정 날짜의 통계를 조회합니다.
     * @param date 조회할 날짜 (epoch day)
     * @return 통계 (없으면 null)
     */
    suspend fun getStatisticsByDate(date: Long): Statistics?

    /**
     * 모든 통계를 조회합니다 (최신순).
     * @return 모든 통계의 Flow
     */
    fun getAllStatistics(): Flow<List<Statistics>>

    /**
     * 특정 기간의 통계를 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 해당 기간의 통계들
     */
    fun getStatisticsBetween(startDate: Long, endDate: Long): Flow<List<Statistics>>

    /**
     * 최근 N일의 통계를 조회합니다.
     * @param limit 조회할 일수
     * @return 최근 통계들
     */
    fun getRecentStatistics(limit: Int): Flow<List<Statistics>>

    /**
     * 총 통계 수를 조회합니다.
     * @return 전체 통계 개수
     */
    suspend fun getTotalStatisticsCount(): Int

    /**
     * 특정 기간의 총 집중 시간을 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 총 집중 시간
     */
    suspend fun getTotalFocusTimeBetween(startDate: Long, endDate: Long): Duration?

    /**
     * 특정 기간의 평균 집중 달성률을 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 평균 집중 달성률
     */
    suspend fun getAverageFocusRateBetween(startDate: Long, endDate: Long): Double?

    /**
     * 특정 기간의 평균 세션 시간을 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 평균 세션 시간
     */
    suspend fun getAverageSessionLengthBetween(startDate: Long, endDate: Long): Duration?

    /**
     * 특정 기간의 총 완료 세션 수를 조회합니다.
     * @param startDate 시작 날짜 (epoch day)
     * @param endDate 종료 날짜 (epoch day)
     * @return 총 완료 세션 수
     */
    suspend fun getTotalCompletedSessionsBetween(startDate: Long, endDate: Long): Int?

    /**
     * 모든 통계를 삭제합니다.
     */
    suspend fun deleteAllStatistics()

    /**
     * 특정 날짜 이전의 통계를 삭제합니다.
     * @param date 기준 날짜 (epoch day)
     */
    suspend fun deleteStatisticsBefore(date: Long)
}
