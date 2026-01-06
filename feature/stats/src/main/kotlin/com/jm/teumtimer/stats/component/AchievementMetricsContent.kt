package com.jm.teumtimer.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jm.teumtimer.designsystem.component.ThemePreviews
import com.jm.teumtimer.designsystem.theme.FocusTimerTheme
import com.jm.teumtimer.domain.model.statistics.AchievementMetrics
import com.jm.teumtimer.stats.R
import com.jm.teumtimer.stats.StatsCard
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 성취 지표 컨텐츠
 */
@Composable
fun AchievementMetricsContent(metrics: AchievementMetrics) {
    Column {
        Text(
            text = stringResource(R.string.achievement_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 성취 지표 그리드 (메인 하이라이트)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.achievement_focus_rate),
                    value = stringResource(R.string.format_percent, metrics.focusRatePercent),
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.achievement_consecutive_days),
                    value = stringResource(R.string.format_days, metrics.consecutiveFocusDays),
                    icon = Icons.Default.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = stringResource(R.string.achievement_longest_time),
                    value = formatDuration(metrics.longestFocusTime),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = stringResource(R.string.achievement_average_session),
                    value = formatDuration(metrics.averageSessionLength),
                    icon = Icons.Default.AccessTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 상세 통계 (2x2 Grid로 변경)
        StatsCard(title = stringResource(R.string.achievement_detail_title)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        label = stringResource(R.string.achievement_total_sessions),
                        value = stringResource(R.string.format_count, metrics.totalSessions),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        label = stringResource(R.string.achievement_total_time),
                        value = formatDuration(metrics.totalFocusTime),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        label = stringResource(R.string.achievement_completed_sessions),
                        value = stringResource(R.string.format_count, metrics.completedSessions),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        label = stringResource(R.string.achievement_incomplete_sessions),
                        value = stringResource(R.string.format_count, metrics.incompleteSessions),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 메인 성취 지표 카드 (아이콘 포함)
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * 상세 통계 아이템 (텍스트 기반)
 */
@Composable
private fun DetailStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Duration을 "Xh Ym" 형식으로 변환
 */
@Composable
private fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)

    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.format_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(R.string.format_hours, hours)
        minutes > 0 -> stringResource(R.string.format_minutes, minutes)
        else -> stringResource(R.string.format_zero_minutes)
    }
}

@ThemePreviews
@Composable
private fun AchievementMetricsContentPreview() {
    val sampleMetrics = AchievementMetrics(
        focusRate = 0.3,
        consecutiveFocusDays = 2,
        longestFocusTime = 40.minutes,
        averageSessionLength = 24.minutes,
        totalSessions = 12,
        completedSessions = 8,
        totalFocusTime = 367.minutes
    )
    FocusTimerTheme {
        AchievementMetricsContent(metrics = sampleMetrics)
    }
}