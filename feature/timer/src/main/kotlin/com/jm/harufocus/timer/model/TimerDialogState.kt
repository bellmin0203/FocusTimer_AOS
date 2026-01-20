package com.jm.harufocus.timer.model

import com.jm.harufocus.domain.model.preset.Preset

/**
 * 타이머 화면의 다이얼로그/바텀시트 상태를 관리하는 sealed interface
 *
 * 한 번에 하나의 다이얼로그/바텀시트만 표시되도록 보장합니다.
 */
sealed interface TimerDialogState {
    /** 다이얼로그/바텀시트가 표시되지 않는 상태 */
    data object None : TimerDialogState

    /** 프리셋 관리 바텀시트 표시 */
    data object PresetManagement : TimerDialogState

    /** 시간 입력 바텀시트 표시 */
    data object TimeInput : TimerDialogState

    /** 프리셋 추가 다이얼로그 표시 */
    data object AddPreset : TimerDialogState

    /** 프리셋 수정 다이얼로그 표시 */
    data class EditPreset(val preset: Preset) : TimerDialogState

    /** 프리셋 삭제 확인 다이얼로그 표시 */
    data class DeletePreset(val preset: Preset) : TimerDialogState
}
