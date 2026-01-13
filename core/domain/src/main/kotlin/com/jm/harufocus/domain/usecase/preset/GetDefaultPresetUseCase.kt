package com.jm.harufocus.domain.usecase.preset

import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.repository.PresetRepository
import com.jm.harufocus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 설정된 기본 프리셋을 가져오는 UseCase
 *
 * SettingsRepository에서 기본 프리셋 ID를 조회하고,
 * 해당 ID로 PresetRepository에서 실제 프리셋을 가져옵니다.
 */
@Singleton
class GetDefaultPresetUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val presetRepository: PresetRepository
) {
    /**
     * 기본 프리셋을 Flow로 반환합니다.
     * 기본 프리셋이 설정되지 않았거나 해당 프리셋이 삭제된 경우 null을 반환합니다.
     *
     * @return 기본 프리셋의 Flow (설정되지 않은 경우 null)
     */
    operator fun invoke(): Flow<Preset?> {
        return settingsRepository.defaultPresetId.flatMapLatest { presetId ->
            if (presetId == null) {
                flowOf(null)
            } else {
                // presetId를 사용하여 실제 Preset 조회
                flowOf(presetRepository.getPresetById(presetId))
            }
        }
    }
}