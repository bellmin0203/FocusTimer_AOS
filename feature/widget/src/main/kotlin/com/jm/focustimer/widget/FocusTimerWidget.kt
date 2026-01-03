package com.jm.focustimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
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
import com.jm.focustimer.designsystem.theme.TimerColorPresets
import com.jm.focustimer.widget.FocusTimerWidgetStateManager.Companion.KEY_OVERTIME
import com.jm.focustimer.widget.FocusTimerWidgetStateManager.Companion.KEY_PRESET_COLOR_INDEX
import com.jm.focustimer.widget.FocusTimerWidgetStateManager.Companion.KEY_REMAINING_TIME
import com.jm.focustimer.widget.FocusTimerWidgetStateManager.Companion.KEY_STATUS
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

    val status = preferences[KEY_STATUS] ?: "idle"
    val remainingTime = preferences[KEY_REMAINING_TIME] ?: "00:00"
    val overtime = preferences[KEY_OVERTIME]
    val presetColorIndex =
        preferences[KEY_PRESET_COLOR_INDEX]?.toIntOrNull() ?: 0

    val widgetState = when (status) {
        "running" -> {
            if (overtime != null) FocusTimerWidgetState.Completed(overtime)
            else FocusTimerWidgetState.Running(remainingTime)
        }
        "paused" -> FocusTimerWidgetState.Paused(remainingTime)
        "completed" -> FocusTimerWidgetState.Completed(overtime ?: "+00:00")
        else -> FocusTimerWidgetState.Idle
    }

    // 선택된 프리셋의 색상 가져오기
    val presetColor = TimerColorPresets.lightPresets.getOrNull(presetColorIndex)
        ?: TimerColorPresets.lightPresets[0]

    FocusTimerWidgetContent(
        state = widgetState,
        presetColor = presetColor.progressColor,
        onStartClick = { actionStartTimer(context) },
        onPauseClick = { actionPauseTimer(context) },
        onResumeClick = { actionResumeTimer(context) },
        onStopClick = { actionStopTimer(context) },
        onOpenAppClick = { actionOpenApp(context) }
    )
}

/**
 * 위젯 UI 컨텐츠
 */
@Composable
private fun FocusTimerWidgetContent(
    state: FocusTimerWidgetState,
    presetColor: Color,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStopClick: () -> Unit,
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
            is FocusTimerWidgetState.Idle -> IdleContent(presetColor, onStartClick)
            is FocusTimerWidgetState.Running -> RunningContent(
                presetColor = presetColor,
                remainingTime = state.remainingTime,
                onPauseClick = onPauseClick
            )

            is FocusTimerWidgetState.Paused -> PausedContent(
                presetColor = presetColor,
                remainingTime = state.remainingTime,
                onResumeClick = onResumeClick
            )

            is FocusTimerWidgetState.Completed -> CompletedContent(
                presetColor = presetColor,
                overtime = state.overtime,
                onStopClick = onStopClick
            )
        }
    }
}

/**
 * Idle 상태 컨텐츠
 */
@Composable
private fun IdleContent(presetColor: Color, onStartClick: () -> Unit) {
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
                color = GlanceTheme.colors.onSurface
            )
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 시작 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_start),
            onClick = action(block = onStartClick),
            modifier = GlanceModifier.size(48.dp),
            contentColor = GlanceTheme.colors.onSecondary,
            backgroundColor = ColorProvider(presetColor)
        )
    }
}

/**
 * Running 상태 컨텐츠
 */
@Composable
private fun RunningContent(
    presetColor: Color,
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
                color = GlanceTheme.colors.onSurface
            )
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 일시정지 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_pause),
            contentDescription = context.getString(R.string.widget_pause),
            onClick = action(block = onPauseClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = ColorProvider(presetColor),
            contentColor = GlanceTheme.colors.onSecondary
        )
    }
}

/**
 * Paused 상태 컨텐츠
 */
@Composable
private fun PausedContent(
    presetColor: Color,
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
                color = GlanceTheme.colors.onSurface
            )
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 재개 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_resume),
            onClick = action(block = onResumeClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = ColorProvider(presetColor),
            contentColor = GlanceTheme.colors.onSecondary
        )
    }
}

/**
 * Completed 상태 컨텐츠
 */
@Composable
private fun CompletedContent(
    presetColor: Color,
    overtime: String,
    onStopClick: () -> Unit
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
                color = GlanceTheme.colors.onTertiaryContainer
            )
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 다시 시작 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_check),
            contentDescription = context.getString(R.string.widget_complete),
            onClick = action(block = onStopClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = ColorProvider(presetColor),
            contentColor = GlanceTheme.colors.onSecondary
        )
    }
}

private class ColorProvider(val color: Color) : androidx.glance.unit.ColorProvider {
    override fun getColor(context: Context): Color {
        return color
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun PreviewIdleContent() {
    FocusTimerGlanceTheme {
        IdleContent(
            presetColor = TimerColorPresets.lightPresets[1].progressColor,
            onStartClick = {})
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview
@Composable
private fun PreviewRunningContent() {
    FocusTimerGlanceTheme {
        RunningContent(
            presetColor = TimerColorPresets.lightPresets[1].progressColor,
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
            presetColor = TimerColorPresets.lightPresets[2].progressColor,
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
            presetColor = TimerColorPresets.lightPresets[3].progressColor,
            overtime = "+01:30",
            onStopClick = {}
        )
    }
}
