package com.jm.focustimer.domain.usecase.session

import com.jm.focustimer.domain.model.session.TimerSession
import com.jm.focustimer.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.Flow

/**
 * 완료된 타이머 세션들을 조회하는 UseCase
 *
 * @param repository TimerSessionRepository 인스턴스
 */
class GetCompletedSessionsUseCase(
    private val repository: TimerSessionRepository
) {
    /**
     * 완료된 모든 타이머 세션을 조회합니다.
     *
     * @return 완료된 세션 리스트의 Flow
     */
    operator fun invoke(): Flow<List<TimerSession>> {
        return repository.getCompletedSessions()
    }
}
