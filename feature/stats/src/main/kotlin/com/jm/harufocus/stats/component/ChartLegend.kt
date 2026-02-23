package com.jm.harufocus.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jm.harufocus.stats.R
import com.jm.harufocus.stats.util.ChartPatterns

/**
 * 차트 범례 컴포넌트
 * 완전 완료와 부분 완료를 색상으로 구분하여 표시
 *
 * @param modifier Modifier
 */
@Composable
fun ChartLegend(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 완전 완료 항목
        LegendItem(
            color = MaterialTheme.colorScheme.primary,
            label = stringResource(R.string.legend_full_completed),
            isPartial = false
        )

        Spacer(modifier = Modifier.width(24.dp))

        // 부분 완료 항목
        LegendItem(
            color = MaterialTheme.colorScheme.secondary,
            label = stringResource(R.string.legend_partial),
            isPartial = true
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isPartial: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 색상 표시 박스
        Box(
            modifier = Modifier
                .size(16.dp)
                .then(
                    if (isPartial) {
                        Modifier.partialPatternBackground(color)
                    } else {
                        Modifier.background(
                            color = color,
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                )
                .border(
                    width = if (isPartial) 1.dp else 0.dp,
                    color = if (isPartial) color else Color.Transparent,
                    shape = RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 라벨 텍스트
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun Modifier.partialPatternBackground(baseColor: Color): Modifier =
    this.drawWithCache {
        val cornerRadius = 4.dp.toPx()
        val shader = ChartPatterns.createDiagonalStripedPattern(
            baseColor = baseColor.copy(alpha = 0.9f),
            stripeColor = baseColor.copy(alpha = 0.45f)
        )
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            this.shader = shader
        }

        onDrawBehind {
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawRoundRect(
                    0f,
                    0f,
                    size.width,
                    size.height,
                    cornerRadius,
                    cornerRadius,
                    paint
                )
            }
        }
    }
