package com.jm.focustimer.timer.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jm.focustimer.designsystem.component.ThemePreviews
import com.jm.focustimer.designsystem.theme.FocusTimerTheme
import com.jm.focustimer.domain.model.Preset
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * 프리셋 추가 다이얼로그
 *
 * @param currentTime 현재 타이머 시간 (표시용)
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onConfirm 확인 버튼 클릭 콜백 (프리셋 이름)
 */
@Composable
fun AddPresetDialog(
    currentTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var presetName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "프리셋 저장")
        },
        text = {
            Column {
                Text(
                    text = "현재 시간: $currentTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = presetName,
                    onValueChange = {
                        presetName = it
                        isError = false
                    },
                    label = { Text("프리셋 이름") },
                    placeholder = { Text("예: 집중 시간") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("프리셋 이름을 입력해주세요 (최대 20자)") }
                    } else {
                        { Text("${presetName.length}/20") }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (presetName.isNotBlank() && presetName.length <= 20) {
                                onConfirm(presetName.trim())
                            } else {
                                isError = true
                            }
                        }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (presetName.isNotBlank() && presetName.length <= 20) {
                        onConfirm(presetName.trim())
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

/**
 * 프리셋 수정 다이얼로그
 *
 * @param preset 수정할 프리셋
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onConfirm 확인 버튼 클릭 콜백 (새 이름)
 */
@Composable
fun EditPresetDialog(
    preset: Preset,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var presetName by remember { mutableStateOf(preset.name) }
    var isError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "프리셋 수정")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = {
                        presetName = it
                        isError = false
                    },
                    label = { Text("프리셋 이름") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("프리셋 이름을 입력해주세요 (최대 20자)") }
                    } else {
                        { Text("${presetName.length}/20") }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (presetName.isNotBlank() && presetName.length <= 20) {
                                onConfirm(presetName.trim())
                            } else {
                                isError = true
                            }
                        }
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (presetName.isNotBlank() && presetName.length <= 20) {
                        onConfirm(presetName.trim())
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("수정")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
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
            Text(text = "프리셋 삭제")
        },
        text = {
            Text(text = "'$presetName' 프리셋을 삭제하시겠습니까?")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    "삭제",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
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
            currentTime = "25:00",
            onDismiss = {},
            onConfirm = {}
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
            onConfirm = {}
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
