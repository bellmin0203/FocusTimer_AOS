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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.domain.model.statistics.DailyStats
import com.jm.harufocus.domain.model.statistics.HourlyStats
import com.jm.harufocus.stats.R
import com.jm.harufocus.stats.StatsCard
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.Companion.rounded
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

/**
 * 일일 통계 컨텐츠
 */
@Composable
fun DailyStatsContent(
    stats: DailyStats,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val locale = Locale.getDefault()
    val pattern = if (locale.language == "ko") {
        "yyyy년 M월 d일 (E)"
    } else {
        "EEE, MMM d, yyyy"
    }
    val dateFormatter = DateTimeFormatter.ofPattern(pattern, locale)

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
                contentDescription = stringResource(R.string.content_description_prev_day)
            )
        }

        Text(
            text = stats.date.format(dateFormatter),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.content_description_next_day)
            )
        }
    }

    // 요약 카드 (Grid Layout)
    StatsCard(title = stringResource(R.string.daily_summary_title)) {
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
                    label = stringResource(R.string.daily_most_productive_hour),
                    value = stats.mostProductiveHour?.let { stringResource(R.string.format_time, it) } ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
            // 세션 정보 표시 (완전 완료 / 부분 완료)
            if (stats.completedSessions > 0) {
                SessionBreakdownItem(
                    fullCompleted = stats.fullCompletedSessions,
                    partial = stats.partialSessions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // 시간대별 집중 시간 차트
    StatsCard(title = stringResource(R.string.daily_chart_title)) {
        if (stats.totalFocusTime.inWholeMilliseconds == 0L) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.daily_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column {
                HourlyChart(stats = stats)
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
    modifier: Modifier = Modifier
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
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HourlyChart(stats: DailyStats) {
    val context = LocalContext.current
    val modelProducer = remember { CartesianChartModelProducer() }

    // 누적 막대 차트 색상 - 완전 완료(Primary), 부분 완료(Secondary)
    val fullCompletedColor = MaterialTheme.colorScheme.primary
    val partialColor = MaterialTheme.colorScheme.secondary
    val markerBackgroundColor = MaterialTheme.colorScheme.primaryContainer
    val markerTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    // 누적 막대 차트 데이터 준비
    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            columnSeries {
                // 첫 번째 시리즈: 완전 완료 시간 (막대 아래쪽)
                series(
                    x = stats.hourlyBreakdown.map { it.hour.toFloat() },
                    y = stats.hourlyBreakdown.map { it.fullCompletedTime.inWholeMinutes.toFloat() }
                )
                // 두 번째 시리즈: 부분 완료 시간 (막대 위쪽에 쌓임)
                series(
                    x = stats.hourlyBreakdown.map { it.hour.toFloat() },
                    y = stats.hourlyBreakdown.map { it.partialTime.inWholeMinutes.toFloat() }
                )
            }
        }
    }

    val bottomAxisValueFormatter = remember {
        CartesianValueFormatter { _, x, _ ->
            context.getString(R.string.format_time, x.toInt())
        }
    }

    // 현재 시간 기준으로 초기 스크롤 위치 설정
    val currentHour = remember { LocalTime.now().hour }
    val scrollAdjustment = 4
    val initialScroll = remember { Scroll.Absolute.x(currentHour - scrollAdjustment.toDouble()) }

    val zoomState = rememberVicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.fixed(1.2f)
    )
    val scrollState = rememberVicoScrollState(initialScroll = initialScroll)

    // 누적 막대용 컬럼 프로바이더 - 두 가지 색상 제공
    val columnProvider = ColumnCartesianLayer.ColumnProvider.series(
        listOf(
            rememberLineComponent(
                fill = fill(fullCompletedColor),
                thickness = 12.dp,
                shape = rounded(allPercent = 40)
            ),
            rememberLineComponent(
                fill = fill(partialColor),
                thickness = 12.dp,
                shape = rounded(allPercent = 40)
            )
        )
    )

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
                            hours > 0 && remainingMinutes > 0 -> context.getString(R.string.format_hours_minutes, hours, remainingMinutes)
                            hours > 0 -> context.getString(R.string.format_hours, hours)
                            remainingMinutes > 0 -> context.getString(R.string.format_minutes, remainingMinutes)
                            else -> context.getString(R.string.format_zero_minutes)
                        }
                        timeText
                    }
            }
        }
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = columnProvider
            ),
            startAxis = VerticalAxis.rememberStart(
                guideline = null,
                line = null,
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textSize = 10.sp
                ),
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                valueFormatter = bottomAxisValueFormatter,
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textSize = 10.sp
                ),
                line = rememberLineComponent(
                    fill = fill(MaterialTheme.colorScheme.outlineVariant),
                    thickness = 1.dp
                ),
            ),
            marker = marker
        ),
        modelProducer = modelProducer,
        zoomState = zoomState,
        scrollState = scrollState,
        modifier = Modifier
            .height(250.dp)
            .padding(8.dp)
    )
}

/**
 * Duration을 "X시간 Y분" 형식으로 변환
 */
@Composable
private fun formatDuration(duration: kotlin.time.Duration): String {
    val hours = duration.inWholeHours
    val minutes = (duration.inWholeMinutes % 60)

    return when {
        hours > 0 && minutes > 0 -> stringResource(R.string.format_hours_minutes, hours, minutes)
        hours > 0 -> stringResource(R.string.format_hours, hours)
        minutes > 0 -> stringResource(R.string.format_minutes, minutes)
        else -> stringResource(R.string.format_zero_minutes)
    }
}

class DailyStatsProvider : PreviewParameterProvider<DailyStats> {
    override val values = sequenceOf(
        DailyStats(
            date = LocalDate.now(),
            totalFocusTime = 120.minutes,
            completedSessions = 4,
            fullCompletedSessions = 3,
            partialSessions = 1,
            mostProductiveHour = 14,
            hourlyBreakdown = (0..23).map { hour ->
                val isPeakHour = hour in 13..15
                val isNormalHour = hour in 9..11
                val hasFullComplete = isPeakHour || isNormalHour
                val hasPartial = isPeakHour // 13-15시에만 부분 완료 추가
                HourlyStats(
                    hour = hour,
                    focusTime = if (isPeakHour) 45.minutes else if (isNormalHour) 15.minutes else 0.minutes,
                    fullCompletedTime = if (hasFullComplete) 30.minutes else 0.minutes,
                    partialTime = if (hasPartial) 15.minutes else 0.minutes,
                    sessionCount = if (isPeakHour) 2 else if (isNormalHour) 1 else 0,
                    fullCompletedCount = if (hasFullComplete) 1 else 0,
                    partialCount = if (hasPartial) 1 else 0
                )
            }
        ),
        DailyStats(
            date = LocalDate.now().minusDays(1),
            totalFocusTime = 0.minutes,
            completedSessions = 0,
            fullCompletedSessions = 0,
            partialSessions = 0,
            mostProductiveHour = null,
            hourlyBreakdown = (0..23).map { hour ->
                HourlyStats(
                    hour = hour,
                    focusTime = 0.minutes,
                    fullCompletedTime = 0.minutes,
                    partialTime = 0.minutes,
                    sessionCount = 0,
                    fullCompletedCount = 0,
                    partialCount = 0
                )
            }
        )
    )
}

@ThemePreviews
@Composable
private fun DailyStatsContentPreview(
    @PreviewParameter(DailyStatsProvider::class) stats: DailyStats
) {
    HaruFocusTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        ) {
            DailyStatsContent(
                stats = stats,
                onPrevious = {},
                onNext = {}
            )
        }
    }
}