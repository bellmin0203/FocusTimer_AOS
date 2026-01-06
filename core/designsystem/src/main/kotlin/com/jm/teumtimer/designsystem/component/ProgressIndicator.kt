package com.jm.teumtimer.designsystem.component

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.jm.teumtimer.designsystem.theme.FocusTimerTheme
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// 상수 정의
private object CircularTimerConstants {
    const val FULL_CIRCLE_DEGREES = 360f
    const val QUARTER_CIRCLE_DEGREES = 90f
    const val DEGREES_PER_MINUTE = 6f // 360도 / 60분
    const val MAJOR_TICK_MULTIPLIER = 1.5f
    const val MINOR_TICK_MULTIPLIER = 0.6f
    const val TEXT_VERTICAL_OFFSET_DIVISOR = 3f
    const val KNOB_BORDER_WIDTH = 2f
}

/**
 * 원형 타이머 프로그레스
 * 타이머 화면의 원형 진행 표시기
 */
@Composable
fun CircularTimerProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    colorScheme: TimerColorScheme? = null,
    showTicks: Boolean = true,
    tickCount: Int = 60,
    tickColor: Color? = null,
    showLabels: Boolean = true,
    maxTime: Int = 60,
    labelInterval: Int = 5,
    labelColor: Color? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color? = null,
    showKnob: Boolean = true,
    knobColor: Color? = null,
    enabled: Boolean = true,
    onProgressChange: ((Float) -> Unit)? = null,
    isCompleted: Boolean = false,
    pulseAnimationEnabled: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    // 무한 반복 트랜지션 (Pulse 효과)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // 스케일 애니메이션
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // 알파 값 애니메이션 (0.8 ~ 1.0)
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    // 최종 색상 결정
    val colors = resolveTimerColors(
        colorScheme = colorScheme,
        progressColor = progressColor,
        knobColor = knobColor,
        tickColor = tickColor,
        labelColor = labelColor
    )

    Box(
        modifier = if (isCompleted && pulseAnimationEnabled) {
            modifier.graphicsLayer(
                scaleX = pulseScale, scaleY = pulseScale, alpha = pulseAlpha
            )
        } else {
            modifier
        }.then(
            createDragModifierIfEnabled(enabled, onProgressChange)
        ), contentAlignment = Alignment.Center
    ) {
        // 타이머 원형 표시
        TimerCircleLayer(
            progress = progress,
            backgroundColor = backgroundColor,
            colors = colors,
            showKnob = showKnob,
            content = content
        )

        // 눈금과 라벨 레이어
        TicksAndLabelsLayer(
            showTicks = showTicks,
            tickCount = tickCount,
            colors = colors,
            showLabels = showLabels,
            maxTime = maxTime,
            labelInterval = labelInterval
        )
    }
}

/**
 * 타이머 색상 스킴 결정
 */
@Composable
private fun resolveTimerColors(
    colorScheme: TimerColorScheme?,
    progressColor: Color?,
    knobColor: Color?,
    tickColor: Color?,
    labelColor: Color?
): TimerColors {
    return TimerColors(
        progressColor = colorScheme?.progressColor
            ?: progressColor
            ?: MaterialTheme.colorScheme.primary,
        knobColor = colorScheme?.knobColor
            ?: knobColor
            ?: MaterialTheme.colorScheme.background,
        tickColor = colorScheme?.tickColor
            ?: tickColor
            ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        labelColor = colorScheme?.labelColor
            ?: labelColor
            ?: MaterialTheme.colorScheme.onSurface
    )
}

/**
 * 타이머 색상 데이터 클래스
 */
private data class TimerColors(
    val progressColor: Color,
    val knobColor: Color,
    val tickColor: Color,
    val labelColor: Color
)

/**
 * 드래그 제스처 Modifier 생성
 */
private fun createDragModifierIfEnabled(
    enabled: Boolean,
    onProgressChange: ((Float) -> Unit)?
): Modifier {
    if (!enabled || onProgressChange == null) {
        return Modifier
    }

    return Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown()
                val center = Offset(size.width / 2f, size.height / 2f)

                // 터치 영역이 원의 바깥이면 무시
                val radius = if (size.height >= size.width) size.width / 2f else size.height / 2f

                val distance = (down.position - center).getDistance()
                if (distance > radius) continue

                // 초기 터치 위치로 progress 설정
                onProgressChange(calculateProgressFromPosition(down.position, center))

                // 드래그 감지
                drag(down.id) { change ->
                    val newProgress = calculateProgressFromPosition(change.position, center)
                    onProgressChange(newProgress)
                    change.consume()
                }
            }
        }
    }
}

/**
 * 터치 위치로부터 progress 값 계산 (0.0 ~ 1.0)
 */
private fun calculateProgressFromPosition(position: Offset, center: Offset): Float {
    val dx = position.x - center.x
    val dy = position.y - center.y

    // 중심에서 터치 지점까지의 각도를 라디안으로 계산 (3시 방향이 0도 기준)
    var angle = atan2(dy, dx) * 180f / Math.PI.toFloat()

    // 12시 방향을 0도 기준으로 조정 (0~360도)
    angle =
        (angle + CircularTimerConstants.QUARTER_CIRCLE_DEGREES + CircularTimerConstants.FULL_CIRCLE_DEGREES) % CircularTimerConstants.FULL_CIRCLE_DEGREES

    // 1분 단위로 스냅
    val snappedAngle = snapAngleToMinute(angle)

    // 각도를 progress로 변환 (0.0 ~ 1.0)
    return (snappedAngle / CircularTimerConstants.FULL_CIRCLE_DEGREES).coerceIn(0f, 1f)
}

/**
 * 각도를 가장 가까운 분 단위로 스냅
 */
private fun snapAngleToMinute(angle: Float): Float {
    val roundedMinutes = (angle / CircularTimerConstants.DEGREES_PER_MINUTE).roundToInt()

    return if (roundedMinutes == 60) {
        CircularTimerConstants.FULL_CIRCLE_DEGREES
    } else {
        roundedMinutes * CircularTimerConstants.DEGREES_PER_MINUTE
    }
}

/**
 * 타이머 원형 및 프로그레스 표시 레이어
 */
@Composable
private fun TimerCircleLayer(
    progress: Float,
    backgroundColor: Color,
    colors: TimerColors,
    showKnob: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val arcPadding = 8.dp.toPx()
            val innerCircleInset = 28.dp.toPx() + arcPadding

            // 배경 원 그리기
            drawBackgroundCircle(
                backgroundColor = backgroundColor,
                diameter = diameter,
                innerCircleInset = innerCircleInset
            )

            // 프로그레스 원 그리기
            if (progress > 0f) {
                drawProgressArc(
                    progress = progress,
                    color = colors.progressColor,
                    diameter = diameter,
                    innerCircleInset = innerCircleInset
                )
            }

            // Knob (진행 위치 표시) 그리기
            if (showKnob && progress > 0f) {
                drawProgressKnob(
                    progress = progress,
                    progressColor = colors.progressColor,
                    knobColor = colors.knobColor
                )
            }
        }

        // 중앙 콘텐츠
        content()
    }
}

/**
 * 배경 원 그리기
 */
private fun DrawScope.drawBackgroundCircle(
    backgroundColor: Color,
    diameter: Float,
    innerCircleInset: Float
) {
    val radius = diameter / 2
    val startX = center.x - radius + innerCircleInset
    val startY = center.y - radius + innerCircleInset

    drawArc(
        color = backgroundColor,
        startAngle = 0f,
        sweepAngle = CircularTimerConstants.FULL_CIRCLE_DEGREES,
        useCenter = false,
        topLeft = Offset(startX, startY),
        size = Size(diameter - innerCircleInset * 2, diameter - innerCircleInset * 2),
        style = Fill
    )
}

/**
 * 프로그레스 원호 그리기
 */
private fun DrawScope.drawProgressArc(
    progress: Float,
    color: Color,
    diameter: Float,
    innerCircleInset: Float
) {
    val radius = diameter / 2
    val startX = center.x - radius + innerCircleInset
    val startY = center.y - radius + innerCircleInset

    drawArc(
        color = color,
        startAngle = -CircularTimerConstants.QUARTER_CIRCLE_DEGREES,
        sweepAngle = CircularTimerConstants.FULL_CIRCLE_DEGREES * progress,
        useCenter = true,
        topLeft = Offset(startX, startY),
        size = Size(diameter - innerCircleInset * 2, diameter - innerCircleInset * 2),
        style = Fill
    )
}

/**
 * 프로그레스 노브 그리기
 */
private fun DrawScope.drawProgressKnob(
    progress: Float,
    progressColor: Color,
    knobColor: Color
) {
    val knobRadius = 4.dp.toPx()

    // Knob 외곽선
    drawCircle(
        color = progressColor,
        radius = knobRadius + CircularTimerConstants.KNOB_BORDER_WIDTH.dp.toPx(),
        center = center
    )

    // Knob 내부
    drawCircle(
        color = knobColor,
        radius = knobRadius,
        center = center
    )

    // Knob에서 중심으로 이어지는 선
    val angle =
        (CircularTimerConstants.FULL_CIRCLE_DEGREES * progress - CircularTimerConstants.QUARTER_CIRCLE_DEGREES) * Math.PI / 180f
    val endX = center.x + knobRadius * cos(angle).toFloat()
    val endY = center.y + knobRadius * sin(angle).toFloat()

    drawLine(
        color = progressColor,
        start = Offset(center.x, center.y),
        end = Offset(endX, endY),
        strokeWidth = 0.5.dp.toPx(),
        cap = StrokeCap.Round
    )
}

/**
 * 눈금과 라벨 표시 레이어
 */
@Composable
private fun TicksAndLabelsLayer(
    showTicks: Boolean,
    tickCount: Int,
    colors: TimerColors,
    showLabels: Boolean,
    maxTime: Int,
    labelInterval: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val diameter = size.minDimension
        val radius = diameter / 2f

        // 눈금 그리기
        if (showTicks) {
            drawTickMarks(
                tickCount = tickCount,
                radius = radius,
                tickLength = 8.dp.toPx(),
                tickWidth = 1.dp.toPx(),
                color = colors.tickColor,
                majorTickInterval = 5
            )
        }

        // 시간 레이블 그리기
        if (showLabels) {
            drawTimeLabels(
                maxTime = maxTime,
                labelInterval = labelInterval,
                radius = radius - 20.dp.toPx(),
                textSize = 12.dp.toPx(),
                color = colors.labelColor
            )
        }
    }
}

/**
 * 눈금 그리기
 */
private fun DrawScope.drawTickMarks(
    tickCount: Int,
    radius: Float,
    tickLength: Float,
    tickWidth: Float,
    color: Color,
    majorTickInterval: Int
) {
    for (i in 0 until tickCount) {
        val angle =
            (i * CircularTimerConstants.FULL_CIRCLE_DEGREES / tickCount - CircularTimerConstants.QUARTER_CIRCLE_DEGREES) * Math.PI / 180f

        // 주요 눈금(5분 단위)과 일반 눈금 구분
        val isMajorTick = i % majorTickInterval == 0
        val length = if (isMajorTick) {
            tickLength
        } else {
            tickLength * CircularTimerConstants.MINOR_TICK_MULTIPLIER
        }
        val width = if (isMajorTick) {
            tickWidth * CircularTimerConstants.MAJOR_TICK_MULTIPLIER
        } else {
            tickWidth
        }

        val startX = center.x + (radius - length) * cos(angle).toFloat()
        val startY = center.y + (radius - length) * sin(angle).toFloat()
        val endX = center.x + radius * cos(angle).toFloat()
        val endY = center.y + radius * sin(angle).toFloat()

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}

/**
 * 시간 라벨 그리기
 */
private fun DrawScope.drawTimeLabels(
    maxTime: Int,
    labelInterval: Int,
    radius: Float,
    textSize: Float,
    color: Color
) {
    val textPaint = createTextPaint(textSize, color)

    for (i in 0 until maxTime step labelInterval) {
        val angle =
            (i * CircularTimerConstants.FULL_CIRCLE_DEGREES / maxTime - CircularTimerConstants.QUARTER_CIRCLE_DEGREES) * Math.PI / 180f

        val x = center.x + radius * cos(angle).toFloat()
        // y 좌표는 텍스트 중앙 정렬을 위해 오프셋 추가
        val y =
            center.y + radius * sin(angle).toFloat() + textSize / CircularTimerConstants.TEXT_VERTICAL_OFFSET_DIVISOR

        drawContext.canvas.nativeCanvas.drawText(
            i.toString(),
            x,
            y,
            textPaint
        )
    }
}

/**
 * 텍스트 Paint 객체 생성
 */
private fun createTextPaint(textSize: Float, color: Color): Paint {
    return Paint().apply {
        this.color = color.toArgb()
        this.textSize = textSize
        this.textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
}

@ThemePreviews
@Composable
fun CircularTimerProgressMintPreview() {
    FocusTimerTheme {
        CircularTimerProgress(
            progress = 0.65f,
            modifier = Modifier.size(200.dp),
            colorScheme = TimerColorPresets.MintLight,
            showKnob = true,
        )
    }
}

@ThemePreviews
@Composable
fun CircularTimerProgressCoralPreview() {
    FocusTimerTheme {
        CircularTimerProgress(
            progress = 0.45f,
            modifier = Modifier.size(200.dp),
            colorScheme = TimerColorPresets.CoralLight,
            showKnob = true,
        )
    }
}

@ThemePreviews
@Composable
fun CircularTimerProgressLavenderPreview() {
    FocusTimerTheme {
        CircularTimerProgress(
            progress = 0.80f,
            modifier = Modifier.size(200.dp),
            colorScheme = TimerColorPresets.LavenderLight,
            showKnob = true,
        )
    }
}

@ThemePreviews
@Composable
fun CircularTimerProgressSkyPreview() {
    FocusTimerTheme {
        CircularTimerProgress(
            progress = 0.30f,
            modifier = Modifier.size(200.dp),
            colorScheme = TimerColorPresets.SkyLight,
            showKnob = true,
        )
    }
}

@ThemePreviews
@Composable
fun CircularTimerProgressPeachPreview() {
    FocusTimerTheme {
        CircularTimerProgress(
            progress = 0.55f,
            modifier = Modifier.size(200.dp),
            colorScheme = TimerColorPresets.PeachLight,
            showKnob = true,
        )
    }
}

@ThemePreviews
@Composable
fun CircularTimerProgressSagePreview() {
    FocusTimerTheme {
        CircularTimerProgress(
            progress = 0.70f,
            modifier = Modifier.size(200.dp),
            colorScheme = TimerColorPresets.SageLight,
            showKnob = true,
        )
    }
}
