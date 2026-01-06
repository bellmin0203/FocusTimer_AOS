package com.jm.teumtimer.domain.repository

import com.jm.teumtimer.domain.model.preset.Preset
import kotlinx.coroutines.flow.Flow

/**
 * 프리셋 데이터 관리를 위한 Repository 인터페이스
 */
interface PresetRepository {

    /**
     * 새로운 프리셋을 추가합니다.
     * @param preset 추가할 프리셋
     * @return 생성된 프리셋의 ID
     */
    suspend fun insertPreset(preset: Preset): Long

    /**
     * 여러 프리셋을 한번에 추가합니다.
     * @param presets 추가할 프리셋들
     */
    suspend fun insertPresets(presets: List<Preset>)

    /**
     * 프리셋을 업데이트합니다.
     * @param preset 업데이트할 프리셋
     */
    suspend fun updatePreset(preset: Preset)

    /**
     * 프리셋을 삭제합니다.
     * @param preset 삭제할 프리셋
     */
    suspend fun deletePreset(preset: Preset)

    /**
     * ID로 특정 프리셋을 조회합니다.
     * @param id 조회할 프리셋 ID
     * @return 프리셋 (없으면 null)
     */
    suspend fun getPresetById(id: Int): Preset?

    /**
     * 모든 프리셋을 조회합니다 (최신순).
     * @return 모든 프리셋의 Flow
     */
    fun getAllPresets(): Flow<List<Preset>>

    /**
     * 총 프리셋 수를 조회합니다.
     * @return 전체 프리셋 개수
     */
    suspend fun getTotalPresetCount(): Int

    /**
     * 모든 프리셋을 삭제합니다.
     */
    suspend fun deleteAllPresets()
}
