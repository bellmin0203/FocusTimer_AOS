package com.jm.focustimer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme

/**
 * Focus Time 막대 차트
 */
@Composable
fun FocusTimeBarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    chartHeight: androidx.compose.ui.unit.Dp = 180.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            ChartBar(
                label = item.label,
                value = item.value,
                isSelected = item.isSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 차트 막대 하나
 */
@Composable
private fun ChartBar(
    label: String,
    value: Float,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 막대
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(value.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }

        // 라벨
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@ThemePreviews
@Composable
fun FocusTimeBarChartPreview() {
    val data = listOf(
        ChartData(label = "Mon", value = 0.5f),
        ChartData(label = "Tue", value = 0.8f, isSelected = true),
        ChartData(label = "Wed", value = 0.3f),
        ChartData(label = "Thu", value = 1.0f),
        ChartData(label = "Fri", value = 0.2f),
        ChartData(label = "Sat", value = 0.6f),
        ChartData(label = "Sun", value = 0.4f)
    )
    FocusTimerTheme {
        FocusTimeBarChart(data = data)
    }
}

@ThemePreviews
@Composable
private fun ChartBarPreview() {
    FocusTimerTheme {
        ChartBar(
            label = "Mon",
            value = 0.75f,
            isSelected = false
        )
    }
}

@ThemePreviews
@Composable
private fun ChartBarSelectedPreview() {
    FocusTimerTheme {
        ChartBar(
            label = "Tue",
            value = 0.9f,
            isSelected = true
        )
    }
}

/**
 * 차트 데이터 클래스
 */
data class ChartData(
    val label: String,        // "Mon", "Tue", ...
    val value: Float,         // 0.0 ~ 1.0
    val isSelected: Boolean = false
)
