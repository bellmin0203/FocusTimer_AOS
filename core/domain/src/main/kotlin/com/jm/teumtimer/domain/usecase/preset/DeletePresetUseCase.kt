package com.jm.teumtimer.domain.usecase.preset

import com.jm.teumtimer.domain.model.preset.Preset
import com.jm.teumtimer.domain.repository.PresetRepository
import com.jm.teumtimer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 프리셋을 삭제하는 UseCase
 *
 * 기본 프리셋으로 설정된 경우 자동으로 기본 프리셋 설정을 해제합니다.
 */
@Singleton
class DeletePresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository,
    private val settingsRepository: SettingsRepository
) {
    /**
     * 프리셋을 삭제합니다.
     *
     * @param preset 삭제할 프리셋
     * @return 성공 시 Unit, 실패 시 에러
     */
    suspend operator fun invoke(preset: Preset): Result<Unit> {
        return try {
            // 기본 프리셋으로 설정된 경우 해제
            val defaultPresetId = settingsRepository.defaultPresetId.first()
            if (defaultPresetId == preset.id) {
                settingsRepository.updateDefaultPresetId(null)
            }

            presetRepository.deletePreset(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
