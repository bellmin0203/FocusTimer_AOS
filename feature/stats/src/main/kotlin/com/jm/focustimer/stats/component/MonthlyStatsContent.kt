package com.jm.focustimer.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.model.statistics.MonthlyStats
import com.jm.focustimer.domain.model.statistics.WeeklyFocusTime
import com.jm.focustimer.stats.StatsCard
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.Companion.rounded
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

/**
 * 월간 통계 컨텐츠
 */
@Composable
fun MonthlyStatsContent(
    stats: MonthlyStats,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")

    // 날짜 네비게이션 헤더
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "이전 달"
            )
        }

        Text(
            text = stats.yearMonth.format(dateFormatter),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "다음 달"
            )
        }
    }

    // 요약 카드
    StatsCard(title = "월간 요약") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "총 집중 시간",
                    value = formatDuration(stats.totalFocusTime),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "주 평균 집중 시간",
                    value = formatDuration(stats.averageWeeklyFocusTime),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "가장 집중한 주",
                    value = stats.mostProductiveWeek?.let { "${it}주차" } ?: "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "생산성 트렌드",
                    value = formatTrend(stats.trend),
                    valueColor = getTrendColor(stats.trend),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 주별 집중 시간 차트
    StatsCard(title = "주별 집중 시간") {
        if (stats.totalFocusTime.inWholeMinutes == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "이번 달 집중 기록이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            WeeklyChart(stats = stats)
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun WeeklyChart(stats: MonthlyStats) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val markerBackgroundColor = MaterialTheme.colorScheme.surfaceContainer
    val markerTextColor = MaterialTheme.colorScheme.onSurface

    // 차트 데이터 준비
    LaunchedEffect(stats) {
        val weeks = stats.weeklyBreakdown.map { it.weekOfMonth.toFloat() }
        val focusTimes = stats.weeklyBreakdown.map {
            it.focusTime.inWholeMinutes.toFloat()
        }

        modelProducer.runTransaction {
            columnSeries { series(weeks, focusTimes) }
        }
    }

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = markerTextColor,
            background = rememberShapeComponent(
                fill = fill(markerBackgroundColor)
            ),
            padding = Insets(horizontalDp = 8f, verticalDp = 4f),
        ),
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        valueFormatter = remember {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                targets.filterIsInstance<ColumnCartesianLayerMarkerTarget>()
                    .flatMap { it.columns }
                    .joinToString("\n") { column ->
                        val minutes = column.entry.y.toInt()
                        val hours = minutes / 60
                        val remainingMinutes = minutes % 60
                        val timeText = when {
                            hours > 0 && remainingMinutes > 0 -> "${hours}시간 ${remainingMinutes}분"
                            hours > 0 -> "${hours}시간"
                            remainingMinutes > 0 -> "${remainingMinutes}분"
                            else -> "0분"
                        }
                        timeText
                    }
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(MaterialTheme.colorScheme.primary),
                        thickness = 20.dp,
                        shape = rounded(allPercent = 40)
                    )
                ),
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurfaceVariant),
                guideline = null,
                title = "분",
                titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurfaceVariant),
                guideline = null,
                title = "주",
                titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                valueFormatter = { _, value, _ ->
                    "${value.toInt()}주"
                }
            ),
            marker = marker
        ),
        modelProducer = modelProducer,
        modifier = Modifier.height(250.dp)
    )
}

/**
 * Duration을 "X시간 Y분" 형식으로 변환
 */
private fun formatDuration(duration: kotlin.time.Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)

    return when {
        hours > 0 && minutes > 0 -> "${hours}시간 ${minutes}분"
        hours > 0 -> "${hours}시간"
        minutes > 0 -> "${minutes}분"
        else -> "0분"
    }
}

/**
 * 트렌드를 퍼센트 형식으로 변환
 */
private fun formatTrend(trend: Double): String {
    val percentage = (trend * 100).toInt()
    return when {
        percentage > 0 -> "↗ +${percentage}%"
        percentage < 0 -> "↘ ${percentage}%"
        else -> "→ 0%"
    }
}

@Composable
private fun getTrendColor(trend: Double): Color {
    return when {
        trend > 0 -> Color(0xFF4CAF50) // Green
        trend < 0 -> MaterialTheme.colorScheme.error // Red
        else -> MaterialTheme.colorScheme.onSurface // Default
    }
}

class MonthlyStatsProvider : PreviewParameterProvider<MonthlyStats> {
    override val values = sequenceOf(
        MonthlyStats(
            yearMonth = YearMonth.now(),
            totalFocusTime = 2800.minutes,
            averageSessionsPerWeek = 28.0,
            mostProductiveWeek = 3,
            trend = 0.15,
            weeklyBreakdown = listOf(
                WeeklyFocusTime(
                    weekOfMonth = 1,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 100.minutes,
                    sessionCount = 2
                ),
                WeeklyFocusTime(
                    weekOfMonth = 2,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 400.minutes,
                    sessionCount = 2
                ),
                WeeklyFocusTime(
                    weekOfMonth = 3,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 120.minutes,
                    sessionCount = 2
                ),
                WeeklyFocusTime(
                    weekOfMonth = 4,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 300.minutes,
                    sessionCount = 2
                ),
            )
        )
    )
}

@ThemePreviews
@Composable
private fun MonthlyStatsContentPreview(
    @PreviewParameter(MonthlyStatsProvider::class) stats: MonthlyStats
) {
    FocusTimerTheme {
        MonthlyStatsContent(
            stats = stats,
            onPrevious = {},
            onNext = {}
        )
    }
}
