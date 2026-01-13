package com.jm.harufocus.domain.usecase.preset

import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.repository.PresetRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration

/**
 * 새로운 프리셋을 추가하는 UseCase
 *
 * 프리셋은 최대 5개까지만 저장 가능합니다.
 */
@Singleton
class AddPresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    /**
     * 프리셋을 추가합니다.
     *
     * @param name 프리셋 이름
     * @param duration 타이머 지속 시간
     * @param colorIndex 타이머 컬러 인덱스
     * @return 성공 시 생성된 프리셋 ID, 실패 시 null
     */
    suspend operator fun invoke(
        name: String,
        duration: Duration,
        colorIndex: Int = 0
    ): Result<Long> {
        // 프리셋 개수 확인
        val currentCount = presetRepository.getTotalPresetCount()
        if (currentCount >= MAX_PRESET_COUNT) {
            return Result.failure(
                PresetException.MaxCountExceeded(MAX_PRESET_COUNT)
            )
        }

        // 이름 유효성 검사
        if (name.isBlank()) {
            return Result.failure(PresetException.InvalidName())
        }

        if (name.length > MAX_NAME_LENGTH) {
            return Result.failure(
                PresetException.NameTooLong(MAX_NAME_LENGTH)
            )
        }

        // 시간 유효성 검사
        if (duration.inWholeSeconds <= 0) {
            return Result.failure(PresetException.InvalidDuration())
        }

        // 컬러 인덱스 유효성 검사
        if (colorIndex !in 0..5) {
            return Result.failure(PresetException.InvalidColor())
        }

        val preset = Preset(
            id = 0, // Auto-generated
            name = name.trim(),
            duration = duration,
            colorIndex = colorIndex,
            createdAt = Instant.now()
        )

        return try {
            val id = presetRepository.insertPreset(preset)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val MAX_PRESET_COUNT = 5
        const val MAX_NAME_LENGTH = 20
    }
}

/**
 * 프리셋 도메인 관련 예외
 */
sealed class PresetException(message: String? = null) : Exception(message) {
    class TimerRunning : PresetException()
    class NotFound : PresetException()
    class InvalidDuration : PresetException()
    class InvalidName : PresetException()
    class InvalidColor : PresetException()
    data class MaxCountExceeded(val maxCount: Int) : PresetException()
    data class NameTooLong(val maxLength: Int) : PresetException()
    class FailSave : PresetException()
    class FailDelete : PresetException()
    class FailUpdate : PresetException()
}
