package com.jm.teumtimer.timer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jm.teumtimer.designsystem.component.ThemePreviews
import com.jm.teumtimer.designsystem.component.TimerColorPresets
import com.jm.teumtimer.designsystem.icon.FocusTimerIcons
import com.jm.teumtimer.designsystem.theme.FocusTimerTheme
import com.jm.teumtimer.domain.model.preset.Preset
import com.jm.teumtimer.domain.usecase.preset.AddPresetUseCase
import com.jm.teumtimer.timer.R
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * 프리셋 추가 다이얼로그
 *
 * @param initialMinutes 초기 분 값
 * @param initialSeconds 초기 초 값
 * @param initialColorIndex 초기 컬러 인덱스
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onConfirm 확인 버튼 클릭 콜백 (프리셋 이름, 분, 초, 컬러 인덱스)
 */
@Composable
fun AddPresetDialog(
    initialMinutes: Int = 25,
    initialSeconds: Int = 0,
    initialColorIndex: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (name: String, minutes: Int, seconds: Int, colorIndex: Int) -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var minutes by remember { mutableIntStateOf(initialMinutes) }
    var seconds by remember { mutableIntStateOf(initialSeconds) }
    var selectedColorIndex by remember { mutableIntStateOf(initialColorIndex) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.add_preset_dialog_title))
        },
        text = {
            Column {
                // 프리셋 이름 입력
                OutlinedTextField(
                    value = presetName,
                    onValueChange = {
                        if (it.length <= 20) {
                            presetName = it
                            isError = false
                        }
                    },
                    label = { Text(stringResource(R.string.preset_dialog_preset_name_label)) },
                    placeholder = { Text(stringResource(R.string.preset_dialog_preset_name_placeholder)) },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(errorMessage) }
                    } else {
                        { Text("${presetName.length}/${AddPresetUseCase.MAX_NAME_LENGTH}") }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 시간 설정
                Text(
                    text = stringResource(R.string.preset_dialog_set_time_text),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 분 입력
                    OutlinedTextField(
                        value = if (minutes == 0) "" else minutes.toString(),
                        onValueChange = { value ->
                            val newMinutes = value.toIntOrNull()
                            if (newMinutes != null && newMinutes in 0..999) {
                                minutes = newMinutes
                            } else if (value.isEmpty()) {
                                minutes = 0
                            }
                        },
                        label = { Text(stringResource(R.string.preset_dialog_time_minutes_label)) },
                        suffix = { Text(stringResource(R.string.preset_dialog_time_minutes_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // 초 입력
                    OutlinedTextField(
                        value = if (seconds == 0) "" else seconds.toString(),
                        onValueChange = { value ->
                            val newSeconds = value.toIntOrNull()
                            if (newSeconds != null && newSeconds in 0..59) {
                                seconds = newSeconds
                            } else if (value.isEmpty()) {
                                seconds = 0
                            }
                        },
                        label = { Text(stringResource(R.string.preset_dialog_time_seconds_label)) },
                        suffix = { Text(stringResource(R.string.preset_dialog_time_seconds_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 컬러 선택
                Text(
                    text = stringResource(R.string.preset_dialog_timer_color_text),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                ColorSelector(
                    selectedIndex = selectedColorIndex,
                    onColorSelected = { selectedColorIndex = it }
                )
            }
        },
        confirmButton = {
            val blankNameErrorMessage =
                stringResource(R.string.preset_dialog_preset_name_blank_error)
            val nameLengthErrorMessage =
                stringResource(R.string.preset_dialog_preset_name_length_error)
            val timeErrorMessage = stringResource(R.string.preset_dialog_preset_time_error)

            TextButton(
                onClick = {
                    when {
                        presetName.isBlank() -> {
                            isError = true
                            errorMessage = blankNameErrorMessage
                        }

                        presetName.length > 20 -> {
                            isError = true
                            errorMessage = nameLengthErrorMessage
                        }

                        minutes == 0 && seconds == 0 -> {
                            isError = true
                            errorMessage = timeErrorMessage
                        }

                        else -> {
                            onConfirm(presetName.trim(), minutes, seconds, selectedColorIndex)
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.add_preset_dialog_save_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.preset_dialog_cancel_text))
            }
        }
    )
}

/**
 * 컬러 선택기
 */
@Composable
private fun ColorSelector(
    selectedIndex: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetColors = TimerColorPresets.presetColors

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(presetColors.size) { index ->
            ColorOption(
                color = TimerColorPresets.presetColors[index].progressColor,
                isSelected = index == selectedIndex,
                onClick = { onColorSelected(index) }
            )
        }
    }
}

/**
 * 컬러 옵션 (개별 컬러 원)
 */
@Composable
private fun ColorOption(
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = FocusTimerIcons.Check,
                contentDescription = stringResource(R.string.content_description_selected),
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 프리셋 수정 다이얼로그
 *
 * @param preset 수정할 프리셋
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onConfirm 확인 버튼 클릭 콜백 (새 이름, 분, 초, 컬러 인덱스)
 */
@Composable
fun EditPresetDialog(
    preset: Preset,
    onDismiss: () -> Unit,
    onConfirm: (name: String, minutes: Int, seconds: Int, colorIndex: Int) -> Unit
) {
    var presetName by remember { mutableStateOf(preset.name) }
    var minutes by remember { mutableIntStateOf(preset.duration.inWholeMinutes.toInt()) }
    var seconds by remember { mutableIntStateOf((preset.duration.inWholeSeconds % 60).toInt()) }
    var selectedColorIndex by remember { mutableIntStateOf(preset.colorIndex) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.edit_preset_dialog_title))
        },
        text = {
            Column {
                // 프리셋 이름 입력
                OutlinedTextField(
                    value = presetName,
                    onValueChange = {
                        if (it.length <= 20) {
                            presetName = it
                            isError = false
                        }
                    },
                    label = { Text(stringResource(R.string.preset_dialog_preset_name_label)) },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(errorMessage) }
                    } else {
                        { Text("${presetName.length}/${AddPresetUseCase.MAX_NAME_LENGTH}") }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 시간 설정
                Text(
                    text = stringResource(R.string.preset_dialog_set_time_text),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 분 입력
                    OutlinedTextField(
                        value = if (minutes == 0) "" else minutes.toString(),
                        onValueChange = { value ->
                            val newMinutes = value.toIntOrNull()
                            if (newMinutes != null && newMinutes in 0..59) {
                                minutes = newMinutes
                            } else if (value.isEmpty()) {
                                minutes = 0
                            }
                        },
                        label = { Text(stringResource(R.string.preset_dialog_time_minutes_label)) },
                        suffix = { Text(stringResource(R.string.preset_dialog_time_minutes_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // 초 입력
                    OutlinedTextField(
                        value = if (seconds == 0) "" else seconds.toString(),
                        onValueChange = { value ->
                            val newSeconds = value.toIntOrNull()
                            if (newSeconds != null && newSeconds in 0..59) {
                                seconds = newSeconds
                            } else if (value.isEmpty()) {
                                seconds = 0
                            }
                        },
                        label = { Text(stringResource(R.string.preset_dialog_time_seconds_label)) },
                        suffix = { Text(stringResource(R.string.preset_dialog_time_seconds_label)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 컬러 선택
                Text(
                    text = stringResource(R.string.preset_dialog_timer_color_text),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                ColorSelector(
                    selectedIndex = selectedColorIndex,
                    onColorSelected = { selectedColorIndex = it }
                )
            }
        },
        confirmButton = {
            val blankNameErrorMessage =
                stringResource(R.string.preset_dialog_preset_name_blank_error)
            val nameLengthErrorMessage =
                stringResource(R.string.preset_dialog_preset_name_length_error)
            val timeErrorMessage = stringResource(R.string.preset_dialog_preset_time_error)

            TextButton(
                onClick = {
                    when {
                        presetName.isBlank() -> {
                            isError = true
                            errorMessage = blankNameErrorMessage
                        }

                        presetName.length > 20 -> {
                            isError = true
                            errorMessage = nameLengthErrorMessage
                        }

                        minutes == 0 && seconds == 0 -> {
                            isError = true
                            errorMessage = timeErrorMessage
                        }

                        else -> {
                            onConfirm(presetName.trim(), minutes, seconds, selectedColorIndex)
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.preset_dialog_modify_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.preset_dialog_cancel_text))
            }
        }
    )
}

/**
 * 프리셋 삭제 확인 다이얼로그
 *
 * @param presetName 삭제할 프리셋 이름
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onConfirm 확인 버튼 클릭 콜백
 */
@Composable
fun DeletePresetDialog(
    presetName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_preset_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.delete_preset_dialog_message, presetName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    stringResource(R.string.preset_dialog_delete_text),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.preset_dialog_cancel_text))
            }
        }
    )
}

/**
 * 프리셋 추가 다이얼로그 프리뷰
 */
@ThemePreviews
@Composable
private fun AddPresetDialogPreview() {
    FocusTimerTheme {
        AddPresetDialog(
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}

/**
 * 프리셋 수정 다이얼로그 프리뷰
 */
@ThemePreviews
@Composable
private fun EditPresetDialogPreview() {
    FocusTimerTheme {
        EditPresetDialog(
            preset = Preset(
                id = 1,
                name = "집중 시간",
                duration = 25.minutes,
                createdAt = Instant.now()
            ),
            onDismiss = {},
            onConfirm = { _, _, _, _ -> }
        )
    }
}

/**
 * 프리셋 삭제 확인 다이얼로그 프리뷰
 */
@ThemePreviews
@Composable
private fun DeletePresetDialogPreview() {
    FocusTimerTheme {
        DeletePresetDialog(
            presetName = "집중 시간",
            onDismiss = {},
            onConfirm = {}
        )
    }
}
