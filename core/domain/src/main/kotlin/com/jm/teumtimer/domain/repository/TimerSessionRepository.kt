package com.jm.teumtimer.domain.repository

import com.jm.teumtimer.domain.model.session.TimerSession
import kotlinx.coroutines.flow.Flow

/**
 * 타이머 세션 데이터 관리를 위한 Repository 인터페이스
 */
interface TimerSessionRepository {

    /**
     * 새로운 타이머 세션을 추가합니다.
     * @param session 추가할 세션
     * @return 생성된 세션의 ID
     */
    suspend fun insertSession(session: TimerSession): Long

    /**
     * 여러 타이머 세션을 한번에 추가합니다.
     * @param sessions 추가할 세션들
     */
    suspend fun insertSessions(sessions: List<TimerSession>)

    /**
     * 타이머 세션을 업데이트합니다.
     * @param session 업데이트할 세션
     */
    suspend fun updateSession(session: TimerSession)

    /**
     * 타이머 세션을 삭제합니다.
     * @param session 삭제할 세션
     */
    suspend fun deleteSession(session: TimerSession)

    /**
     * ID로 특정 타이머 세션을 조회합니다.
     * @param id 조회할 세션 ID
     * @return 세션 (없으면 null)
     */
    suspend fun getSessionById(id: Long): TimerSession?

    /**
     * 모든 타이머 세션을 조회합니다 (최신순).
     * @return 모든 세션의 Flow
     */
    fun getAllSessions(): Flow<List<TimerSession>>

    /**
     * 특정 프리셋의 모든 세션을 조회합니다.
     * @param presetId 프리셋 ID
     * @return 해당 프리셋의 세션들
     */
    fun getSessionsByPresetId(presetId: Int): Flow<List<TimerSession>>

    /**
     * 완료된 세션만 조회합니다.
     * @return 완료된 세션들의 Flow
     */
    fun getCompletedSessions(): Flow<List<TimerSession>>

    /**
     * 미완료된 세션만 조회합니다.
     * @return 미완료된 세션들의 Flow
     */
    fun getIncompleteSessions(): Flow<List<TimerSession>>

    /**
     * 특정 기간의 세션을 조회합니다.
     * @param startTime 시작 시간 (타임스탬프 밀리초)
     * @param endTime 종료 시간 (타임스탬프 밀리초)
     * @return 해당 기간의 세션들
     */
    fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<TimerSession>>

    /**
     * 특정 기간의 완료된 세션만 조회합니다.
     * @param startTime 시작 시간 (타임스탬프)
     * @param endTime 종료 시간 (타임스탬프)
     * @return 해당 기간의 완료된 세션들
     */
    fun getCompletedSessionsBetween(startTime: Long, endTime: Long): Flow<List<TimerSession>>

    /**
     * 특정 날짜의 총 집중 시간을 조회합니다.
     * @param startTime 시작 시간 (타임스탬프)
     * @param endTime 종료 시간 (타임스탬프)
     * @return 총 집중 시간 (밀리초)
     */
    suspend fun getTotalFocusTimeBetween(startTime: Long, endTime: Long): Long

    /**
     * 최근 N개의 세션을 조회합니다.
     * @param limit 조회할 세션 개수
     * @return 최근 세션들
     */
    fun getRecentSessions(limit: Int): Flow<List<TimerSession>>

    /**
     * 총 세션 수를 조회합니다.
     * @return 전체 세션 개수
     */
    suspend fun getTotalSessionCount(): Int

    /**
     * 완료된 세션 수를 조회합니다.
     * @return 완료된 세션 개수
     */
    suspend fun getCompletedSessionCount(): Int

    /**
     * 특정 프리셋의 세션 수를 조회합니다.
     * @param presetId 프리셋 ID
     * @return 해당 프리셋의 세션 개수
     */
    suspend fun getSessionCountByPresetId(presetId: Int): Int

    /**
     * 모든 세션을 삭제합니다.
     */
    suspend fun deleteAllSessions()

    /**
     * 특정 프리셋의 모든 세션을 삭제합니다.
     * @param presetId 프리셋 ID
     */
    suspend fun deleteSessionsByPresetId(presetId: Int)

    /**
     * 전체 세션의 평균 집중 시간을 조회합니다 (완료된 세션만).
     * @return 평균 집중 시간 (밀리초)
     */
    suspend fun getAverageSessionLength(): Long?

    /**
     * 전체 완료된 세션의 총 집중 시간을 조회합니다.
     * @return 총 집중 시간 (밀리초)
     */
    suspend fun getTotalFocusTime(): Long?

    /**
     * 날짜별로 그룹화된 완료된 세션의 날짜를 조회합니다 (연속 일수 계산용).
     * @return 날짜 문자열 리스트 (yyyy-MM-dd 형식)
     */
    suspend fun getCompletedSessionDates(): List<String>

    /**
     * 일별 최대 집중 시간을 조회합니다.
     * @return 일별 최대 집중 시간 (밀리초)
     */
    suspend fun getLongestDailyFocusTime(): Long?
}
