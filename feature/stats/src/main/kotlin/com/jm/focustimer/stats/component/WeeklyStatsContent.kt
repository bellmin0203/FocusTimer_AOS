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
import com.jm.focustimer.domain.model.DailyFocusTime
import com.jm.focustimer.domain.model.WeeklyStats
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.minutes

/**
 * 주간 통계 컨텐츠
 */
@Composable
fun WeeklyStatsContent(stats: WeeklyStats) {
    val dateFormatter = DateTimeFormatter.ofPattern("M월 d일")

    // 기간 표시
    Text(
        text = "${stats.weekStartDate.format(dateFormatter)} ~ ${
            stats.weekEndDate.format(
                dateFormatter
            )
        }",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    // 요약 카드
    StatsCard(title = "주간 요약") {
        StatsRow(
            label = "총 집중 시간",
            value = formatDuration(stats.totalFocusTime)
        )
        StatsRow(
            label = "완료된 세션",
            value = "${stats.totalSessions}개"
        )
        StatsRow(
            label = "하루 평균 세션",
            value = String.format("%.1f개", stats.averageSessionsPerDay)
        )
        StatsRow(
            label = "하루 평균 집중 시간",
            value = formatDuration(stats.averageDailyFocusTime)
        )
        stats.mostProductiveDay?.let { day ->
            StatsRow(
                label = "가장 생산적인 요일",
                value = getDayOfWeekKorean(day)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // 일별 집중 시간 차트
    StatsCard(title = "일별 집중 시간") {
        DailyChart(stats = stats)
    }
}

@Composable
private fun DailyChart(stats: WeeklyStats) {
    val modelProducer = remember { CartesianChartModelProducer() }

    // 차트 데이터 준비
    LaunchedEffect(stats) {
        val days = stats.dailyBreakdown.mapIndexed { index, _ -> index.toFloat() }
        val focusTimes = stats.dailyBreakdown.map {
            it.focusTime.inWholeMinutes.toFloat()
        }

        modelProducer.runTransaction {
            columnSeries {
                series(x = days, y = focusTimes)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = fill(Color(0xFF03DAC6)),
                        thickness = 8.dp,
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
                title = "요일",
                titleComponent = rememberTextComponent(),
                valueFormatter = { _, value, _ ->
                    val index = value.toInt()
                    if (index in stats.dailyBreakdown.indices) {
                        getDayOfWeekShort(stats.dailyBreakdown[index].dayOfWeek)
                    } else {
                        ""
                    }
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
 * 요일을 한글로 변환
 */
private fun getDayOfWeekKorean(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "월요일"
        DayOfWeek.TUESDAY -> "화요일"
        DayOfWeek.WEDNESDAY -> "수요일"
        DayOfWeek.THURSDAY -> "목요일"
        DayOfWeek.FRIDAY -> "금요일"
        DayOfWeek.SATURDAY -> "토요일"
        DayOfWeek.SUNDAY -> "일요일"
    }
}

/**
 * 요일을 짧은 한글로 변환
 */
private fun getDayOfWeekShort(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
}

class WeeklyStatsProvider : PreviewParameterProvider<WeeklyStats> {
    override val values = sequenceOf(
        WeeklyStats(
            weekStartDate = LocalDate.of(2023, 10, 23),
            weekEndDate = LocalDate.of(2023, 10, 29),
            totalFocusTime = 700.minutes,
            averageSessionsPerDay = 4.0,
            mostProductiveDay = DayOfWeek.WEDNESDAY,
            dailyBreakdown = listOf(
                DailyFocusTime(
                    date = LocalDate.now(),
                    dayOfWeek = DayOfWeek.MONDAY,
                    focusTime = 80.minutes,
                    sessionCount = 12
                ),
                DailyFocusTime(
                    date = LocalDate.now(),
                    dayOfWeek = DayOfWeek.TUESDAY,
                    focusTime = 30.minutes,
                    sessionCount = 12
                ),
                DailyFocusTime(
                    date = LocalDate.now(),
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    focusTime = 200.minutes,
                    sessionCount = 12
                ),
            )
        )
    )
}

@ThemePreviews
@Composable
private fun WeeklyStatsContentPreview(
    @PreviewParameter(WeeklyStatsProvider::class) stats: WeeklyStats
) {
    FocusTimerTheme {
        WeeklyStatsContent(stats = stats)
    }
}
