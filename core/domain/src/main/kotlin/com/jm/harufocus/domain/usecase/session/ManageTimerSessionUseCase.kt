package com.jm.harufocus.domain.usecase.session

import com.jm.harufocus.domain.model.session.TimerSession
import com.jm.harufocus.domain.usecase.review.IncrementSessionForReviewUseCase
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

/**
 * 타이머 세션 관리 전용 UseCase
 * 세션 저장 및 업데이트 로직을 캡슐화합니다
 */
class ManageTimerSessionUseCase @Inject constructor(
    private val saveTimerSessionUseCase: SaveTimerSessionUseCase,
    private val updateTimerSessionUseCase: UpdateTimerSessionUseCase,
    private val incrementSessionForReviewUseCase: IncrementSessionForReviewUseCase,
) {
    /**
     * 새로운 타이머 세션을 시작합니다
     */
    suspend fun startSession(
        presetId: Int,
        duration: Duration,
    ): Result<Long> {
        val session = TimerSession(
            presetId = presetId,
            startTime = Instant.now(),
            endTime = null,
            duration = duration,
            completed = false,
            overrunTime = null
        )

        return saveTimerSessionUseCase(session)
    }

    /**
     * 세션을 완료 상태로 업데이트합니다
     */
    suspend fun completeSession(
        sessionId: Long,
        presetId: Int? = null,
        startTime: Instant?,
        initialDuration: Duration,
        overtime: Duration,
    ): Result<Unit> {
        if (startTime == null) {
            return Result.failure(SessionException.MissingStartTime())
        }

        val session = TimerSession(
            id = sessionId,
            presetId = presetId,
            startTime = startTime,
            endTime = Instant.now(),
            duration = initialDuration,
            completed = true,
            overrunTime = if (overtime > Duration.ZERO) overtime else null
        )

        return updateTimerSessionUseCase(session).also { result ->
            // 세션 완료 성공 시 리뷰용 세션 카운트 증가
            if (result.isSuccess) {
                incrementSessionForReviewUseCase()
            }
        }
    }

    /**
     * 타이머 세션을 중간에 중단한 경우, 부분 완료 상태로 저장합니다
     * 실제 경과 시간만큼을 duration으로 저장합니다
     */
    suspend fun savePartialSession(
        sessionId: Long,
        presetId: Int? = null,
        startTime: Instant?,
        elapsedDuration: Duration,
    ): Result<Unit> {
        if (startTime == null) {
            return Result.failure(SessionException.MissingStartTime())
        }

        val session = TimerSession(
            id = sessionId,
            presetId = presetId,
            startTime = startTime,
            endTime = Instant.now(),
            duration = elapsedDuration,
            completed = true, // 부분 완료도 완료로 표시하여 통계에 반영
            overrunTime = null,
            isPartial = true // 부분 완료 여부 표시
        )

        return updateTimerSessionUseCase(session)
    }

    /**
     * 세션을 미완료 상태로 종료합니다 (중도 정지)
     */
    suspend fun stopSession(
        sessionId: Long,
        presetId: Int? = null,
        startTime: Instant?,
        initialDuration: Duration,
    ): Result<Unit> {
        if (startTime == null) {
            return Result.failure(SessionException.MissingStartTime())
        }

        val session = TimerSession(
            id = sessionId,
            presetId = presetId,
            startTime = startTime,
            endTime = Instant.now(),
            duration = initialDuration,
            completed = false,
            overrunTime = null
        )

        return updateTimerSessionUseCase(session)
    }
}
