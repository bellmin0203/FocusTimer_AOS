package com.jm.harufocus.timer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jm.harufocus.timer.R

/**
 * 타이머 다이얼 중앙에 표시되는 시간 컴포넌트
 *
 * @param formattedTime 표시할 시간 문자열 (HH:MM:SS 형식)
 * @param isCompleted 타이머 완료 상태 여부
 * @param isTimerActive 타이머가 활성 상태인지 여부 (활성 상태면 클릭 비활성화)
 * @param onTimeClick 시간 영역 클릭 시 호출되는 콜백
 * @param modifier Modifier
 */
@Composable
fun TimerCenterContent(
    formattedTime: String,
    isCompleted: Boolean,
    isTimerActive: Boolean,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                color = if (isCompleted) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                },
            )
            .clickable(enabled = !isTimerActive, onClick = onTimeClick)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displaySmall,
            color = if (isCompleted) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )

        // 완료 상태일 때 안내 메시지 표시
        if (isCompleted) {
            Text(
                text = stringResource(R.string.timer_completed_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}
