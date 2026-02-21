package com.jm.harufocus.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jm.harufocus.stats.R
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.time.Duration

data class CompletionRatio(
    val fullPercent: Int,
    val partialPercent: Int
)

fun calculateCompletionRatio(
    fullCompletedTime: Duration,
    partialCompletedTime: Duration
): CompletionRatio {
    val fullMinutes = fullCompletedTime.inWholeMinutes
    val partialMinutes = partialCompletedTime.inWholeMinutes
    val totalMinutes = fullMinutes + partialMinutes

    if (totalMinutes <= 0L) {
        return CompletionRatio(fullPercent = 0, partialPercent = 0)
    }

    val fullPercent = ((fullMinutes.toDouble() / totalMinutes.toDouble()) * 100.0)
        .roundToInt()
        .coerceIn(0, 100)
    val partialPercent = 100 - fullPercent
    return CompletionRatio(fullPercent = fullPercent, partialPercent = partialPercent)
}

@Composable
fun CompletionRatioBadge(
    ratio: CompletionRatio,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = stringResource(
                R.string.chart_ratio_badge_format,
                ratio.fullPercent,
                ratio.partialPercent
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ChartMeaningTooltip(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    autoDismissMillis: Long = 6000L
) {
    LaunchedEffect(Unit) {
        delay(autoDismissMillis)
        onDismiss()
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.chart_tooltip_message,
                        stringResource(R.string.chart_color_main_name),
                        stringResource(R.string.chart_color_sub_name)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TooltipLegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = stringResource(R.string.legend_full_completed)
                    )
                    TooltipLegendItem(
                        color = MaterialTheme.colorScheme.secondary,
                        label = stringResource(R.string.legend_partial)
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.content_description_close_tooltip),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TooltipLegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
