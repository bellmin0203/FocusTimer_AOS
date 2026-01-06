package com.jm.teumtimer.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jm.teumtimer.core.database.model.PresetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 프리셋 데이터베이스 접근을 위한 DAO 인터페이스
 */
@Dao
interface PresetDao {

    /**
     * 새로운 프리셋을 추가합니다.
     * @param preset 추가할 프리셋
     * @return 생성된 프리셋의 ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: PresetEntity): Long

    /**
     * 여러 프리셋을 한번에 추가합니다.
     * @param presets 추가할 프리셋들
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<PresetEntity>)

    /**
     * 프리셋을 업데이트합니다.
     * @param preset 업데이트할 프리셋
     */
    @Update
    suspend fun updatePreset(preset: PresetEntity)

    /**
     * 프리셋을 삭제합니다.
     * @param preset 삭제할 프리셋
     */
    @Delete
    suspend fun deletePreset(preset: PresetEntity)

    /**
     * ID로 특정 프리셋을 조회합니다.
     * @param id 조회할 프리셋 ID
     * @return 프리셋 엔티티 (없으면 null)
     */
    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getPresetById(id: Int): PresetEntity?

    /**
     * 모든 프리셋을 조회합니다 (최신순).
     * @return 모든 프리셋의 Flow
     */
    @Query("SELECT * FROM presets ORDER BY created_at DESC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    /**
     * 총 프리셋 수를 조회합니다.
     * @return 전체 프리셋 개수
     */
    @Query("SELECT COUNT(*) FROM presets")
    suspend fun getTotalPresetCount(): Int

    /**
     * 모든 프리셋을 삭제합니다.
     */
    @Query("DELETE FROM presets")
    suspend fun deleteAllPresets()
}
