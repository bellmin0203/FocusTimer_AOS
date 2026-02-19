package com.jm.harufocus.domain.usecase.preset

import com.jm.harufocus.domain.model.preset.Preset
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration


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

    fun selectPreset(
        presetId: Int,
        presets: List<Preset>,
        isTimerActive: Boolean
    ): Result<Preset> {
        if (isTimerActive) {
            return Result.failure(
                PresetException.TimerRunning()
            )
        }

        val preset = presets.find { it.id == presetId }
            ?: return Result.failure(
                PresetException.NotFound()
            )

        return Result.success(preset)
    }

    suspend fun addPreset(
        name: String,
        duration: Duration,
        colorIndex: Int
    ): Result<Long> {
        return addPresetUseCase(name, duration, colorIndex)
    }

    suspend fun updatePreset(preset: Preset): Result<Unit> {
        return try {
            updatePresetUseCase(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(PresetException.FailUpdate())
        }
    }

    suspend fun deletePreset(preset: Preset): Result<Unit> {
        return try {
            deletePresetUseCase(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(PresetException.FailDelete())
        }
    }
}
