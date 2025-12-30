package com.jm.focustimer.stats

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
import androidx.compose.material.icons.filled.Today
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.model.statistics.AchievementMetrics
import com.jm.focustimer.domain.model.statistics.DailyFocusTime
import com.jm.focustimer.domain.model.statistics.DailyStats
import com.jm.focustimer.domain.model.statistics.HourlyStats
import com.jm.focustimer.domain.model.statistics.MonthlyStats
import com.jm.focustimer.domain.model.statistics.WeeklyFocusTime
import com.jm.focustimer.domain.model.statistics.WeeklyStats
import com.jm.focustimer.stats.component.AchievementMetricsContent
import com.jm.focustimer.stats.component.DailyStatsContent
import com.jm.focustimer.stats.component.MonthlyStatsContent
import com.jm.focustimer.stats.component.WeeklyStatsContent
import com.jm.focustimer.stats.model.StatsPeriod
import com.jm.focustimer.stats.model.StatsUiState
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
                title = { Text("통계") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAchievementSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "성취 기록 보기"
                        )
                    }
                    IconButton(onClick = onToday) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "오늘"
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
                    text = { Text("일간") }
                )
                Tab(
                    selected = uiState.selectedPeriod == StatsPeriod.WEEKLY,
                    onClick = { onPeriodSelected(StatsPeriod.WEEKLY) },
                    text = { Text("주간") }
                )
                Tab(
                    selected = uiState.selectedPeriod == StatsPeriod.MONTHLY,
                    onClick = { onPeriodSelected(StatsPeriod.MONTHLY) },
                    text = { Text("월간") }
                )
            }

            // 로딩 또는 통계 내용
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "통계를 불러오는 중...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                                    onNext = onNextPeriod
                                )
                            }
                        }

                        StatsPeriod.WEEKLY -> {
                            uiState.weeklyStats?.let { stats ->
                                WeeklyStatsContent(
                                    stats = stats,
                                    onPrevious = onPreviousPeriod,
                                    onNext = onNextPeriod
                                )
                            }
                        }

                        StatsPeriod.MONTHLY -> {
                            uiState.monthlyStats?.let { stats ->
                                MonthlyStatsContent(
                                    stats = stats,
                                    onPrevious = onPreviousPeriod,
                                    onNext = onNextPeriod
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
                                Text("아직 기록된 성취가 없습니다.")
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
    FocusTimerTheme {
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
    FocusTimerTheme {
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
    FocusTimerTheme {
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
    FocusTimerTheme {
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
    FocusTimerTheme {
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
        date = LocalDate.now(),
        totalFocusTime = 150.minutes,
        completedSessions = 6,
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
        weekStartDate = LocalDate.now().minusDays(3),
        weekEndDate = LocalDate.now().plusDays(3),
        totalFocusTime = 15.hours,
        dailyBreakdown = (0..6).map { day ->
            DailyFocusTime(
                date = LocalDate.now().minusDays(3L - day),
                dayOfWeek = LocalDate.now().minusDays(3L - day).dayOfWeek,
                focusTime = 2.hours,
                sessionCount = 4
            )
        },
        averageSessionsPerDay = 4.5,
        mostProductiveDay = DayOfWeek.MONDAY
    )

    val monthlyStats = MonthlyStats(
        yearMonth = YearMonth.now(),
        totalFocusTime = 60.hours,
        weeklyBreakdown = (1..4).map { week ->
            WeeklyFocusTime(
                weekOfMonth = week,
                weekStartDate = LocalDate.now(),
                weekEndDate = LocalDate.now(),
                focusTime = 15.hours,
                sessionCount = 20
            )
        },
        averageSessionsPerWeek = 25.0,
        mostProductiveWeek = 2,
        growthRate = 0.15
    )
}
