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
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.CircleIconButton
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
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.jm.focustimer.widget.theme.FocusTimerGlanceTheme

/**
 * Focus Timer Glance Widget
 *
 * Jetpack Glance를 사용한 타이머 위젯
 */
class FocusTimerWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            FocusTimerGlanceTheme {
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
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.widget_title),
            style = TextStyle(
                fontSize = 18.sp,
                color = GlanceTheme.colors.onBackground
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = context.getString(R.string.widget_default_time),
            style = TextStyle(
                fontSize = 32.sp,
                color = GlanceTheme.colors.primary
            )
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 시작 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_start),
            onClick = action(block = onStartClick),
            modifier = GlanceModifier.size(48.dp),
            contentColor = GlanceTheme.colors.onSecondary
        )
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
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.widget_status_running),
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
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_pause),
            contentDescription = context.getString(R.string.widget_pause),
            onClick = action(block = onPauseClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = GlanceTheme.colors.primary,
            contentColor = GlanceTheme.colors.onSecondary
        )
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
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.widget_status_paused),
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
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_resume),
            onClick = action(block = onResumeClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = GlanceTheme.colors.primary,
            contentColor = GlanceTheme.colors.onSecondary
        )
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
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = context.getString(R.string.widget_status_completed),
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
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_start),
            onClick = action(block = onStartClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = GlanceTheme.colors.primary,
            contentColor = GlanceTheme.colors.onSecondary
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun PreviewIdleContent() {
    FocusTimerGlanceTheme {
        IdleContent(onStartClick = {})
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun PreviewRunningContent() {
    FocusTimerGlanceTheme {
        RunningContent(
            remainingTime = "25:00",
            onPauseClick = {}
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun PreviewPausedContent() {
    FocusTimerGlanceTheme {
        PausedContent(
            remainingTime = "20:00",
            onResumeClick = {}
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun PreviewCompletedContent() {
    FocusTimerGlanceTheme {
        CompletedContent(
            overtime = "+01:30",
            onStartClick = {}
        )
    }
}
