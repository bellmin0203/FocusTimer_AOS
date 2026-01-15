package com.jm.harufocus.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.theme.FocusTimerTheme
import com.jm.harufocus.ui.R

/**
 * 하루 몰입 Alert Dialog
 */
@Composable
fun FocusAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String? = null,
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = if (text != null) {
            {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else null,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        modifier = modifier
    )
}

/**
 * 확인/취소 다이얼로그
 */
@Composable
fun FocusTimerAlertDialog(
    modifier: Modifier = Modifier,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmText: String = stringResource(R.string.action_confirm),
    dismissText: String = stringResource(R.string.action_cancel)
) {
    FocusAlertDialog(
        modifier = modifier,
        title = title,
        text = text,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = dismissText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

/**
 * FocusAlertDialog Preview - 확인 버튼만 있는 경우
 */
@ThemePreviews
@Composable
private fun FocusAlertDialogPreview() {
    FocusTimerTheme {
        FocusAlertDialog(
            onDismissRequest = {},
            title = "알림",
            text = "작업이 완료되었습니다.",
            confirmButton = {
                TextButton(onClick = {}) {
                    Text(
                        text = "확인",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}

/**
 * FocusAlertDialog Preview - 확인/취소 버튼이 있는 경우
 */
@ThemePreviews
@Composable
private fun FocusAlertDialogWithDismissPreview() {
    FocusTimerTheme {
        FocusAlertDialog(
            onDismissRequest = {},
            title = "경고",
            text = "정말로 삭제하시겠습니까?",
            confirmButton = {
                TextButton(onClick = {}) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text(
                        text = "취소",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

/**
 * FocusConfirmDialog Preview
 */
@ThemePreviews
@Composable
private fun FocusConfirmDialogPreview() {
    FocusTimerTheme {
        FocusTimerAlertDialog(
            onDismissRequest = {},
            onConfirm = {},
            title = "세션 종료",
            text = "현재 진행 중인 세션을 종료하시겠습니까?\n진행 상황이 저장됩니다."
        )
    }
}

/**
 * FocusConfirmDialog Preview - 커스텀 버튼 텍스트
 */
@ThemePreviews
@Composable
private fun FocusConfirmDialogCustomTextPreview() {
    FocusTimerTheme {
        FocusTimerAlertDialog(
            onDismissRequest = {},
            onConfirm = {},
            title = "타이머 리셋",
            text = "모든 진행 상황이 초기화됩니다.",
            confirmText = "리셋",
            dismissText = "닫기"
        )
    }
}
