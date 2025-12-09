package com.jm.focustimer.domain.usecase.preset

import com.jm.focustimer.domain.model.preset.Preset
import com.jm.focustimer.domain.repository.PresetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 모든 프리셋을 조회하는 UseCase
 */
@Singleton
class GetAllPresetsUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    /**
     * 모든 프리셋을 최신순으로 반환합니다.
     *
     * @return 프리셋 리스트의 Flow
     */
    operator fun invoke(): Flow<List<Preset>> {
        return presetRepository.getAllPresets()
    }
}
