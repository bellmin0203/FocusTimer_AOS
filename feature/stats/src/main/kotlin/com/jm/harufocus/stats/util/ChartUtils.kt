package com.jm.harufocus.stats.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jm.harufocus.stats.R
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Duration을 "Xh Ym" 형식으로 변환 (영어, 마커용)
 */
fun formatDurationShort(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}

/**
 * Duration을 "X시간 Y분" 또는 "X분" 형식으로 변환 (한국어)
 */
@Composable
fun formatDurationKo(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)

    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.format_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(R.string.format_hours, hours)
        minutes > 0 -> stringResource(R.string.format_minutes, minutes)
        else -> stringResource(R.string.format_zero_minutes)
    }
}

/**
 * 총 시간, 완전 완료 시간, 부분 완료 시간을 포맷팅하여 상세 마커 텍스트 생성
 *
 * @param totalMinutes 총 시간(분)
 * @param fullCompletedMinutes 완전 완료 시간(분)
 * @param partialMinutes 부분 완료 시간(분)
 * @return 포맷팅된 마커 텍스트 (다중 라인)
 */
fun formatDetailedMarker(
    context: Context,
    totalMinutes: Int,
    fullCompletedMinutes: Int,
    partialMinutes: Int
): String {
    val totalStr = formatDurationShort(totalMinutes.minutes)
    val fullStr = formatDurationShort(fullCompletedMinutes.minutes)
    val partialStr = formatDurationShort(partialMinutes.minutes)

    return """${context.getString(R.string.marker_total, totalStr)}
${context.getString(R.string.marker_full, fullStr)}
${context.getString(R.string.marker_partial, partialStr)}""".trimIndent()
}
