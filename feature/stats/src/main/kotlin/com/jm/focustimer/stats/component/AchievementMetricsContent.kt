package com.jm.focustimer.stats.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.model.statistics.AchievementMetrics
import com.jm.focustimer.stats.StatsCard
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 성취 지표 컨텐츠
 */
@Composable
fun AchievementMetricsContent(metrics: AchievementMetrics) {
    Column {
        Text(
            text = "성취 지표",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 성취 지표 그리드
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "집중률",
                value = "${metrics.focusRatePercent}%",
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "연속 일수",
                value = "${metrics.consecutiveFocusDays}일",
                icon = Icons.Default.CalendarToday,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "최장 시간",
                value = formatDuration(metrics.longestFocusTime),
                icon = Icons.Default.Timer,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "평균 세션",
                value = formatDuration(metrics.averageSessionLength),
                icon = Icons.Default.AccessTime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 상세 통계
        StatsCard(title = "상세 통계") {
            MetricRow(
                label = "전체 세션",
                value = "${metrics.totalSessions}개"
            )
            MetricRow(
                label = "완료된 세션",
                value = "${metrics.completedSessions}개"
            )
            MetricRow(
                label = "미완료 세션",
                value = "${metrics.incompleteSessions}개"
            )
            MetricRow(
                label = "총 집중 시간",
                value = formatDuration(metrics.totalFocusTime)
            )
        }
    }
}

/**
 * 성취 지표 카드
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 지표 행
 */
@Composable
private fun MetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Duration을 "X시간 Y분" 형식으로 변환
 */
private fun formatDuration(duration: Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
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
