package com.jm.harufocus.domain.usecase.preset

import com.jm.harufocus.domain.model.preset.Preset
import com.jm.harufocus.domain.repository.PresetRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 기존 프리셋을 수정하는 UseCase
 */
@Singleton
class UpdatePresetUseCase @Inject constructor(
    private val presetRepository: PresetRepository
) {
    /**
     * 프리셋을 수정합니다.
     *
     * @param preset 수정할 프리셋
     * @return 성공 시 true, 실패 시 에러
     */
    suspend operator fun invoke(preset: Preset): Result<Unit> {
        // 이름 유효성 검사
        if (preset.name.isBlank()) {
            return Result.failure(IllegalArgumentException("타이머 이름을 입력해주세요."))
        }

        if (preset.name.length > MAX_NAME_LENGTH) {
            return Result.failure(
                IllegalArgumentException("타이머 이름은 ${MAX_NAME_LENGTH}자 이하로 입력해주세요.")
            )
        }

        // 시간 유효성 검사
        if (preset.duration.inWholeSeconds <= 0) {
            return Result.failure(IllegalArgumentException("타이머 시간은 0보다 커야 합니다."))
        }

        return try {
            presetRepository.updatePreset(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 20
    }
}
