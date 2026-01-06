package com.jm.teumtimer.domain.usecase.session

import com.jm.teumtimer.domain.repository.TimerSessionRepository
import java.time.Instant

/**
 * 특정 기간의 총 집중 시간을 조회하는 UseCase
 *
 * @param repository TimerSessionRepository 인스턴스
 */
class GetTotalFocusTimeUseCase(
    private val repository: TimerSessionRepository
) {
    /**
     * 특정 기간의 총 집중 시간을 조회합니다.
     *
     * @param startTime 조회 시작 시간
     * @param endTime 조회 종료 시간
     * @return 총 집중 시간 (밀리초)
     * @throws IllegalArgumentException 시작 시간이 종료 시간보다 늦은 경우
     */
    suspend operator fun invoke(startTime: Instant, endTime: Instant): Long {
        // 유효성 검증
        if (startTime.isAfter(endTime)) {
            throw IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.")
        }

        // 총 집중 시간 조회
        return repository.getTotalFocusTimeBetween(
            startTime.toEpochMilli(),
            endTime.toEpochMilli()
        )
    }
}
