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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jm.harufocus.stats.R

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
    color: androidx.compose.ui.graphics.Color,
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
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = if (isPartial) 2.dp else 0.dp,
                    color = if (isPartial) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
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
