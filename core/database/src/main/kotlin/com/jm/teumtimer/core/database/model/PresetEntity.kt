package com.jm.teumtimer.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jm.teumtimer.domain.model.preset.Preset
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val duration: Long,

    @ColumnInfo(name = "color_index")
    val colorIndex: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)

fun PresetEntity.toPreset(): Preset {
    return Preset(
        id = id,
        name = name,
        duration = duration.milliseconds,
        colorIndex = colorIndex,
        createdAt = Instant.ofEpochMilli(createdAt)
    )
}

fun Preset.toPresetEntity(): PresetEntity {
    return PresetEntity(
        id = id,
        name = name,
        duration = duration.inWholeMilliseconds,
        colorIndex = colorIndex,
        createdAt = createdAt.toEpochMilli()
    )
}