package com.jm.harufocus.timer.util

import com.jm.harufocus.ui.component.PickerState
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * PickerState들로부터 Duration을 계산합니다.
 *
 * @param hourState 시간 PickerState
 * @param minuteState 분 PickerState
 * @param secondState 초 PickerState
 * @return 계산된 Duration (유효하지 않은 값은 0으로 처리)
 */
fun calculateDurationFromPickerStates(
    hourState: PickerState,
    minuteState: PickerState,
    secondState: PickerState
): Duration {
    val hours = hourState.selectedItem.toIntOrNull() ?: 0
    val minutes = minuteState.selectedItem.toIntOrNull() ?: 0
    val seconds = secondState.selectedItem.toIntOrNull() ?: 0

    return hours.hours + minutes.minutes + seconds.seconds
}
