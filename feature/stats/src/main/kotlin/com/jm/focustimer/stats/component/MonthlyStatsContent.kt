package com.jm.focustimer.stats.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.model.MonthlyStats
import com.jm.focustimer.domain.model.WeeklyFocusTime
import com.jm.focustimer.stats.StatsCard
import com.jm.focustimer.stats.StatsRow
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape.Companion.rounded
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

/**
 * 월간 통계 컨텐츠
 */
@Composable
fun MonthlyStatsContent(stats: MonthlyStats) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월")

    // 기간 표시
    Text(
        text = stats.yearMonth.format(dateFormatter),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    // 요약 카드
    StatsCard(title = "월간 요약") {
        StatsRow(
            label = "총 집중 시간",
            value = formatDuration(stats.totalFocusTime)
        )
        StatsRow(
            label = "완료된 세션",
            value = "${stats.totalSessions}개"
        )
        StatsRow(
            label = "주평균 세션",
            value = String.format("%.1f개", stats.averageSessionsPerWeek)
        )
        StatsRow(
            label = "주평균 집중 시간",
            value = formatDuration(stats.averageWeeklyFocusTime)
        )
        stats.mostProductiveWeek?.let { week ->
            StatsRow(
                label = "가장 생산적인 주",
                value = "${week}주차"
            )
        }
        StatsRow(
            label = "생산성 트렌드",
            value = formatTrend(stats.trend)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 주별 집중 시간 차트
    StatsCard(title = "주별 집중 시간") {
        WeeklyChart(stats = stats)
    }
}

@Composable
private fun WeeklyChart(stats: MonthlyStats) {
    val modelProducer = remember { CartesianChartModelProducer() }

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

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(Color(0xFFFF6B6B)),
                        thickness = 20.dp,
                        shape = rounded(allPercent = 40)
                    )
                ),
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(),
                title = "분",
                titleComponent = rememberTextComponent()
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberTextComponent(),
                title = "주",
                titleComponent = rememberTextComponent(),
                valueFormatter = { _, value, _ ->
                    "${value.toInt()}주"
                }
            )
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
        MonthlyStatsContent(stats = stats)
    }
}
