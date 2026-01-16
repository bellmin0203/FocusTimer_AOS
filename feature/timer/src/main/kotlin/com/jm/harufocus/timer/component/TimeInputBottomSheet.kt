package com.jm.harufocus.timer.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jm.harufocus.designsystem.component.ThemePreviews
import com.jm.harufocus.designsystem.icon.HaruFocusIcons
import com.jm.harufocus.designsystem.theme.HaruFocusTheme
import com.jm.harufocus.timer.R
import com.jm.harufocus.ui.component.Picker
import com.jm.harufocus.ui.component.PickerState
import com.jm.harufocus.ui.component.rememberPickerState
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// 시간 상수
private const val MAX_HOURS = 23
private const val MAX_MINUTES = 59
private const val MAX_SECONDS = 59

// 초기값
private const val DEFAULT_TIME_VALUE = "00"

// 숫자 범위
private const val DECIMAL_BASE = 10
private const val DIGIT_MODULO = 100

/**
 * 시간 필드 선택 상태
 */
private enum class TimeField {
    HOUR, MINUTE, SECOND
}

/**
 * 시간 입력을 위한 바텀시트
 *
 * 스크롤 가능한 NumberPicker 스타일로 시간:분:초를 선택할 수 있습니다.
 * 숫자 키패드로도 입력 가능합니다.
 *
 * @param onDismissRequest 바텀시트 닫기 콜백
 * @param onConfirm 시간 입력 확인 콜백 (밀리초 단위)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TimeInputBottomSheet(
    initialTime: Duration = 0.seconds,
    hourPickerState: PickerState = rememberPickerState(),
    minutePickerState: PickerState = rememberPickerState(),
    secondPickerState: PickerState = rememberPickerState(),
    onDismissRequest: () -> Unit,
    onConfirm: (Duration) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // 초기값 설정
    InitializePickerStates(
        initialTime = initialTime,
        hourPickerState = hourPickerState,
        minutePickerState = minutePickerState,
        secondPickerState = secondPickerState
    )

    // 현재 선택된 필드
    var selectedField by remember { mutableStateOf(TimeField.MINUTE) }

    // 편집 모드 여부 추적
    val isInEditMode = hourPickerState.isEditMode ||
            minutePickerState.isEditMode ||
            secondPickerState.isEditMode

    // 편집 모드 종료 함수
    val exitEditMode = {
        hourPickerState.isEditMode = false
        minutePickerState.isEditMode = false
        secondPickerState.isEditMode = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목
            Text(
                text = stringResource(R.string.time_input_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // NumberPicker 스타일 시간 디스플레이
            NumberPickerTimeDisplay(
                hourPickerState = hourPickerState,
                minutePickerState = minutePickerState,
                secondPickerState = secondPickerState,
                selectedField = selectedField,
                onFieldSelected = { field ->
                    selectedField = field
                    hourPickerState.isEditMode = true
                    minutePickerState.isEditMode = true
                    secondPickerState.isEditMode = true
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 숫자 키패드 및 확인 버튼
            TimeInputControls(
                isInEditMode = isInEditMode,
                selectedField = selectedField,
                hourPickerState = hourPickerState,
                minutePickerState = minutePickerState,
                secondPickerState = secondPickerState,
                onExitEditMode = exitEditMode,
                onConfirm = { timeInMillis ->
                    onConfirm(timeInMillis)
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }
            )
        }
    }
}

/**
 * Picker 상태 초기화
 */
@Composable
private fun InitializePickerStates(
    initialTime: Duration,
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState
) {
    LaunchedEffect(Unit) {
        initialTime.toComponents { hours, minutes, seconds, _ ->
            hourPickerState.selectedItem = hours.toString().padStart(2, '0')
            minutePickerState.selectedItem = minutes.toString().padStart(2, '0')
            secondPickerState.selectedItem = seconds.toString().padStart(2, '0')
        }
    }
}

/**
 * 시간 입력 컨트롤 (키패드 + 확인 버튼)
 */
@Composable
private fun TimeInputControls(
    isInEditMode: Boolean,
    selectedField: TimeField,
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState,
    onExitEditMode: () -> Unit,
    onConfirm: (Duration) -> Unit
) {
    Column {
        // 편집 모드일 때만 "설정" 버튼 표시
        if (isInEditMode) {
            FilledTonalButton(
                onClick = onExitEditMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.time_input_set_button),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 숫자 키패드
        NumericKeypad(
            onNumberClick = { number ->
                if (isInEditMode) {
                    handleNumberInput(
                        digit = number.toIntOrNull() ?: return@NumericKeypad,
                        selectedField = selectedField,
                        hourPickerState = hourPickerState,
                        minutePickerState = minutePickerState,
                        secondPickerState = secondPickerState,
                    )
                }
            },
            onClearClick = {
                clearAllFields(hourPickerState, minutePickerState, secondPickerState)
            },
            onBackspaceClick = {
                if (isInEditMode) {
                    handleBackspace(
                        selectedField = selectedField,
                        hourPickerState = hourPickerState,
                        minutePickerState = minutePickerState,
                        secondPickerState = secondPickerState
                    )
                }
            },
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 시작 버튼
        StartButton(
            hourPickerState = hourPickerState,
            minutePickerState = minutePickerState,
            secondPickerState = secondPickerState,
            onConfirm = onConfirm
        )
    }
}

/**
 * 숫자 입력 처리
 */
private fun handleNumberInput(
    digit: Int,
    selectedField: TimeField,
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState,
) {
    when (selectedField) {
        TimeField.HOUR -> {
            val newValue = calculateNewValue(
                currentValue = hourPickerState.selectedItem,
                digit = digit,
            )
            if (newValue in 0..MAX_HOURS) {
                hourPickerState.selectedItem = newValue.toString().padStart(2, '0')
            }
        }

        TimeField.MINUTE -> {
            val newValue = calculateNewValue(
                currentValue = minutePickerState.selectedItem,
                digit = digit,
            )
            if (newValue in 0..MAX_MINUTES) {
                minutePickerState.selectedItem = newValue.toString().padStart(2, '0')
            }
        }

        TimeField.SECOND -> {
            val newValue = calculateNewValue(
                currentValue = secondPickerState.selectedItem,
                digit = digit,
            )
            if (newValue in 0..MAX_SECONDS) {
                secondPickerState.selectedItem = newValue.toString().padStart(2, '0')
            }
        }
    }
}

/**
 * 새로운 값 계산
 */
private fun calculateNewValue(
    currentValue: String,
    digit: Int,
): Int {
    val current = currentValue.toIntOrNull() ?: 0
    return (current * DECIMAL_BASE + digit) % DIGIT_MODULO
}

/**
 * 모든 필드 초기화
 */
private fun clearAllFields(
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState
) {
    hourPickerState.selectedItem = DEFAULT_TIME_VALUE
    minutePickerState.selectedItem = DEFAULT_TIME_VALUE
    secondPickerState.selectedItem = DEFAULT_TIME_VALUE
}

/**
 * 백스페이스 처리
 */
private fun handleBackspace(
    selectedField: TimeField,
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState
) {
    when (selectedField) {
        TimeField.HOUR -> {
            val current = hourPickerState.selectedItem.toIntOrNull() ?: 0
            hourPickerState.selectedItem = (current / DECIMAL_BASE).toString().padStart(2, '0')
        }

        TimeField.MINUTE -> {
            val current = minutePickerState.selectedItem.toIntOrNull() ?: 0
            minutePickerState.selectedItem = (current / DECIMAL_BASE).toString().padStart(2, '0')
        }

        TimeField.SECOND -> {
            val current = secondPickerState.selectedItem.toIntOrNull() ?: 0
            secondPickerState.selectedItem = (current / DECIMAL_BASE).toString().padStart(2, '0')
        }
    }
}

/**
 * 시작 버튼
 */
@Composable
private fun StartButton(
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState,
    onConfirm: (Duration) -> Unit
) {
    val isTimeSet = hourPickerState.selectedItem != DEFAULT_TIME_VALUE ||
            minutePickerState.selectedItem != DEFAULT_TIME_VALUE ||
            secondPickerState.selectedItem != DEFAULT_TIME_VALUE

    FilledTonalButton(
        onClick = {
            val hours = hourPickerState.selectedItem.toIntOrNull() ?: 0
            val minutes = minutePickerState.selectedItem.toIntOrNull() ?: 0
            val seconds = secondPickerState.selectedItem.toIntOrNull() ?: 0
            val setTime = hours.hours + minutes.minutes + seconds.seconds
            if (setTime > 0.seconds) {
                onConfirm(setTime)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = isTimeSet
    ) {
        Text(
            text = stringResource(R.string.time_input_start_button),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * NumberPicker 스타일 시간 디스플레이
 *
 * 스크롤 가능한 3개의 NumberPicker로 시간을 선택합니다.
 */
@Composable
private fun NumberPickerTimeDisplay(
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState,
    selectedField: TimeField,
    onFieldSelected: (TimeField) -> Unit,
    modifier: Modifier = Modifier
) {
    // 아이템 리스트 생성
    val hourItems = remember { (0..MAX_HOURS).map { it.toString().padStart(2, '0') } }
    val minuteItems = remember { (0..MAX_MINUTES).map { it.toString().padStart(2, '0') } }
    val secondItems = remember { (0..MAX_SECONDS).map { it.toString().padStart(2, '0') } }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 라벨 (시간/분/초)
        TimeFieldLabels(selectedField = selectedField)

        Spacer(modifier = Modifier.height(16.dp))

        // Picker 컬럼들
        TimeFieldPickers(
            hourItems = hourItems,
            minuteItems = minuteItems,
            secondItems = secondItems,
            hourPickerState = hourPickerState,
            minutePickerState = minutePickerState,
            secondPickerState = secondPickerState,
            onFieldSelected = onFieldSelected
        )
    }
}

/**
 * 시간 필드 라벨 (시간/분/초)
 */
@Composable
private fun TimeFieldLabels(selectedField: TimeField) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeFieldLabel(
            text = stringResource(R.string.time_input_label_hour),
            isSelected = selectedField == TimeField.HOUR
        )
        Spacer(modifier = Modifier.width(16.dp))
        TimeFieldLabel(
            text = stringResource(R.string.time_input_label_minute),
            isSelected = selectedField == TimeField.MINUTE
        )
        Spacer(modifier = Modifier.width(16.dp))
        TimeFieldLabel(
            text = stringResource(R.string.time_input_label_second),
            isSelected = selectedField == TimeField.SECOND
        )
    }
}

/**
 * 단일 시간 필드 라벨
 */
@Composable
private fun TimeFieldLabel(
    text: String,
    isSelected: Boolean
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier.width(80.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

/**
 * 시간 필드 Picker들 (시:분:초)
 */
@Composable
private fun TimeFieldPickers(
    hourItems: List<String>,
    minuteItems: List<String>,
    secondItems: List<String>,
    hourPickerState: PickerState,
    minutePickerState: PickerState,
    secondPickerState: PickerState,
    onFieldSelected: (TimeField) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 시간 피커 (0-23)
        Picker(
            items = hourItems,
            state = hourPickerState,
            startIndex = 0,
            modifier = Modifier.width(80.dp),
            onSelected = { onFieldSelected(TimeField.HOUR) }
        )

        TimeSeparator()

        // 분 피커 (0-59)
        Picker(
            items = minuteItems,
            state = minutePickerState,
            startIndex = 0,
            modifier = Modifier.width(80.dp),
            onSelected = { onFieldSelected(TimeField.MINUTE) }
        )

        TimeSeparator()

        // 초 피커 (0-59)
        Picker(
            items = secondItems,
            state = secondPickerState,
            startIndex = 0,
            modifier = Modifier.width(80.dp),
            onSelected = { onFieldSelected(TimeField.SECOND) }
        )
    }
}

/**
 * 시간 구분자 (:)
 */
@Composable
private fun TimeSeparator() {
    Text(
        text = ":",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

/**
 * 숫자 키패드 컴포넌트
 * 0-9 숫자와 Clear, Backspace 버튼을 제공합니다.
 */
@Composable
private fun NumericKeypad(
    onNumberClick: (String) -> Unit,
    onClearClick: () -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 첫 번째 행: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                text = "1",
                onClick = { onNumberClick("1") },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "2",
                onClick = { onNumberClick("2") },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "3",
                onClick = { onNumberClick("3") },
                modifier = Modifier.weight(1f)
            )
        }

        // 두 번째 행: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                text = "4",
                onClick = { onNumberClick("4") },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "5",
                onClick = { onNumberClick("5") },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "6",
                onClick = { onNumberClick("6") },
                modifier = Modifier.weight(1f)
            )
        }

        // 세 번째 행: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadButton(
                text = "7",
                onClick = { onNumberClick("7") },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "8",
                onClick = { onNumberClick("8") },
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "9",
                onClick = { onNumberClick("9") },
                modifier = Modifier.weight(1f)
            )
        }

        // 네 번째 행: Clear, 0, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KeypadIconButton(
                icon = HaruFocusIcons.Refresh,
                contentDescription = stringResource(R.string.content_description_clear),
                onClick = onClearClick,
                modifier = Modifier.weight(1f)
            )
            KeypadButton(
                text = "0",
                onClick = { onNumberClick("0") },
                modifier = Modifier.weight(1f)
            )
            KeypadIconButton(
                icon = HaruFocusIcons.Backspace,
                contentDescription = stringResource(R.string.content_description_backspace),
                onClick = onBackspaceClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 키패드 버튼 (숫자)
 */
@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 키패드 아이콘 버튼 (Clear, Backspace)
 */
@Composable
private fun KeypadIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}



@ThemePreviews
@Composable
private fun TimeInputBottomSheetPreview() {
    HaruFocusTheme {
        TimeInputBottomSheet(
            onDismissRequest = {},
            onConfirm = {}
        )
    }
}