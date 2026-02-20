package com.jm.harufocus.data.util

import com.jm.harufocus.core.database.dao.TimerSessionDao
import com.jm.harufocus.core.database.util.DummyDataGenerator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 더미 데이터 생성을 도와주는 헬퍼 클래스
 * 디버그 빌드에서만 사용되며, 통계 화면 테스트용 더미 데이터를 생성합니다.
 */
@Singleton
class DummyDataHelper @Inject constructor(
    private val timerSessionDao: TimerSessionDao
) {
    
    /**
     * 랜덤한 30일치 더미 데이터를 생성합니다.
     * @return 생성된 세션 개수
     */
    suspend fun generateRandomMonthData(): Int {
        val sessions = DummyDataGenerator.generateDummySessions(30, 2..6)
        timerSessionDao.insertDummySessions(sessions)
        return sessions.size
    }
    
    /**
     * 차트 테스트용 패턴 데이터를 생성합니다.
     * 특정 시간대에 집중된 세션들로 차트가 잘 보이도록 합니다.
     * @return 생성된 세션 개수
     */
    suspend fun generatePatternedData(): Int {
        val sessions = DummyDataGenerator.generatePatternedDummySessions()
        timerSessionDao.insertDummySessions(sessions)
        return sessions.size
    }
    
    /**
     * 주간 통계 테스트용 데이터를 생성합니다.
     * @return 생성된 세션 개수  
     */
    suspend fun generateWeeklyTestData(): Int {
        val sessions = DummyDataGenerator.generateWeeklyDummySessions()
        timerSessionDao.insertDummySessions(sessions)
        return sessions.size
    }
    
    /**
     * 모든 타이머 세션 데이터를 삭제합니다.
     * @return 삭제 성공 여부
     */
    suspend fun clearAllData(): Boolean {
        return try {
            timerSessionDao.deleteAllSessions()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 현재 저장된 세션 개수를 조회합니다.
     * @return 총 세션 개수
     */
    suspend fun getTotalSessionCount(): Int {
        return timerSessionDao.getTotalSessionCount()
    }
    
    /**
     * 완료된 세션 개수를 조회합니다.
     * (완전 완료 + 부분 완료)
     * @return 완료된 세션 개수
     */
    suspend fun getCompletedSessionCount(): Int {
        return timerSessionDao.getCompletedSessionCount()
    }

    /**
     * 완전 완료된 세션 개수를 조회합니다.
     * @return 완전 완료된 세션 개수
     */
    suspend fun getFullCompletedSessionCount(): Int {
        return timerSessionDao.getFullCompletedSessionCount()
    }

    /**
     * 부분 완료된 세션 개수를 조회합니다.
     * @return 부분 완료된 세션 개수
     */
    suspend fun getPartialSessionCount(): Int {
        return timerSessionDao.getPartialSessionCount()
    }
}
