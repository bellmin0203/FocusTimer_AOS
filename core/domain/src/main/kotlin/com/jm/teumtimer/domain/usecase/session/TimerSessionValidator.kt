package com.jm.teumtimer.domain.usecase.session

import com.jm.teumtimer.domain.model.session.TimerSession

/**
 * 타이머 세션의 유효성을 검증하는 유틸리티 클래스
 *
 * TDD Refactor 단계 - 중복 코드 제거 및 재사용성 향상
 */
internal object TimerSessionValidator {

    /**
     * 세션의 유효성을 검증합니다.
     *
     * @param session 검증할 세션
     * @throws SessionException 유효하지 않은 세션인 경우
     */
    fun validate(session: TimerSession) {
        validateDuration(session.duration.inWholeMilliseconds)
        validateTimeRange(session.startTime.toEpochMilli(), session.endTime?.toEpochMilli())
    }

    /**
     * 지속 시간의 유효성을 검증합니다.
     *
     * @param durationMillis 지속 시간 (밀리초)
     * @throws SessionException.InvalidDuration 지속 시간이 0 이하인 경우
     */
    private fun validateDuration(durationMillis: Long) {
        if (durationMillis <= 0) {
            throw SessionException.InvalidDuration()
        }
    }

    /**
     * 시작/종료 시간의 유효성을 검증합니다.
     *
     * @param startTimeMillis 시작 시간 (밀리초)
     * @param endTimeMillis 종료 시간 (밀리초, nullable)
     * @throws SessionException.InvalidTimeRange 종료 시간이 시작 시간보다 이른 경우
     */
    private fun validateTimeRange(startTimeMillis: Long, endTimeMillis: Long?) {
        endTimeMillis?.let { endTime ->
            if (endTime < startTimeMillis) {
                throw SessionException.InvalidTimeRange()
            }
        }
    }
}
