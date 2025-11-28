package com.jm.focustimer.data.repository

import com.jm.focustimer.core.database.dao.TimerSessionDao
import com.jm.focustimer.core.database.model.toTimerSession
import com.jm.focustimer.core.database.model.toTimerSessionEntity
import com.jm.focustimer.domain.model.TimerSession
import com.jm.focustimer.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * TimerSessionRepository의 기본 구현체
 * Room Database를 통해 타이머 세션 데이터를 관리합니다.
 */
class TimerSessionRepositoryImpl @Inject constructor(
    private val timerSessionDao: TimerSessionDao
) : TimerSessionRepository {

    override suspend fun insertSession(session: TimerSession): Long {
        return timerSessionDao.insertSession(session.toTimerSessionEntity())
    }

    override suspend fun insertSessions(sessions: List<TimerSession>) {
        timerSessionDao.insertSessions(sessions.map { it.toTimerSessionEntity() })
    }

    override suspend fun updateSession(session: TimerSession) {
        timerSessionDao.updateSession(session.toTimerSessionEntity())
    }

    override suspend fun deleteSession(session: TimerSession) {
        timerSessionDao.deleteSession(session.toTimerSessionEntity())
    }

    override suspend fun getSessionById(id: Int): TimerSession? {
        return timerSessionDao.getSessionById(id)?.toTimerSession()
    }

    override fun getAllSessions(): Flow<List<TimerSession>> {
        return timerSessionDao.getAllSessions()
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override fun getSessionsByPresetId(presetId: Int): Flow<List<TimerSession>> {
        return timerSessionDao.getSessionsByPresetId(presetId)
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override fun getCompletedSessions(): Flow<List<TimerSession>> {
        return timerSessionDao.getCompletedSessions()
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override fun getIncompleteSessions(): Flow<List<TimerSession>> {
        return timerSessionDao.getIncompleteSessions()
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<TimerSession>> {
        return timerSessionDao.getSessionsBetween(startTime, endTime)
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override fun getCompletedSessionsBetween(startTime: Long, endTime: Long): Flow<List<TimerSession>> {
        return timerSessionDao.getCompletedSessionsBetween(startTime, endTime)
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override suspend fun getTotalFocusTimeBetween(startTime: Long, endTime: Long): Long {
        return timerSessionDao.getTotalFocusTimeBetween(startTime, endTime) ?: 0
    }

    override fun getRecentSessions(limit: Int): Flow<List<TimerSession>> {
        return timerSessionDao.getRecentSessions(limit)
            .map { entities -> entities.map { it.toTimerSession() } }
    }

    override suspend fun getTotalSessionCount(): Int {
        return timerSessionDao.getTotalSessionCount()
    }

    override suspend fun getCompletedSessionCount(): Int {
        return timerSessionDao.getCompletedSessionCount()
    }

    override suspend fun getSessionCountByPresetId(presetId: Int): Int {
        return timerSessionDao.getSessionCountByPresetId(presetId)
    }

    override suspend fun deleteAllSessions() {
        timerSessionDao.deleteAllSessions()
    }

    override suspend fun deleteSessionsByPresetId(presetId: Int) {
        timerSessionDao.deleteSessionsByPresetId(presetId)
    }

    override suspend fun getAverageSessionLength(): Long? {
        return timerSessionDao.getAverageSessionLength()
    }

    override suspend fun getTotalFocusTime(): Long? {
        return timerSessionDao.getTotalFocusTime()
    }

    override suspend fun getCompletedSessionDates(): List<String> {
        return timerSessionDao.getCompletedSessionDates()
    }

    override suspend fun getLongestDailyFocusTime(): Long? {
        return timerSessionDao.getLongestDailyFocusTime()
    }
}
