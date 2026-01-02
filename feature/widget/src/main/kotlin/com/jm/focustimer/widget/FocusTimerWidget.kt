package com.jm.focustimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

/**
 * Focus Timer Glance Widget
 * 
 * Jetpack Glance를 사용한 타이머 위젯
 */
class FocusTimerWidget : GlanceAppWidget() {

    override val stateDefinition = androidx.glance.state.PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }
}

/**
 * 위젯 메인 컨텐츠 (Composable)
 * 
 * Glance는 일반 Compose의 State를 지원하지 않으므로,
 * 위젯 업데이트 시마다 DataStore에서 상태를 직접 읽어옵니다.
 */
@Composable
private fun WidgetContent(context: Context) {
    // Glance 위젯은 currentState를 사용하여 DataStore에서 상태를 동기적으로 읽습니다
    val preferences = currentState<Preferences>()
    
    val status = preferences[stringPreferencesKey("status")] ?: "idle"
    val remainingTime = preferences[stringPreferencesKey("remaining_time")] ?: "00:00"
    val overtime = preferences[stringPreferencesKey("overtime")] ?: "+00:00"
    
    val widgetState = when (status) {
        "running" -> FocusTimerWidgetState.Running(remainingTime)
        "paused" -> FocusTimerWidgetState.Paused(remainingTime)
        "completed" -> FocusTimerWidgetState.Completed(overtime)
        else -> FocusTimerWidgetState.Idle
    }
    
    FocusTimerWidgetContent(
        state = widgetState,
        onStartClick = { actionStartTimer(context) },
        onPauseClick = { actionPauseTimer(context) },
        onResumeClick = { actionResumeTimer(context) },
        onOpenAppClick = { actionOpenApp(context) }
    )
}

/**
 * 위젯 UI 컨텐츠
 */
@Composable
private fun FocusTimerWidgetContent(
    state: FocusTimerWidgetState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onOpenAppClick: () -> Unit
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(16.dp)
            .clickable(onClick = action(block = onOpenAppClick)),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is FocusTimerWidgetState.Idle -> IdleContent(onStartClick)
            is FocusTimerWidgetState.Running -> RunningContent(
                remainingTime = state.remainingTime,
                onPauseClick = onPauseClick
            )
            is FocusTimerWidgetState.Paused -> PausedContent(
                remainingTime = state.remainingTime,
                onResumeClick = onResumeClick
            )
            is FocusTimerWidgetState.Completed -> CompletedContent(
                overtime = state.overtime,
                onStartClick = onStartClick
            )
        }
    }
}

/**
 * Idle 상태 컨텐츠
 */
@Composable
private fun IdleContent(onStartClick: () -> Unit) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Focus Timer",
            style = TextStyle(
                fontSize = 18.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = "00:00",
            style = TextStyle(
                fontSize = 32.sp,
                color = GlanceTheme.colors.primary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // 시작 버튼
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(GlanceTheme.colors.primary)
                .clickable(onClick = action(block = onStartClick)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = GlanceTheme.colors.onPrimary
                )
            )
        }
    }
}

/**
 * Running 상태 컨텐츠
 */
@Composable
private fun RunningContent(
    remainingTime: String,
    onPauseClick: () -> Unit
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "진행 중",
            style = TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = remainingTime,
            style = TextStyle(
                fontSize = 32.sp,
                color = GlanceTheme.colors.primary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // 일시정지 버튼
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(GlanceTheme.colors.secondary)
                .clickable(onClick = action(block = onPauseClick)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⏸",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = GlanceTheme.colors.onSecondary
                )
            )
        }
    }
}

/**
 * Paused 상태 컨텐츠
 */
@Composable
private fun PausedContent(
    remainingTime: String,
    onResumeClick: () -> Unit
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "일시정지",
            style = TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = remainingTime,
            style = TextStyle(
                fontSize = 32.sp,
                color = GlanceTheme.colors.secondary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // 재개 버튼
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(GlanceTheme.colors.primary)
                .clickable(onClick = action(block = onResumeClick)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = GlanceTheme.colors.onPrimary
                )
            )
        }
    }
}

/**
 * Completed 상태 컨텐츠
 */
@Composable
private fun CompletedContent(
    overtime: String,
    onStartClick: () -> Unit
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "완료!",
            style = TextStyle(
                fontSize = 18.sp,
                color = GlanceTheme.colors.tertiary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Text(
            text = overtime,
            style = TextStyle(
                fontSize = 24.sp,
                color = GlanceTheme.colors.tertiary
            )
        )
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // 다시 시작 버튼
        Box(
            modifier = GlanceModifier
                .size(48.dp)
                .background(GlanceTheme.colors.primary)
                .clickable(onClick = action(block = onStartClick)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = GlanceTheme.colors.onPrimary
                )
            )
        }
    }
}
