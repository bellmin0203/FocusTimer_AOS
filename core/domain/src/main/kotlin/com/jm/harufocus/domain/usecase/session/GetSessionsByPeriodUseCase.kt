package com.jm.harufocus.domain.usecase.session

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.repository.TimerSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

/**
 * 특정 기간의 타이머 세션들을 조회하는 UseCase
 *
 * @param repository TimerSessionRepository 인스턴스
 */
class GetSessionsByPeriodUseCase(
    private val repository: TimerSessionRepository
) {
    /**
     * 특정 기간의 타이머 세션을 조회합니다.
     *
     * @param startTime 조회 시작 시간
     * @param endTime 조회 종료 시간
     * @return 해당 기간의 세션 리스트의 Flow
     * @throws IllegalArgumentException 시작 시간이 종료 시간보다 늦은 경우
     */
    operator fun invoke(startTime: Instant, endTime: Instant): Flow<List<TimerSession>> {
        return flow {
            // 유효성 검증
            if (startTime.isAfter(endTime)) {
                throw IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.")
            }

            // 기간별 세션 조회
            repository.getSessionsBetween(
                startTime.toEpochMilli(),
                endTime.toEpochMilli()
            ).collect { sessions ->
                emit(sessions)
            }
        }
    }
}
