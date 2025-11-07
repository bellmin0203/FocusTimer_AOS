package com.jm.focustimer.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.FocusSecondaryButton
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 빠른 시간 설정 버튼 그룹
 *
 * 미리 정의된 시간(5, 15, 25, 30, 45, 60분)으로 타이머를 빠르게 설정할 수 있는 버튼들을 제공합니다.
 *
 * @param onTimeSelected 시간이 선택되었을 때 호출되는 콜백 (밀리초 단위)
 * @param modifier Modifier
 * @param enabled 버튼들의 활성화 상태
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickTimeButtons(
    onTimeSelected: (Duration) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 미리 정의된 시간 목록 (분 단위)
    val quickTimes = listOf(5, 15, 25, 30, 45, 60)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick Time",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickTimes.forEach { time ->
                QuickTimeButton(
                    minutes = time,
                    onClick = {
                        onTimeSelected(time.minutes)
                    },
                    enabled = enabled
                )
            }
        }
    }
}

/**
 * 개별 빠른 시간 설정 버튼
 *
 * @param minutes 분 단위 시간
 * @param onClick 클릭 시 호출되는 콜백
 * @param enabled 버튼 활성화 상태
 */
@Composable
private fun QuickTimeButton(
    minutes: Int,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    FocusSecondaryButton(
        onClick = onClick,
        text = "${minutes}분",
        enabled = enabled
    )
}

@ThemePreviews
@Composable
fun QuickTimeButtonsPreview() {
    FocusTimerTheme {
        QuickTimeButtons(
            onTimeSelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
