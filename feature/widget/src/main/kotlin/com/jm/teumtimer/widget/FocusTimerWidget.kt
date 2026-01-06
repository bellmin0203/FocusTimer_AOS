package com.jm.teumtimer.widget

import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
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
import androidx.glance.appwidget.AndroidRemoteViews
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
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentWidth
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.jm.logutil.LogUtil
import com.jm.teumtimer.designsystem.component.TimerColorPresets
import com.jm.teumtimer.widget.FocusTimerWidgetStateManager.Companion.KEY_OVERTIME
import com.jm.teumtimer.widget.FocusTimerWidgetStateManager.Companion.KEY_PRESET_COLOR_INDEX
import com.jm.teumtimer.widget.FocusTimerWidgetStateManager.Companion.KEY_REMAINING_TIME
import com.jm.teumtimer.widget.FocusTimerWidgetStateManager.Companion.KEY_STATUS
import com.jm.teumtimer.widget.theme.FocusTimerGlanceTheme
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Focus Timer Glance Widget
 *
 * Jetpack Glance를 사용한 타이머 위젯
 */
class FocusTimerWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        LogUtil.d("provideGlance Called at: ${System.currentTimeMillis()}")

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

    val status = preferences[KEY_STATUS] ?: FocusTimerWidgetState.IDLE
    val remainingTime =
        (preferences[KEY_REMAINING_TIME]
            ?: FocusTimerWidgetState.DEFAULT_REMAINING_TIME).milliseconds
    val overtime = preferences[KEY_OVERTIME]?.milliseconds
    val presetColorIndex =
        preferences[KEY_PRESET_COLOR_INDEX]?.toIntOrNull() ?: 0

    val widgetState = when (status) {
        FocusTimerWidgetState.RUNNING -> {
            if (overtime != null) FocusTimerWidgetState.Completed(overtime)
            else FocusTimerWidgetState.Running(remainingTime)
        }

        FocusTimerWidgetState.PAUSED -> FocusTimerWidgetState.Paused(remainingTime)
        FocusTimerWidgetState.COMPLETED -> FocusTimerWidgetState.Completed(
            overtime ?: Duration.ZERO
        )

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
 * Duration을 "MM:SS" 형식의 문자열로 변환
 */
private fun formatDuration(duration: Duration, includeSign: Boolean = false): String {
    val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    val formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    return if (includeSign && duration >= Duration.ZERO) {
        "+$formatted"
    } else {
        formatted
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

@Composable
fun TimerChronometer(
    durationMillis: Long, // 타이머 남은 시간 (밀리초)
    isTimerRunning: Boolean,
    isOvertime: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    AndroidRemoteViews(
        remoteViews = RemoteViews(
            LocalContext.current.packageName,
            R.layout.focus_timer_widget_chronometer
        ).apply {
            // 현재 시스템의 부팅 후 경과 시간(elapsedRealtime)을 기준으로 종료 시간을 계산합니다.
            // 주의: durationMillis가 '남은 시간'이라면 아래와 같이 계산합니다.
            // 이미 계산된 '종료 목표 시각'이 있다면 그 값을 elapsedRealtime 기준으로 변환해야 합니다.
            if (isTimerRunning) {
                val targetTime = SystemClock.elapsedRealtime() + durationMillis

                setChronometerCountDown(R.id.chronometer_view, true)
                setChronometer(
                    R.id.chronometer_view,
                    targetTime,
                    null, // XML에 정의된 format 사용
                    true // 타이머가 실행 중일 때만 시계가 흐르도록 설정
                )
            } else {
                setChronometer(
                    R.id.chronometer_view,
                    SystemClock.elapsedRealtime(),
                    null,
                    false
                )

                setTextViewText(
                    R.id.chronometer_view,
                    formatDuration(duration = durationMillis.milliseconds, includeSign = isOvertime)
                )
            }
        },
        modifier = modifier.wrapContentWidth().wrapContentHeight()
    )
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
            remainingTime = 25.minutes,
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
            remainingTime = 20.minutes,
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
            overtime = 1.minutes + 30.seconds,
            onStopClick = {}
        )
    }
}
