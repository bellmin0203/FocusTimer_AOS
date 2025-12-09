package com.jm.focustimer.domain.usecase.session

import com.jm.focustimer.domain.repository.TimerSessionRepository

/**
 * 타이머 세션을 삭제하는 UseCase
 *
 * @param repository TimerSessionRepository 인스턴스
 */
class DeleteTimerSessionUseCase(
    private val repository: TimerSessionRepository
) {
    /**
     * ID로 타이머 세션을 삭제합니다.
     *
     * @param sessionId 삭제할 세션 ID
     * @return 삭제 결과를 담은 Result
     * @throws IllegalArgumentException 유효하지 않은 ID이거나 세션을 찾을 수 없는 경우
     */
    suspend operator fun invoke(sessionId: Int): Result<Unit> {
        return try {
            // ID 유효성 검증
            if (sessionId < 0) {
                throw IllegalArgumentException("유효하지 않은 세션 ID입니다.")
            }

            // 세션 존재 확인
            val session = repository.getSessionById(sessionId)
                ?: throw IllegalArgumentException("세션을 찾을 수 없습니다. (ID: $sessionId)")

            // 세션 삭제
            repository.deleteSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
