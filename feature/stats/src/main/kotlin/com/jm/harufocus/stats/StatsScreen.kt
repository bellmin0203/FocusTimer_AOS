package com.jm.harufocus.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.domain.model.statistics.AchievementMetrics
import com.jm.harufocus.domain.model.statistics.DailyFocusTime
import com.jm.harufocus.domain.model.statistics.DailyStats
import com.jm.harufocus.domain.model.statistics.HourlyStats
import com.jm.harufocus.domain.model.statistics.MonthlyStats
import com.jm.harufocus.domain.model.statistics.WeeklyFocusTime
import com.jm.harufocus.domain.model.statistics.WeeklyStats
import com.jm.harufocus.stats.component.AchievementMetricsContent
import com.jm.harufocus.stats.component.DailyStatsContent
import com.jm.harufocus.stats.component.MonthlyStatsContent
import com.jm.harufocus.stats.component.WeeklyStatsContent
import com.jm.harufocus.stats.model.StatsPeriod
import com.jm.harufocus.stats.model.StatsUiState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * 통계 화면
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    StatsScreen(
        uiState = uiState,
        onPeriodSelected = viewModel::selectPeriod,
        onPreviousPeriod = viewModel::navigateToPreviousPeriod,
        onNextPeriod = viewModel::navigateToNextPeriod,
        onToday = viewModel::navigateToToday,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsScreen(
    uiState: StatsUiState,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onToday: () -> Unit,
    onBackClick: () -> Unit
) {

    var showAchievementSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAchievementSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = stringResource(R.string.content_description_achievements)
                        )
                    }
                    TextButton(onClick = onToday) {
                        Text(
                            text = when (uiState.selectedPeriod) {
                                StatsPeriod.DAILY -> stringResource(R.string.stats_today)
                                StatsPeriod.WEEKLY -> stringResource(R.string.stats_this_week)
                                StatsPeriod.MONTHLY -> stringResource(R.string.stats_this_month)
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 기간 선택 탭
            PrimaryTabRow(
                selectedTabIndex = uiState.selectedPeriod.ordinal,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Tab(
                    selected = uiState.selectedPeriod == StatsPeriod.DAILY,
                    onClick = { onPeriodSelected(StatsPeriod.DAILY) },
                    text = {
                        Text(
                            text = stringResource(R.string.stats_tab_daily),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedPeriod == StatsPeriod.WEEKLY,
                    onClick = { onPeriodSelected(StatsPeriod.WEEKLY) },
                    text = {
                        Text(
                            text = stringResource(R.string.stats_tab_weekly),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                Tab(
                    selected = uiState.selectedPeriod == StatsPeriod.MONTHLY,
                    onClick = { onPeriodSelected(StatsPeriod.MONTHLY) },
                    text = {
                        Text(
                            text = stringResource(R.string.stats_tab_monthly),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }

            // 로딩 또는 통계 내용
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.stats_loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    when (uiState.selectedPeriod) {
                        StatsPeriod.DAILY -> {
                            uiState.dailyStats?.let { stats ->
                                DailyStatsContent(
                                    stats = stats,
                                    onPrevious = onPreviousPeriod,
                                    onNext = onNextPeriod,
                                    canNavigateNext = stats.date.isBefore(LocalDate.now())
                                )
                            }
                        }

                        StatsPeriod.WEEKLY -> {
                            uiState.weeklyStats?.let { stats ->
                                WeeklyStatsContent(
                                    stats = stats,
                                    onPrevious = onPreviousPeriod,
                                    onNext = onNextPeriod,
                                    canNavigateNext = !stats.weekStartDate.plusWeeks(1).isAfter(LocalDate.now())
                                )
                            }
                        }

                        StatsPeriod.MONTHLY -> {
                            uiState.monthlyStats?.let { stats ->
                                MonthlyStatsContent(
                                    stats = stats,
                                    onPrevious = onPreviousPeriod,
                                    onNext = onNextPeriod,
                                    canNavigateNext = stats.yearMonth.isBefore(YearMonth.now())
                                )
                            }
                        }
                    }

                    // 에러 메시지
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // 성취 기록 바텀시트
                if (showAchievementSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showAchievementSheet = false },
                        sheetState = sheetState,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 48.dp)
                        ) {
                            uiState.achievementMetrics?.let { metrics ->
                                AchievementMetricsContent(metrics = metrics)
                            } ?: run {
                                Text(stringResource(R.string.stats_no_achievements))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 통계 카드 공통 컴포넌트
 */
@Composable
fun StatsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenLoadingPreview() {
    HaruFocusTheme {
        StatsScreen(
            uiState = StatsUiState(isLoading = true),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onToday = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenDailyPreview() {
    HaruFocusTheme {
        StatsScreen(
            uiState = StatsUiState(
                selectedPeriod = StatsPeriod.DAILY,
                achievementMetrics = StatsPreviewData.achievementMetrics,
                dailyStats = StatsPreviewData.dailyStats
            ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onToday = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenWeeklyPreview() {
    HaruFocusTheme {
        StatsScreen(
            uiState = StatsUiState(
                selectedPeriod = StatsPeriod.WEEKLY,
                achievementMetrics = StatsPreviewData.achievementMetrics,
                weeklyStats = StatsPreviewData.weeklyStats
            ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onToday = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenMonthlyPreview() {
    HaruFocusTheme {
        StatsScreen(
            uiState = StatsUiState(
                selectedPeriod = StatsPeriod.MONTHLY,
                achievementMetrics = StatsPreviewData.achievementMetrics,
                monthlyStats = StatsPreviewData.monthlyStats
            ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onToday = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenErrorPreview() {
    HaruFocusTheme {
        StatsScreen(
            uiState = StatsUiState(
                error = "데이터를 불러오는 중 오류가 발생했습니다."
            ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onToday = {},
            onBackClick = {}
        )
    }
}

private object StatsPreviewData {
    val achievementMetrics = AchievementMetrics(
        focusRate = 0.85,
        consecutiveFocusDays = 5,
        longestFocusTime = 50.minutes,
        averageSessionLength = 25.minutes,
        totalSessions = 20,
        completedSessions = 17,
        totalFocusTime = 425.minutes
    )

    val dailyStats = DailyStats(
        date = LocalDate.of(2024, 3, 15),
        totalFocusTime = 150.minutes,
        completedSessions = 6,
        fullCompletedTime = 100.minutes,
        partialCompletedTime = 50.minutes,
        hourlyBreakdown = (0..23).map { hour ->
            HourlyStats(
                hour = hour,
                focusTime = if (hour in 9..18) 25.minutes else 0.minutes,
                sessionCount = if (hour in 9..18) 1 else 0
            )
        },
        mostProductiveHour = 10
    )

    val weeklyStats = WeeklyStats(
        weekStartDate = LocalDate.of(2024, 3, 11),
        weekEndDate = LocalDate.of(2024, 3, 17),
        totalFocusTime = 15.hours,
        dailyBreakdown = (0..6).map { day ->
            val date = LocalDate.of(2024, 3, 11).plusDays(day.toLong())
            DailyFocusTime(
                date = date,
                dayOfWeek = date.dayOfWeek,
                focusTime = 2.hours,
                sessionCount = 4,
                fullCompletedSessionCount = 3,
                partialSessionCount = 1
            )
        },
        averageSessionsPerDay = 4.5,
        mostProductiveDay = DayOfWeek.MONDAY,
        growthRate = 0.5
    )

    val monthlyStats = MonthlyStats(
        yearMonth = YearMonth.of(2024, 3),
        totalFocusTime = 60.hours,
        weeklyBreakdown = (1..4).map { week ->
            WeeklyFocusTime(
                weekOfMonth = week,
                weekStartDate = LocalDate.of(2024, 3, 1).plusWeeks(week.toLong() - 1),
                weekEndDate = LocalDate.of(2024, 3, 7).plusWeeks(week.toLong() - 1),
                focusTime = 15.hours,
                sessionCount = 20,
                fullCompletedSessionCount = 15,
                partialSessionCount = 5
            )
        },
        averageSessionsPerWeek = 25.0,
        mostProductiveWeek = 2,
        growthRate = 0.15
    )
}
