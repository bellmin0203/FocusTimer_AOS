package com.jm.focustimer.domain.usecase.session

import com.jm.focustimer.domain.model.session.TimerSession
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
        sessionId: Int,
        presetId: Int? = null,
        startTime: Instant?,
        initialDuration: Duration,
        overtime: Duration,
    ): Result<Unit> {
        if (startTime == null) {
            return Result.failure(IllegalStateException("세션 시작 시간을 찾을 수 없습니다"))
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

        return updateTimerSessionUseCase(session)
    }

    /**
     * 세션을 미완료 상태로 종료합니다 (중도 정지)
     */
    suspend fun stopSession(
        sessionId: Int,
        presetId: Int? = null,
        startTime: Instant?,
        initialDuration: Duration,
    ): Result<Unit> {
        if (startTime == null) {
            return Result.failure(IllegalStateException("세션 시작 시간을 찾을 수 없습니다"))
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