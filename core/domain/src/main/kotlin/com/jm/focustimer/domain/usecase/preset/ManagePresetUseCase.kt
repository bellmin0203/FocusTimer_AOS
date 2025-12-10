package com.jm.focustimer.domain.usecase.preset

import com.jm.focustimer.domain.model.preset.Preset
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * 프리셋 관리 전용 UseCase
 * 프리셋 선택, 추가, 수정, 삭제 로직을 캡슐화합니다
 */
class ManagePresetUseCase @Inject constructor(
    private val getAllPresetsUseCase: GetAllPresetsUseCase,
    private val addPresetUseCase: AddPresetUseCase,
    private val updatePresetUseCase: UpdatePresetUseCase,
    private val deletePresetUseCase: DeletePresetUseCase,
) {
    fun getAllPresets(): Flow<List<Preset>> =
        getAllPresetsUseCase()

    suspend fun selectPreset(
        presetId: Int,
        presets: List<Preset>,
        isTimerActive: Boolean
    ): Result<Preset> {
        if (isTimerActive) {
            return Result.failure(
                PresetException.TimerRunning("타이머 실행 중에는 프리셋을 변경할 수 없습니다")
            )
        }

        val preset = presets.find { it.id == presetId }
            ?: return Result.failure(
                PresetException.NotFound("프리셋을 찾을 수 없습니다")
            )

        return Result.success(preset)
    }

    suspend fun addPreset(
        name: String,
        duration: Duration,
        colorIndex: Int,
        currentPresetCount: Int
    ): Result<Long> {
        // 비즈니스 로직 검증
        if (duration <= 0.seconds) {
            return Result.failure(
                PresetException.InvalidDuration("시간이 설정되지 않았습니다")
            )
        }

        if (currentPresetCount >= MAX_PRESET_COUNT) {
            return Result.failure(
                PresetException.MaxCountExceeded("프리셋은 최대 ${MAX_PRESET_COUNT}개까지 저장할 수 있습니다")
            )
        }

        return addPresetUseCase(name, duration, colorIndex)
    }

    suspend fun updatePreset(preset: Preset): Result<Unit> {
        return try {
            updatePresetUseCase(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePreset(preset: Preset): Result<Unit> {
        return try {
            deletePresetUseCase(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val MAX_PRESET_COUNT = 5
    }
}

// 예외 클래스 정의 (타입 안전한 에러 처리)
sealed class PresetException(message: String) : Exception(message) {
    class TimerRunning(message: String) : PresetException(message)
    class NotFound(message: String) : PresetException(message)
    class InvalidDuration(message: String) : PresetException(message)
    class MaxCountExceeded(message: String) : PresetException(message)
}
