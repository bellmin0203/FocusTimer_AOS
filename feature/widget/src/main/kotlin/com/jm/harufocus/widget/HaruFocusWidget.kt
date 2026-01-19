package com.jm.harufocus.widget

import android.content.Context
import android.content.res.Configuration
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentWidth
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.jm.harufocus.designsystem.component.TimerColorPresets
import com.jm.harufocus.widget.HaruFocusWidgetStateManager.Companion.KEY_OVERTIME
import com.jm.harufocus.widget.HaruFocusWidgetStateManager.Companion.KEY_PRESET_COLOR_INDEX
import com.jm.harufocus.widget.HaruFocusWidgetStateManager.Companion.KEY_REMAINING_TIME
import com.jm.harufocus.widget.HaruFocusWidgetStateManager.Companion.KEY_STATUS
import com.jm.harufocus.widget.theme.HaruFocusGlanceTheme
import com.jm.logutil.LogUtil
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 하루 몰입 Glance Widget
 *
 * Jetpack Glance를 사용한 타이머 위젯
 */
class HaruFocusWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        LogUtil.d("provideGlance Called at: ${System.currentTimeMillis()}")

        provideContent {
            HaruFocusGlanceTheme {
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

    val status = preferences[KEY_STATUS] ?: HaruFocusWidgetState.IDLE
    val remainingTime =
        (preferences[KEY_REMAINING_TIME]
            ?: HaruFocusWidgetState.DEFAULT_REMAINING_TIME).milliseconds
    val overtime = preferences[KEY_OVERTIME]?.milliseconds
    val presetColorIndex =
        preferences[KEY_PRESET_COLOR_INDEX]?.toIntOrNull() ?: 0

    val widgetState = when (status) {
        HaruFocusWidgetState.RUNNING -> {
            if (overtime != null) HaruFocusWidgetState.Completed(overtime)
            else HaruFocusWidgetState.Running(remainingTime)
        }

        HaruFocusWidgetState.PAUSED -> HaruFocusWidgetState.Paused(remainingTime)
        HaruFocusWidgetState.COMPLETED -> HaruFocusWidgetState.Completed(
            overtime ?: Duration.ZERO
        )

        else -> HaruFocusWidgetState.Idle
    }

    // 선택된 프리셋의 색상 가져오기
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val presets = if (isDark) TimerColorPresets.darkPresets else TimerColorPresets.lightPresets
    
    val presetColor = presets.getOrNull(presetColorIndex)
        ?: presets[0]

    HaruFocusWidgetContent(
        state = widgetState,
        presetColor = presetColor.progressColor,
        currentRemainingTime = remainingTime,
        onStartClick = { actionStartTimer(context) },
        onPauseClick = { actionPauseTimer(context) },
        onResumeClick = { actionResumeTimer(context) },
        onCompleteClick = { actionCompleteTimer(context) },
        onOpenAppClick = { actionOpenApp(context) },
        onIncreaseClick = { actionIncreaseTime(context) },
        onDecreaseClick = { actionDecreaseTime(context) }
    )
}

/**
 * 위젯 UI 컨텐츠
 */
@Composable
private fun HaruFocusWidgetContent(
    state: HaruFocusWidgetState,
    presetColor: Color,
    currentRemainingTime: Duration,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onOpenAppClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    onDecreaseClick: () -> Unit
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
            is HaruFocusWidgetState.Idle -> IdleContent(
                presetColor = presetColor,
                remainingTime = currentRemainingTime,
                onStartClick = onStartClick,
                onIncreaseClick = onIncreaseClick,
                onDecreaseClick = onDecreaseClick
            )
            is HaruFocusWidgetState.Running -> RunningContent(
                presetColor = presetColor,
                remainingTime = state.remainingTime,
                onPauseClick = onPauseClick
            )

            is HaruFocusWidgetState.Paused -> PausedContent(
                presetColor = presetColor,
                remainingTime = state.remainingTime,
                onResumeClick = onResumeClick
            )

            is HaruFocusWidgetState.Completed -> CompletedContent(
                presetColor = presetColor,
                overtime = state.overtime,
                onCompleteClick = onCompleteClick
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
private fun IdleContent(
    presetColor: Color,
    remainingTime: Duration,
    onStartClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    onDecreaseClick: () -> Unit
) {
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
            durationMillis = remainingTime.inWholeMilliseconds,
            isTimerRunning = false,
            isOvertime = false
        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 감소 버튼
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_widget_minus),
                contentDescription = context.getString(R.string.widget_decrease_time),
                onClick = action(block = onDecreaseClick),
                modifier = GlanceModifier.size(32.dp),
                contentColor = GlanceTheme.colors.onSurface,
                backgroundColor = GlanceTheme.colors.background
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // 시작 버튼
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_widget_play),
                contentDescription = context.getString(R.string.widget_start),
                onClick = action(block = onStartClick),
                modifier = GlanceModifier.size(48.dp),
                contentColor = GlanceTheme.colors.onSecondary,
                backgroundColor = ColorProvider(presetColor)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // 증가 버튼
            CircleIconButton(
                imageProvider = ImageProvider(R.drawable.ic_widget_plus),
                contentDescription = context.getString(R.string.widget_increase_time),
                onClick = action(block = onIncreaseClick),
                modifier = GlanceModifier.size(32.dp),
                contentColor = GlanceTheme.colors.onSurface,
                backgroundColor = GlanceTheme.colors.background
            )
        }
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
    onCompleteClick: () -> Unit
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
            isOvertime = true
        )
//        Text(
//            text = overtime,
//            style = TextStyle(
//                fontSize = 24.sp,
//                color = GlanceTheme.colors.onTertiaryContainer
//            )
//        )

        Spacer(modifier = GlanceModifier.height(16.dp))

        // 완료 확인 버튼
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_widget_check),
            contentDescription = context.getString(R.string.widget_complete),
            onClick = action(block = onCompleteClick),
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
                if (isOvertime) {
                    // Overtime: Count UP
                    // Chronometer (countUp) shows: SystemClock.elapsedRealtime() - base
                    // We want it to equal: durationMillis + (time elapsed since update)
                    // At this exact moment, it should display durationMillis.
                    // durationMillis = Now - base => base = Now - durationMillis
                    val baseTime = SystemClock.elapsedRealtime() - durationMillis
                    setChronometerCountDown(R.id.chronometer_view, false)
                    setChronometer(
                        R.id.chronometer_view,
                        baseTime,
                        "+%s",
                        true
                    )
                } else {
                    // Running: Count DOWN
                    val targetTime = SystemClock.elapsedRealtime() + durationMillis
                    setChronometerCountDown(R.id.chronometer_view, true)
                    setChronometer(
                        R.id.chronometer_view,
                        targetTime,
                        null, // XML에 정의된 format 사용
                        true // 타이머가 실행 중일 때만 시계가 흐르도록 설정
                    )
                }
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