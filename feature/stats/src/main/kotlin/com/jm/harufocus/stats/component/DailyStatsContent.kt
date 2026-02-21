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
import androidx.compose.foundation.layout.size
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
import com.patrykandpatrick.vico.core.common.shader.toShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.Companion.rounded
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 일일 통계 컨텐츠
 */
@Composable
fun DailyStatsContent(
    stats: DailyStats,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canNavigateNext: Boolean = true,
    showChartTooltip: Boolean = false,
    onDismissChartTooltip: () -> Unit = {}
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

        if (canNavigateNext) {
            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.content_description_next_day)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
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
                TimeBreakdownItem(
                    fullCompletedTime = stats.fullCompletedTime,
                    partialCompletedTime = stats.partialCompletedTime,
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
private fun TimeBreakdownItem(
    fullCompletedTime: Duration,
    partialCompletedTime: Duration,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.session_full_completed_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDuration(fullCompletedTime),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .height(24.dp)
                .padding(horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.session_partial_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDuration(partialCompletedTime),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
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
    val partialPatternFill = remember(partialColor) {
        fill(
            ChartPatterns.createDiagonalStripedPattern(
                baseColor = partialColor.copy(alpha = 0.9f),
                stripeColor = partialColor.copy(alpha = 0.45f)
            ).toShaderProvider()
        )
    }

    // 누적 막대 차트 데이터 준비
    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            columnSeries {
                // 첫 번째 시리즈: 부분 완료 시간 (막대 아래쪽)
                series(
                    x = stats.hourlyBreakdown.map { it.hour.toFloat() },
                    y = stats.hourlyBreakdown.map { it.partialTime.inWholeMinutes.toFloat() }
                )
                // 두 번째 시리즈: 완전 완료 시간 (막대 위쪽에 쌓임)
                series(
                    x = stats.hourlyBreakdown.map { it.hour.toFloat() },
                    y = stats.hourlyBreakdown.map { it.fullCompletedTime.inWholeMinutes.toFloat() }
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

    // 누적 막대용 컬럼 프로바이더 - 접근성을 위한 시각적 구분
    val columnProvider = ColumnCartesianLayer.ColumnProvider.series(
        listOf(
            // 부분 완료: 투명도 + 테두리 (접근성 개선)
            rememberLineComponent(
                fill = partialPatternFill,
                thickness = 12.dp,
                strokeFill = fill(partialColor),
                strokeThickness = 1.dp
            ),
            // 완전 완료: 솔리드 색상 (진한 파랑)
            rememberLineComponent(
                fill = fill(fullCompletedColor),
                thickness = 12.dp,
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
                // 누적/비누적 모드 모두 대응: target 단위가 아니라 column 목록 기준으로 계산
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
        },
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = columnProvider,
                mergeMode = { ColumnCartesianLayer.MergeMode.stacked() }
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
private fun formatDuration(duration: Duration): String {
    return formatDurationKo(duration)
}

class DailyStatsProvider : PreviewParameterProvider<DailyStats> {
    override val values = sequenceOf(
        DailyStats(
            date = LocalDate.now(),
            totalFocusTime = 120.minutes,
            completedSessions = 4,
            fullCompletedTime = 90.minutes,
            partialCompletedTime = 30.minutes,
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
            fullCompletedTime = 0.minutes,
            partialCompletedTime = 0.minutes,
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
                onNext = {},
                canNavigateNext = true
            )
        }
    }
}
