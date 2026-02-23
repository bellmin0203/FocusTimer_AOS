package com.jm.harufocus.stats.util

import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import kotlin.math.abs

/**
 * 차트 막대용 패턴 생성 유틸리티
 *
 * 색맹 사용자를 위한 접근성 개선 - 부분 완료 막대에 대각선 줄무늬 패턴 적용
 */
object ChartPatterns {

    private const val PATTERN_SIZE = 24
    private const val STRIPE_SPACING = 8
    private const val STROKE_WIDTH = 2.5f

    /**
     * 대각선 줄무늬 패턴을 생성합니다
     *
     * @param baseColor 기본 배경색
     * @param stripeColor 줄무늬 색상 (기본적으로 더 어두운 색)
     * @return BitmapShader 패턴
     */
    fun createDiagonalStripedPattern(
        baseColor: Color,
        stripeColor: Color = baseColor.copy(alpha = 0.3f)
    ): Shader {
        val resolvedStripeColor = resolveStripeColor(baseColor, stripeColor)
        val bitmap = createBitmap(PATTERN_SIZE, PATTERN_SIZE)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.withSave {
            // 배경 채우기
            paint.color = baseColor.toArgb()
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, PATTERN_SIZE.toFloat(), PATTERN_SIZE.toFloat(), paint)

            // 대각선 줄무늬 그리기
            paint.color = resolvedStripeColor.toArgb()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = STROKE_WIDTH

            // 왼쪽 아래에서 오른쪽 위로 가는 선들
            val path = android.graphics.Path()
            for (i in -PATTERN_SIZE..PATTERN_SIZE * 2 step STRIPE_SPACING) {
                path.moveTo(i.toFloat(), PATTERN_SIZE.toFloat())
                path.lineTo((i + PATTERN_SIZE).toFloat(), 0f)
            }
            canvas.drawPath(path, paint)
        }

        return BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private fun resolveStripeColor(baseColor: Color, stripeColor: Color): Color {
        val luminanceDiff = abs(baseColor.luminance() - stripeColor.luminance())
        if (luminanceDiff >= 0.2f) return stripeColor

        return if (baseColor.luminance() > 0.5f) {
            Color.Black.copy(alpha = 0.55f)
        } else {
            Color.White.copy(alpha = 0.55f)
        }
    }

    /**
     * 격자 패턴을 생성합니다
     *
     * @param baseColor 기본 배경색
     * @param gridColor 격자 색상
     * @return BitmapShader 패턴
     */
    fun createGridPattern(
        baseColor: Color,
        gridColor: Color = baseColor.copy(alpha = 0.4f)
    ): Shader {
        val bitmap = createBitmap(PATTERN_SIZE, PATTERN_SIZE)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.withSave {
            // 배경 채우기
            paint.color = baseColor.toArgb()
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, PATTERN_SIZE.toFloat(), PATTERN_SIZE.toFloat(), paint)

            // 격자선 그리기
            paint.color = gridColor.toArgb()
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = STROKE_WIDTH

            // 세로선
            for (x in 0..PATTERN_SIZE step 10) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), PATTERN_SIZE.toFloat(), paint)
            }
            // 가로선
            for (y in 0..PATTERN_SIZE step 10) {
                canvas.drawLine(0f, y.toFloat(), PATTERN_SIZE.toFloat(), y.toFloat(), paint)
            }
        }

        return BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    /**
     * 점 패턴을 생성합니다
     *
     * @param baseColor 기본 배경색
     * @param dotColor 점 색상
     * @return BitmapShader 패턴
     */
    fun createDotPattern(
        baseColor: Color,
        dotColor: Color = baseColor.copy(alpha = 0.5f)
    ): Shader {
        val bitmap = createBitmap(PATTERN_SIZE, PATTERN_SIZE)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.withSave {
            // 배경 채우기
            paint.color = baseColor.toArgb()
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, PATTERN_SIZE.toFloat(), PATTERN_SIZE.toFloat(), paint)

            // 점 그리기
            paint.color = dotColor.toArgb()
            paint.style = Paint.Style.FILL

            for (x in 5..PATTERN_SIZE step 10) {
                for (y in 5..PATTERN_SIZE step 10) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 1.5f, paint)
                }
            }
        }

        return BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }
}
