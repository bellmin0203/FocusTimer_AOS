package com.jm.harufocus.stats.component

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.domain.model.statistics.MonthlyStats
import com.jm.harufocus.domain.model.statistics.WeeklyFocusTime
import com.jm.harufocus.stats.R
import com.jm.harufocus.stats.StatsCard
import com.jm.harufocus.stats.util.ChartPatterns
import com.jm.harufocus.stats.util.formatDetailedMarker
import com.jm.harufocus.stats.util.formatDurationKo
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.stacked
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
import com.patrykandpatrick.vico.core.common.shader.toShaderProvider
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
                contentDescription = stringResource(R.string.content_description_prev_month)
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
                contentDescription = stringResource(R.string.content_description_next_month)
            )
        }
    }

    // 요약 카드
    StatsCard(title = stringResource(R.string.monthly_summary_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = stringResource(R.string.achievement_total_time),
                    value = formatDuration(stats.totalFocusTime),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.monthly_average_weekly),
                    value = formatDuration(stats.averageWeeklyFocusTime),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = stringResource(R.string.monthly_most_productive_week),
                    value = stats.mostProductiveWeek?.let { stringResource(R.string.format_week_order, it) } ?: "-",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = stringResource(R.string.monthly_growth_rate),
                    value = formatGrowthRate(stats.growthRate),
                    valueColor = getGrowthRateColor(stats.growthRate),
                    modifier = Modifier.weight(1f)
                )
            }
            // 세션 정보 표시 (완전 완료 / 부분 완료)
            if (stats.totalSessions > 0) {
                SessionTimeBreakdownItem(
                    fullCompletedTime = stats.totalFullCompletedTime,
                    partialCompletedTime = stats.totalPartialTime,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 주별 집중 시간 차트
    StatsCard(title = stringResource(R.string.monthly_chart_title)) {
        if (stats.totalFocusTime.inWholeMinutes == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.monthly_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column {
                WeeklyChart(stats = stats)
                ChartLegend(
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
    val context = LocalContext.current
    val modelProducer = remember { CartesianChartModelProducer() }

    // 누적 막대 차트 색상 - 완전 완료(Primary), 부분 완료(Secondary)
    val fullCompletedColor = MaterialTheme.colorScheme.primary
    val partialColor = MaterialTheme.colorScheme.secondary
    val markerBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    val markerTextColor = MaterialTheme.colorScheme.onPrimaryContainer
    val partialPatternFill = remember(partialColor) {
        fill(
            ChartPatterns.createDiagonalStripedPattern(
                baseColor = partialColor
            ).toShaderProvider()
        )
    }

    // 누적 막대 차트 데이터 준비
    LaunchedEffect(stats) {
        val weeks = stats.weeklyBreakdown.map { it.weekOfMonth.toFloat() }

        modelProducer.runTransaction {
            columnSeries {
                // 첫 번째 시리즈: 부분 완료 시간 (막대 아래쪽)
                series(
                    x = weeks,
                    y = stats.weeklyBreakdown.map { it.partialTime.inWholeMinutes.toFloat() }
                )
                // 두 번째 시리즈: 완전 완료 시간 (막대 위쪽에 쌓임)
                series(
                    x = weeks,
                    y = stats.weeklyBreakdown.map { it.fullCompletedTime.inWholeMinutes.toFloat() }
                )
            }
        }
    }

    // 누적 막대용 컬럼 프로바이더 - 접근성을 위한 시각적 구분
    val columnProvider = ColumnCartesianLayer.ColumnProvider.series(
        listOf(
            // 부분 완료: 투명도 적용 (접근성 개선)
            rememberLineComponent(
                fill = partialPatternFill,
                thickness = 20.dp,
                strokeFill = fill(partialColor),
                strokeThickness = 1.dp
            ),
            // 완전 완료: 솔리드 색상 (진한 파랑)
            rememberLineComponent(
                fill = fill(fullCompletedColor),
                thickness = 20.dp,
                shape = rounded(topLeftPercent = 40, topRightPercent = 40)
            )
        )
    )

    // 상세 마커 - 완전/부분 완료 시간 모두 표시
    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = markerTextColor,
            textSize = 11.sp,
            lineCount = 3,
            background = rememberShapeComponent(
                fill = fill(markerBackgroundColor)
            ),
            padding = Insets(horizontalDp = 10f, verticalDp = 6f),
        ),
        labelPosition = DefaultCartesianMarker.LabelPosition.AroundPoint,
        valueFormatter = remember(stats, context) {
            DefaultCartesianMarker.ValueFormatter { _, targets ->
                val columns = targets
                    .filterIsInstance<ColumnCartesianLayerMarkerTarget>()
                    .flatMap { it.columns }

                // 누적 순서: 0=부분 완료(아래), 1=완전 완료(위)
                val partialMinutes = columns.getOrNull(0)?.entry?.y?.toInt() ?: 0
                val fullCompletedMinutes = columns.getOrNull(1)?.entry?.y?.toInt() ?: 0
                val totalMinutes = fullCompletedMinutes + partialMinutes

                formatDetailedMarker(
                    context = context,
                    totalMinutes = totalMinutes,
                    fullCompletedMinutes = fullCompletedMinutes,
                    partialMinutes = partialMinutes
                )
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = columnProvider,
                mergeMode = { ColumnCartesianLayer.MergeMode.stacked() }
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurfaceVariant),
                guideline = null,
                title = stringResource(R.string.format_minutes, 0).replace("0", ""), // "분"
                titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurfaceVariant),
                guideline = null,
                title = stringResource(R.string.format_week, 0).replace("0", ""), // "주"
                titleComponent = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface),
                valueFormatter = { _, value, _ ->
                    context.getString(R.string.format_week, value.toInt())
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
@Composable
private fun formatDuration(duration: kotlin.time.Duration): String {
    return formatDurationKo(duration)
}

/**
 * 월 집중 시간 증감률을 퍼센트 형식으로 변환
 */
@Composable
private fun formatGrowthRate(growthRate: Double): String {
    val percentage = (growthRate * 100).toInt()
    return when {
        percentage > 0 -> "↗ +${percentage}%"
        percentage < 0 -> "↘ ${percentage}%"
        else -> "→ 0%"
    }
}

@Composable
private fun getGrowthRateColor(growthRate: Double): Color {
    return when {
        growthRate > 0 -> Color(0xFF4CAF50) // Green
        growthRate < 0 -> MaterialTheme.colorScheme.error // Red
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
            growthRate = 0.15,
            weeklyBreakdown = listOf(
                WeeklyFocusTime(
                    weekOfMonth = 1,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 100.minutes,
                    fullCompletedTime = 80.minutes,
                    partialTime = 20.minutes,
                    sessionCount = 2,
                    fullCompletedSessionCount = 1,
                    partialSessionCount = 1
                ),
                WeeklyFocusTime(
                    weekOfMonth = 2,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 400.minutes,
                    fullCompletedTime = 350.minutes,
                    partialTime = 50.minutes,
                    sessionCount = 6,
                    fullCompletedSessionCount = 5,
                    partialSessionCount = 1
                ),
                WeeklyFocusTime(
                    weekOfMonth = 3,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 120.minutes,
                    fullCompletedTime = 90.minutes,
                    partialTime = 30.minutes,
                    sessionCount = 2,
                    fullCompletedSessionCount = 1,
                    partialSessionCount = 1
                ),
                WeeklyFocusTime(
                    weekOfMonth = 4,
                    weekStartDate = YearMonth.now().atDay(1),
                    weekEndDate = YearMonth.now().atDay(7),
                    focusTime = 300.minutes,
                    fullCompletedTime = 250.minutes,
                    partialTime = 50.minutes,
                    sessionCount = 5,
                    fullCompletedSessionCount = 4,
                    partialSessionCount = 1
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
    HaruFocusTheme {
        Column {
            MonthlyStatsContent(
                stats = stats,
                onPrevious = {},
                onNext = {}
            )
        }
    }
}
