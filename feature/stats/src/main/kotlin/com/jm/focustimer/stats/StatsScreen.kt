package com.jm.focustimer.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jm.focustimer.stats.component.AchievementMetricsContent
import com.jm.focustimer.stats.component.DailyStatsContent
import com.jm.focustimer.stats.component.MonthlyStatsContent
import com.jm.focustimer.stats.component.WeeklyStatsContent
import com.jm.focustimer.stats.model.StatsPeriod
import com.jm.focustimer.stats.model.StatsUiState

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
                selectedTabIndex = uiState.selectedPeriod.ordinal
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

            // 기간 네비게이션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousPeriod) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "이전"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onNextPeriod) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "다음"
                    )
                }
            }

            // 로딩 또는 통계 내용
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("통계를 불러오는 중...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 성취 지표 섹션
                    uiState.achievementMetrics?.let { metrics ->
                        AchievementMetricsContent(metrics = metrics)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    when (uiState.selectedPeriod) {
                        StatsPeriod.DAILY -> {
                            uiState.dailyStats?.let { stats ->
                                DailyStatsContent(stats = stats)
                            }
                        }

                        StatsPeriod.WEEKLY -> {
                            uiState.weeklyStats?.let { stats ->
                                WeeklyStatsContent(stats = stats)
                            }
                        }

                        StatsPeriod.MONTHLY -> {
                            uiState.monthlyStats?.let { stats ->
                                MonthlyStatsContent(stats = stats)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * 통계 항목 행
 */
@Composable
fun StatsRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End
        )
    }
}
