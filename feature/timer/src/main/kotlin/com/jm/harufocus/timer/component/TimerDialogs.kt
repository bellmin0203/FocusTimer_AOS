package com.jm.harufocus.timer.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.jm.harufocus.common.TimerServiceAction
import com.jm.harufocus.timer.model.TimerDialogState
import com.jm.harufocus.timer.model.TimerIntent
import com.jm.harufocus.timer.model.TimerUiState
import com.jm.harufocus.timer.util.calculateDurationFromPickerStates
import com.jm.harufocus.timer.util.sendTimerServiceAction
import com.jm.harufocus.ui.component.PickerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * 타이머 화면의 모든 다이얼로그/바텀시트를 통합 관리하는 컴포넌트
 *
 * @param dialogState 현재 다이얼로그 상태
 * @param uiState 타이머 UI 상태
 * @param presetSheetState 프리셋 바텀시트 상태
 * @param hourPickerState 시간 피커 상태
 * @param minutePickerState 분 피커 상태
 * @param secondPickerState 초 피커 상태
 * @param scope 코루틴 스코프
 * @param onIntent 타이머 인텐트 처리 함수
 * @param onDialogStateChange 다이얼로그 상태 변경 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerDialogs(
    dialogState: TimerDialogState,
    uiState: TimerUiState,
    presetSheetState: SheetState,
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState,
    scope: CoroutineScope,
    onIntent: (TimerIntent) -> Unit,
    onDialogStateChange: (TimerDialogState) -> Unit
) {
    val context = LocalContext.current

    when (dialogState) {
        TimerDialogState.None -> { /* 아무것도 표시하지 않음 */ }

        TimerDialogState.TimeInput -> {
            TimeInputBottomSheet(
                initialTime = uiState.remainingTime,
                hourPickerState = hourPickerState,
                minutePickerState = minutePickerState,
                secondPickerState = secondPickerState,
                onDismissRequest = {
                    val selectedTime = calculateDurationFromPickerStates(
                        hourState = hourPickerState,
                        minuteState = minutePickerState,
                        secondState = secondPickerState
                    )
                    if (selectedTime > 0.seconds) {
                        onIntent(TimerIntent.SetTime(selectedTime))
                    }
                    onDialogStateChange(TimerDialogState.None)
                },
                onConfirm = { time ->
                    onIntent(TimerIntent.SetTime(time))
                    onIntent(TimerIntent.Start())

                    // 서비스 시작
                    context.sendTimerServiceAction(
                        action = TimerServiceAction.ACTION_START,
                        durationMillis = time.inWholeMilliseconds
                    )

                    onDialogStateChange(TimerDialogState.None)
                }
            )
        }

        TimerDialogState.AddPreset -> {
            AddPresetDialog(
                initialMinutes = uiState.remainingTime.inWholeMinutes.toInt(),
                initialSeconds = (uiState.remainingTime.inWholeSeconds % 60).toInt(),
                initialColorIndex = 0,
                onDismiss = { onDialogStateChange(TimerDialogState.None) },
                onConfirm = { name, minutes, seconds, colorIndex ->
                    val duration = minutes.minutes + seconds.seconds
                    onIntent(TimerIntent.SaveAsPreset(name, duration, colorIndex))
                    onDialogStateChange(TimerDialogState.None)
                }
            )
        }

        is TimerDialogState.EditPreset -> {
            EditPresetDialog(
                preset = dialogState.preset,
                onDismiss = { onDialogStateChange(TimerDialogState.None) },
                onConfirm = { name, minutes, seconds, colorIndex ->
                    val duration = minutes.minutes + seconds.seconds
                    val updatedPreset = dialogState.preset.copy(
                        name = name,
                        duration = duration,
                        colorIndex = colorIndex
                    )
                    onIntent(TimerIntent.UpdatePreset(updatedPreset))
                    onDialogStateChange(TimerDialogState.None)
                }
            )
        }

        is TimerDialogState.DeletePreset -> {
            DeletePresetDialog(
                presetName = dialogState.preset.name,
                onDismiss = { onDialogStateChange(TimerDialogState.None) },
                onConfirm = {
                    onIntent(TimerIntent.DeletePreset(dialogState.preset.id))
                    onDialogStateChange(TimerDialogState.None)
                }
            )
        }

        TimerDialogState.PresetManagement -> {
            PresetManagementBottomSheet(
                sheetState = presetSheetState,
                presets = uiState.presets,
                canAddPreset = uiState.hasPresetSpaceAvailable,
                onDismiss = {
                    scope.launch {
                        presetSheetState.hide()
                        onDialogStateChange(TimerDialogState.None)
                    }
                },
                onPresetClick = { presetId ->
                    onIntent(TimerIntent.SelectPreset(presetId))
                    scope.launch {
                        presetSheetState.hide()
                        onDialogStateChange(TimerDialogState.None)
                    }
                },
                onAddPreset = {
                    onDialogStateChange(TimerDialogState.AddPreset)
                },
                onEditPreset = { preset ->
                    onDialogStateChange(TimerDialogState.EditPreset(preset))
                },
                onDeletePreset = { presetId ->
                    onIntent(TimerIntent.DeletePreset(presetId))
                }
            )
        }
    }
}
