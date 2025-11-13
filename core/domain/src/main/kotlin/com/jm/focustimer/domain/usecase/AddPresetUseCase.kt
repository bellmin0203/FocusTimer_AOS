package com.jm.focustimer.domain.usecase

import com.jm.focustimer.domain.model.Preset
import com.jm.focustimer.domain.repository.PresetRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 새로운 프리셋을 추가하는 UseCase
 *
 * 프리셋은 최대 10개까지만 저장 가능합니다.
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
        duration: kotlin.time.Duration,
        colorIndex: Int = 0
    ): Result<Long> {
        // 프리셋 개수 확인
        val currentCount = presetRepository.getTotalPresetCount()
        if (currentCount >= MAX_PRESET_COUNT) {
            return Result.failure(
                IllegalStateException("프리셋은 최대 ${MAX_PRESET_COUNT}개까지 저장할 수 있습니다.")
            )
        }

        // 이름 유효성 검사
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("프리셋 이름을 입력해주세요."))
        }

        if (name.length > MAX_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("프리셋 이름은 ${MAX_NAME_LENGTH}자 이하로 입력해주세요.")
            )
        }

        // 시간 유효성 검사
        if (duration.inWholeSeconds <= 0) {
            return Result.failure(IllegalArgumentException("타이머 시간은 0보다 커야 합니다."))
        }

        // 컬러 인덱스 유효성 검사
        if (colorIndex !in 0..5) {
            return Result.failure(IllegalArgumentException("컬러 인덱스는 0에서 5 사이여야 합니다."))
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
        const val MAX_PRESET_COUNT = 10
        const val MAX_NAME_LENGTH = 20
    }
}
