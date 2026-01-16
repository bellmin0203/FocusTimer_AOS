package com.jm.harufocus.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.action
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.jm.harufocus.designsystem.component.TimerColorPresets
import com.jm.harufocus.widget.theme.HaruFocusGlanceTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

        TimerChronometer(
            durationMillis = 0,
            isTimerRunning = false,
            isOvertime = false
        )
//        Text(
//            text = context.getString(R.string.widget_default_time),
//            style = TextStyle(
//                fontSize = 32.sp,
//                color = GlanceTheme.colors.onSurface
//            )
//        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 시작 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_start),
            onClick = action(block = onStartClick),
            modifier = GlanceModifier.size(48.dp),
            contentColor = GlanceTheme.colors.onSecondary,
            backgroundColor = ColorProvider2(presetColor)
        )
    }
}

/**
 * Running 상태 컨텐츠
 */
@Composable
private fun RunningContent(
    presetColor: Color,
    remainingTime: Duration,
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

        TimerChronometer(
            durationMillis = remainingTime.inWholeMilliseconds,
            isTimerRunning = true,
            isOvertime = false
        )
//        Text(
//            text = remainingTime,
//            style = TextStyle(
//                fontSize = 32.sp,
//                color = GlanceTheme.colors.onSurface
//            )
//        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 일시정지 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_pause),
            contentDescription = context.getString(R.string.widget_pause),
            onClick = action(block = onPauseClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = ColorProvider2(presetColor),
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
    remainingTime: Duration,
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

        TimerChronometer(
            durationMillis = remainingTime.inWholeMilliseconds,
            isTimerRunning = false,
            isOvertime = false
        )
//        Text(
//            text = remainingTime,
//            style = TextStyle(
//                fontSize = 32.sp,
//                color = GlanceTheme.colors.onSurface
//            )
//        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 재개 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_play),
            contentDescription = context.getString(R.string.widget_resume),
            onClick = action(block = onResumeClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = ColorProvider2(presetColor),
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
    overtime: Duration,
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

        TimerChronometer(
            durationMillis = overtime.inWholeMilliseconds,
            isTimerRunning = true,
            isOvertime = overtime > Duration.ZERO
        )
//        Text(
//            text = overtime,
//            style = TextStyle(
//                fontSize = 24.sp,
//                color = GlanceTheme.colors.onTertiaryContainer
//            )
//        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 다시 시작 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_check),
            contentDescription = context.getString(R.string.widget_complete),
            onClick = action(block = onStopClick),
            modifier = GlanceModifier.size(48.dp),
            backgroundColor = ColorProvider2(presetColor),
            contentColor = GlanceTheme.colors.onSecondary
        )
    }
}

private class ColorProvider2(val color: Color) : androidx.glance.unit.ColorProvider {
    override fun getColor(context: Context): Color {
        return color
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 180)
@Composable
private fun PreviewIdleContent() {
    HaruFocusGlanceTheme {
        IdleContent(
            presetColor = TimerColorPresets.presetColors[1].progressColor,
            onStartClick = {})
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 180)
@Composable
private fun PreviewRunningContent() {
    HaruFocusGlanceTheme {
        RunningContent(
            presetColor = TimerColorPresets.presetColors[1].progressColor,
            remainingTime = 25.minutes,
            onPauseClick = {}
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 180)
@Composable
private fun PreviewPausedContent() {
    HaruFocusGlanceTheme {
        PausedContent(
            presetColor = TimerColorPresets.presetColors[2].progressColor,
            remainingTime = 20.minutes,
            onResumeClick = {}
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 180)
@Composable
private fun PreviewCompletedContent() {
    HaruFocusGlanceTheme {
        CompletedContent(
            presetColor = TimerColorPresets.presetColors[3].progressColor,
            overtime = 1.minutes + 30.seconds,
            onStopClick = {}
        )
    }
}