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
import com.jm.focustimer.domain.model.DailyStats
import com.jm.focustimer.domain.model.HourlyStats
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

/**
 * 일일 통계 컨텐츠
 */
@Composable
fun DailyStatsContent(stats: DailyStats) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)")

    // 날짜 표시
    Text(
        text = stats.date.format(dateFormatter),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    // 요약 카드
    StatsCard(title = "하루 요약") {
        StatsRow(
            label = "총 집중 시간",
            value = formatDuration(stats.totalFocusTime)
        )
        StatsRow(
            label = "완료된 세션",
            value = "${stats.completedSessions}개"
        )
        StatsRow(
            label = "평균 세션 길이",
            value = formatDuration(stats.averageSessionLength)
        )
        stats.mostProductiveHour?.let { hour ->
            StatsRow(
                label = "가장 생산적인 시간",
                value = "${hour}시"
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 시간대별 집중 시간 차트
    StatsCard(title = "24시간 집중 시간") {
        HourlyChart(stats = stats)
    }
}

@Composable
private fun HourlyChart(stats: DailyStats) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(stats) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = stats.hourlyBreakdown.map { it.hour.toFloat() },
                    y = stats.hourlyBreakdown.map { it.focusTime.inWholeMinutes.toFloat() }
                )
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(Color(0xFF6200EE)),
                        thickness = 8.dp,
                        shape = rounded(allPercent = 40)
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                label = rememberTextComponent(),
                title = "분",
                titleComponent = rememberTextComponent()
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberTextComponent(),
                title = "시간",
                titleComponent = rememberTextComponent()
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

class DailyStatsProvider : PreviewParameterProvider<DailyStats> {
    override val values = sequenceOf(
        DailyStats(
            date = LocalDate.now(),
            totalFocusTime = 120.minutes,
            completedSessions = 4,
            mostProductiveHour = 14,
            hourlyBreakdown = (0..23).map { hour ->
                HourlyStats(
                    hour = hour,
                    focusTime = (hour % 5).minutes,
                    sessionCount = hour * 2
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
    FocusTimerTheme {
        DailyStatsContent(stats = stats)
    }
}
