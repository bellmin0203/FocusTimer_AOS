package com.jm.focustimer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jm.focustimer.domain.model.session.TimerSession
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

/**
 * Room 데이터베이스를 위한 TimerSession 엔티티
 *
 * @param id 세션의 고유 식별자 (Primary key)
 * @param presetId 사용된 프리셋의 ID
 * @param startTime 세션 시작 시간 (타임스탬프로 저장)
 * @param endTime 세션 종료 시간 (nullable - 진행 중인 세션의 경우 null)
 * @param duration 세션 지속 시간 (밀리초로 저장)
 * @param completed 세션 완료 여부
 * @param overrunTime 초과 시간 (nullable - 초과하지 않은 경우 null, 밀리초로 저장)
 */
@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "preset_id")
    val presetId: Int?,

    @ColumnInfo(name = "start_time")
    val startTime: Long, // Instant를 timestamp(millis)로 저장

    @ColumnInfo(name = "end_time")
    val endTime: Long?, // Instant를 timestamp(millis)로 저장

    @ColumnInfo(name = "duration")
    val duration: Long, // Duration을 밀리초로 저장

    @ColumnInfo(name = "completed")
    val completed: Boolean,

    @ColumnInfo(name = "overrun_time")
    val overrunTime: Long? // Duration을 밀리초로 저장
)

/**
 * TimerSessionEntity를 TimerSession으로 변환하는 확장 함수
 */
fun TimerSessionEntity.toTimerSession(): TimerSession {
    return TimerSession(
        id = this.id,
        presetId = this.presetId,
        startTime = Instant.ofEpochMilli(this.startTime),
        endTime = this.endTime?.let { Instant.ofEpochMilli(it) },
        duration = this.duration.milliseconds,
        completed = this.completed,
        overrunTime = this.overrunTime?.milliseconds
    )
}

/**
 * TimerSession을 TimerSessionEntity로 변환하는 확장 함수
 */
fun TimerSession.toTimerSessionEntity(): TimerSessionEntity {
    return TimerSessionEntity(
        id = this.id,
        presetId = this.presetId,
        startTime = this.startTime.toEpochMilli(),
        endTime = this.endTime?.toEpochMilli(),
        duration = this.duration.inWholeMilliseconds,
        completed = this.completed,
        overrunTime = this.overrunTime?.inWholeMilliseconds
    )
}
