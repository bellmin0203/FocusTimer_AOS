package com.jm.focustimer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jm.focustimer.domain.model.statistics.Statistics
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

/**
 * Room 데이터베이스를 위한 Statistics 엔티티
 *
 * @param id 통계의 고유 식별자 (Primary key)
 * @param date 통계 날짜 (epoch day로 저장)
 * @param totalFocusTime 총 집중 시간 (밀리초로 저장)
 * @param completedSessions 완료된 세션 수
 * @param focusRate 집중 달성률 (0.0 ~ 1.0)
 * @param longestFocusTime 가장 긴 집중 시간 (밀리초로 저장)
 * @param averageSessionLength 평균 세션 길이 (밀리초로 저장)
 */
@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "date")
    val date: Long, // LocalDate를 epoch day로 저장

    @ColumnInfo(name = "total_focus_time")
    val totalFocusTime: Long, // Duration을 밀리초로 저장

    @ColumnInfo(name = "completed_sessions")
    val completedSessions: Int,

    @ColumnInfo(name = "focus_rate")
    val focusRate: Double,

    @ColumnInfo(name = "longest_focus_time")
    val longestFocusTime: Long, // Duration을 밀리초로 저장

    @ColumnInfo(name = "average_session_length")
    val averageSessionLength: Long // Duration을 밀리초로 저장
)

/**
 * StatisticsEntity를 Statistics로 변환하는 확장 함수
 */
fun StatisticsEntity.toStatistics(): Statistics {
    return Statistics(
        id = this.id,
        date = LocalDate.ofEpochDay(this.date),
        totalFocusTime = this.totalFocusTime.milliseconds,
        completedSessions = this.completedSessions,
        focusRate = this.focusRate,
        longestFocusTime = this.longestFocusTime.milliseconds,
        averageSessionLength = this.averageSessionLength.milliseconds
    )
}

/**
 * Statistics를 StatisticsEntity로 변환하는 확장 함수
 */
fun Statistics.toStatisticsEntity(): StatisticsEntity {
    return StatisticsEntity(
        id = this.id,
        date = this.date.toEpochDay(),
        totalFocusTime = this.totalFocusTime.inWholeMilliseconds,
        completedSessions = this.completedSessions,
        focusRate = this.focusRate,
        longestFocusTime = this.longestFocusTime.inWholeMilliseconds,
        averageSessionLength = this.averageSessionLength.inWholeMilliseconds
    )
}
