package com.jm.focustimer.data.repository

import com.jm.focustimer.core.database.dao.PresetDao
import com.jm.focustimer.core.database.model.toPreset
import com.jm.focustimer.core.database.model.toPresetEntity
import com.jm.focustimer.domain.model.preset.Preset
import com.jm.focustimer.domain.repository.PresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * PresetRepository의 기본 구현체
 * Room Database를 통해 프리셋 데이터를 관리합니다.
 */
class PresetRepositoryImpl @Inject constructor(
    private val presetDao: PresetDao
) : PresetRepository {

    override suspend fun insertPreset(preset: Preset): Long {
        return presetDao.insertPreset(preset.toPresetEntity())
    }

    override suspend fun insertPresets(presets: List<Preset>) {
        presetDao.insertPresets(presets.map { it.toPresetEntity() })
    }

    override suspend fun updatePreset(preset: Preset) {
        presetDao.updatePreset(preset.toPresetEntity())
    }

    override suspend fun deletePreset(preset: Preset) {
        presetDao.deletePreset(preset.toPresetEntity())
    }

    override suspend fun getPresetById(id: Int): Preset? {
        return presetDao.getPresetById(id)?.toPreset()
    }

    override fun getAllPresets(): Flow<List<Preset>> {
        return presetDao.getAllPresets()
            .map { entities -> entities.map { it.toPreset() } }
    }

    override suspend fun getTotalPresetCount(): Int {
        return presetDao.getTotalPresetCount()
    }

    override suspend fun deleteAllPresets() {
        presetDao.deleteAllPresets()
    }
}
