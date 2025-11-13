package com.jm.focustimer.ui.util

import com.jm.focustimer.domain.model.Preset
import com.jm.focustimer.ui.component.ChartData
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Compose Preview용 샘플 데이터 제공자
 */
object PreviewProvider {
    /**
     * 샘플 Preset 데이터
     */
    val samplePresets = listOf(
        Preset(
            id = 1,
            name = "Focus",
            duration = 25.minutes,
            colorIndex = 0,
            createdAt = Instant.now()
        ),
        Preset(
            id = 2,
            name = "Short Break",
            duration = 5.minutes,
            colorIndex = 1,
            createdAt = Instant.now()
        ),
        Preset(
            id = 3,
            name = "Long Break",
            duration = 15.minutes,
            colorIndex = 2,
            createdAt = Instant.now()
        )
    )

    /**
     * 샘플 차트 데이터 (주간)
     */
    val sampleWeeklyChartData = listOf(
        ChartData(label = "Mon", value = 0.6f, isSelected = false),
        ChartData(label = "Tue", value = 0.75f, isSelected = false),
        ChartData(label = "Wed", value = 1.0f, isSelected = true),
        ChartData(label = "Thu", value = 0.5f, isSelected = false),
        ChartData(label = "Fri", value = 0.8f, isSelected = false),
        ChartData(label = "Sat", value = 0.3f, isSelected = false),
        ChartData(label = "Sun", value = 0.9f, isSelected = false)
    )

    /**
     * 샘플 통계 데이터
     */
    val sampleStatistics = mapOf(
        "totalFocusTime" to "4h 30m",
        "sessionCount" to "6",
        "successRate" to "83%",
        "averageSessionTime" to "45m"
    )

    /**
     * 샘플 세션 데이터
     */
    data class SampleSession(
        val id: Int,
        val type: String, // "Focus" or "Break"
        val duration: String,
        val time: String
    )

    val sampleSessions = listOf(
        SampleSession(
            id = 1,
            type = "Focus",
            duration = "1h 30m",
            time = "9:30 AM"
        ),
        SampleSession(
            id = 2,
            type = "Break",
            duration = "15m",
            time = "11:00 AM"
        ),
        SampleSession(
            id = 3,
            type = "Focus",
            duration = "1h",
            time = "11:15 AM"
        ),
        SampleSession(
            id = 4,
            type = "Break",
            duration = "10m",
            time = "12:15 PM"
        ),
        SampleSession(
            id = 5,
            type = "Focus",
            duration = "45m",
            time = "12:25 PM"
        ),
        SampleSession(
            id = 6,
            type = "Break",
            duration = "5m",
            time = "1:10 PM"
        )
    )
}
