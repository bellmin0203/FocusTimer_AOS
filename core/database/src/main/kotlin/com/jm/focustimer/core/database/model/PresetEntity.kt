package com.jm.focustimer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jm.focustimer.domain.model.Preset
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val duration: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

fun PresetEntity.toPreset(): Preset {
    return Preset(
        id = id,
        name = name,
        duration = duration.milliseconds,
        createdAt = Instant.ofEpochMilli(createdAt)
    )
}

fun Preset.toPresetEntity(): PresetEntity {
    return PresetEntity(
        name = name,
        duration = duration.inWholeMilliseconds,
        createdAt = createdAt.toEpochMilli()
    )
}