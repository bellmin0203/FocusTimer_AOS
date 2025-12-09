package com.jm.focustimer.domain.usecase.session

import com.jm.focustimer.domain.model.session.TimerSession
import com.jm.focustimer.domain.repository.TimerSessionRepository
import javax.inject.Inject

/**
 * 타이머 세션을 업데이트하는 UseCase
 *
 * @param repository TimerSessionRepository 인스턴스
 */
class UpdateTimerSessionUseCase @Inject constructor(
    private val repository: TimerSessionRepository
) {
    /**
     * 타이머 세션을 업데이트합니다.
     *
     * @param session 업데이트할 세션
     * @return 업데이트 결과를 담은 Result
     * @throws IllegalArgumentException 세션이 존재하지 않거나 데이터가 유효하지 않은 경우
     */
    suspend operator fun invoke(session: TimerSession): Result<Unit> {
        return try {
            // 세션 존재 확인
            repository.getSessionById(session.id)
                ?: throw IllegalArgumentException("업데이트할 세션을 찾을 수 없습니다. (ID: ${session.id})")

            // 유효성 검증 (Refactored: 공통 Validator 사용)
            TimerSessionValidator.validate(session)

            // 세션 업데이트
            repository.updateSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
