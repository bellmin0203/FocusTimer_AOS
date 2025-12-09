package com.jm.focustimer.timer.model

import android.annotation.SuppressLint
import androidx.compose.runtime.Immutable
import com.jm.focustimer.domain.model.preset.Preset
import java.time.Instant
import kotlin.time.Duration

/**
 * 타이머 화면의 UI 상태를 나타내는 불변 데이터 클래스
 *
 * @property remainingTime 현재 타이머 시간 (초 단위)
 * @property isRunning 타이머가 실행 중인지 여부
 * @property isPaused 타이머가 일시정지 상태인지 여부
 * @property isCompleted 타이머가 완료되었는지 여부
 * @property overtime 타이머 완료 후 경과한 초과 시간 (초 단위)
 * @property progress 타이머 진행률 (0.0 ~ 1.0)
 * @property error 에러 메시지 (에러가 없으면 null)
 * @property presets 저장된 프리셋 목록
 * @property selectedPresetId 현재 선택된 프리셋 ID (선택되지 않았으면 null)
 * @property initialTime 처음 설정된 타이머 시간 (완료 후 총 시간 계산용)
 * @property isScreenOnEnabled 화면 켜짐 유지 기능이 활성화되었는지 여부 (설정값)
 */
@Immutable
data class TimerUiState(
    val initialTime: Duration = Duration.ZERO,
    val remainingTime: Duration = Duration.ZERO,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val overtime: Duration = Duration.ZERO,
    val progress: Float = 0f,
    val error: String? = null,
    val presets: List<Preset> = emptyList(),
    val selectedPresetId: Int? = null,
    val isScreenOnEnabled: Boolean = false, // 화면 켜짐 유지 기능 활성화 여부
    val currentSessionId: Long? = null, // 현재 진행 중인 세션 ID
    val sessionStartTime: Instant? = null, // 세션 시작 시간
) {

    /**
     * 타이머가 유휴 상태인지 (시작되지 않은 상태)
     */
    val isIdle: Boolean
        get() = !isRunning && !isPaused && !isCompleted

    /**
     * 타이머가 활성 상태인지 (실행 중이거나 일시정지 상태)
     */
    val isActive: Boolean
        get() = isRunning || isPaused

    val isOvertime: Boolean = overtime > Duration.ZERO

    /**
     * 시간 형식으로 포맷팅 (HH:MM:SS)
     * 완료 상태일 때는 총 시간과 초과 시간을 함께 표시
     */
    val formattedTime: String
        get() = if (isCompleted && isOvertime) {
            // 타이머 완료 후: "총시간 (+초과시간)" 형식
            val overtimeFormatted = overtime.formatDuration()
            "${initialTime.formatDuration()} (+$overtimeFormatted)"
        } else {
            // 일반 상태: 남은 시간만 표시
            remainingTime.formatDuration()
        }

    /**
     * Duration을 HH:MM:SS 또는 MM:SS 형식의 문자열로 변환하는 내부 헬퍼 함수.
     */
    @SuppressLint("DefaultLocale")
    private fun Duration.formatDuration(): String {
        return toComponents { hours, minutes, seconds, _ ->
            if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }

    /**
     * 에러가 있는지 여부
     */
    val hasError: Boolean
        get() = error != null

    /**
     * 프리셋을 추가할 수 있는지 여부 (최대 5개 제한)
     */
    val canAddPreset: Boolean
        get() = presets.size < 5
}
