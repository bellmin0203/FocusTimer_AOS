package com.jm.teumtimer.timer.model

import com.jm.teumtimer.timer.R
import com.jm.teumtimer.ui.util.UiText


sealed class TimerError {
    data class SetTime(val code: SetTimeError) : TimerError()
    data class Preset(val code: PresetError, val args: List<String> = emptyList()) : TimerError()
    data class Session(val code: SessionError, val args: List<String> = emptyList()) : TimerError()
    data class Run(val message: String? = null) : TimerError() // 타이머 실행 관련 에러
    data class Overtime(val message: String? = null) :
        TimerError() // 초과 시간 추적 관련 에러
}

// SetTimeError - 시간 설정 관련 에러, 각 에러마다 메시지 리소스 ID 부여
enum class SetTimeError(val messageResId: Int) {
    /** 타이머 실행 중 시간 변경 불가 */
    TimerRunning(R.string.timer_error_set_time_running),

    /** 0 이하의 잘못된 시간 입력 */
    InvalidTime(R.string.timer_error_set_time_invalid),
}

// PresetError - 프리셋 관련 에러, 각 에러에 메시지 리소스 ID 부여
enum class PresetError(val messageResId: Int) {
    /** 타이머 실행 중에는 프리셋 변경 불가 */
    TimerRunning(R.string.timer_error_preset_running),

    /** 프리셋을 찾을 수 없음 */
    NotFound(R.string.timer_error_preset_not_found),

    /** 시간이 없는 상태에서 프리셋 저장 시도 */
    NoTime(R.string.timer_error_preset_no_time),

    /** 최대 프리셋 개수 초과 (args[0] = maxCount) */
    MaxCount(R.string.timer_error_preset_max_count),

    /** 이름이 비어있음 */
    InvalidName(R.string.preset_dialog_preset_name_blank_error),

    /** 이름 길이 초과 (args[0] = maxLength) */
    NameTooLong(R.string.preset_dialog_preset_name_length_error),

    /** 프리셋 저장 실패 */
    FailSave(R.string.timer_error_preset_fail_save),

    /** 프리셋 삭제 실패 */
    FailDelete(R.string.timer_error_preset_fail_delete),

    /** 프리셋 수정 실패 */
    FailUpdate(R.string.timer_error_preset_fail_update),
}

// SessionError - 세션 관련 에러
enum class SessionError(val messageResId: Int) {
    /** 세션을 찾을 수 없음 */
    NotFound(R.string.timer_error_session_not_found),
    
    /** 유효하지 않은 지속 시간 */
    InvalidDuration(R.string.timer_error_session_invalid_duration),
    
    /** 유효하지 않은 시간 범위 */
    InvalidTimeRange(R.string.timer_error_session_invalid_time_range),
    
    /** 시작 시간이 누락됨 */
    MissingStartTime(R.string.timer_error_session_missing_start_time),
    
    /** 세션 저장 실패 */
    FailSave(R.string.timer_error_session_fail_save),
    
    /** 세션 업데이트 실패 */
    FailUpdate(R.string.timer_error_session_fail_update),
}

fun TimerError.toUiText(): UiText {
    return when (this) {
        is TimerError.SetTime -> UiText.StringResource(this.code.messageResId)
        is TimerError.Preset -> {
            if (this.args.isNotEmpty()) {
                UiText.StringResource(this.code.messageResId, this.args)
            } else {
                UiText.StringResource(this.code.messageResId)
            }
        }
        is TimerError.Session -> {
            if (this.args.isNotEmpty()) {
                UiText.StringResource(this.code.messageResId, this.args)
            } else {
                UiText.StringResource(this.code.messageResId)
            }
        }

        is TimerError.Run -> {
            if (this.message.isNullOrBlank()) {
                UiText.StringResource(R.string.timer_error_run)
            } else {
                UiText.StringResource(R.string.timer_error_run_with_msg, listOf(this.message))
            }
        }

        is TimerError.Overtime -> {
            if (this.message.isNullOrBlank()) {
                UiText.StringResource(R.string.timer_error_overtime)
            } else {
                UiText.StringResource(R.string.timer_error_overtime_with_msg, listOf(this.message))
            }
        }
    }
}
