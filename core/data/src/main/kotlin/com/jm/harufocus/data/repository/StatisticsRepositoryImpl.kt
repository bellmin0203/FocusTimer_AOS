package com.jm.harufocus.data.repository

import com.jm.harufocus.core.database.dao.StatisticsDao
import com.jm.harufocus.core.database.model.toStatistics
import com.jm.harufocus.core.database.model.toStatisticsEntity
import com.jm.harufocus.domain.model.statistics.Statistics
import com.jm.harufocus.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * StatisticsRepository의 기본 구현체
 * Room Database를 통해 통계 데이터를 관리합니다.
 */
class StatisticsRepositoryImpl @Inject constructor(
    private val statisticsDao: StatisticsDao
) : StatisticsRepository {

    override suspend fun insertStatistics(statistics: Statistics): Long {
        return statisticsDao.insertStatistics(statistics.toStatisticsEntity())
    }

    override suspend fun insertStatistics(statistics: List<Statistics>) {
        statisticsDao.insertStatistics(statistics.map { it.toStatisticsEntity() })
    }

    override suspend fun updateStatistics(statistics: Statistics) {
        statisticsDao.updateStatistics(statistics.toStatisticsEntity())
    }

    override suspend fun deleteStatistics(statistics: Statistics) {
        statisticsDao.deleteStatistics(statistics.toStatisticsEntity())
    }

    override suspend fun getStatisticsById(id: Int): Statistics? {
        return statisticsDao.getStatisticsById(id)?.toStatistics()
    }

    override suspend fun getStatisticsByDate(date: Long): Statistics? {
        return statisticsDao.getStatisticsByDate(date)?.toStatistics()
    }

    override fun getAllStatistics(): Flow<List<Statistics>> {
        return statisticsDao.getAllStatistics()
            .map { entities -> entities.map { it.toStatistics() } }
    }

    override fun getStatisticsBetween(startDate: Long, endDate: Long): Flow<List<Statistics>> {
        return statisticsDao.getStatisticsBetween(startDate, endDate)
            .map { entities -> entities.map { it.toStatistics() } }
    }

    override fun getRecentStatistics(limit: Int): Flow<List<Statistics>> {
        return statisticsDao.getRecentStatistics(limit)
            .map { entities -> entities.map { it.toStatistics() } }
    }

    override suspend fun getTotalStatisticsCount(): Int {
        return statisticsDao.getTotalStatisticsCount()
    }

    override suspend fun getTotalFocusTimeBetween(startDate: Long, endDate: Long): Duration? {
        return statisticsDao.getTotalFocusTimeBetween(startDate, endDate)?.milliseconds
    }

    override suspend fun getAverageFocusRateBetween(startDate: Long, endDate: Long): Double? {
        return statisticsDao.getAverageFocusRateBetween(startDate, endDate)
    }

    override suspend fun getAverageSessionLengthBetween(startDate: Long, endDate: Long): Duration? {
        return statisticsDao.getAverageSessionLengthBetween(startDate, endDate)?.milliseconds
    }

    override suspend fun getTotalCompletedSessionsBetween(startDate: Long, endDate: Long): Int? {
        return statisticsDao.getTotalCompletedSessionsBetween(startDate, endDate)
    }

    override suspend fun deleteAllStatistics() {
        statisticsDao.deleteAllStatistics()
    }

    override suspend fun deleteStatisticsBefore(date: Long) {
        statisticsDao.deleteStatisticsBefore(date)
    }
}
