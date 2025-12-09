package com.jm.focustimer.domain.usecase.session

import com.jm.focustimer.domain.model.session.TimerSession
import com.jm.focustimer.domain.repository.TimerSessionRepository
import javax.inject.Inject

/**
 * 타이머 세션을 저장하는 UseCase
 *
 * @param repository TimerSessionRepository 인스턴스
 */
class SaveTimerSessionUseCase @Inject constructor(
    private val repository: TimerSessionRepository
) {
    /**
     * 타이머 세션을 저장합니다.
     *
     * @param session 저장할 세션
     * @return 저장된 세션의 ID를 담은 Result
     * @throws IllegalArgumentException 세션 데이터가 유효하지 않은 경우
     */
    suspend operator fun invoke(session: TimerSession): Result<Long> {
        return try {
            // 유효성 검증 (Refactored: 공통 Validator 사용)
            TimerSessionValidator.validate(session)

            // 세션 저장
            val sessionId = repository.insertSession(session)
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
