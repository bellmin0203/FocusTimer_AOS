package com.jm.harufocus.domain.usecase.review

import com.jm.harufocus.domain.repository.InAppReviewRepository
import javax.inject.Inject

/**
 * 세션 완료 후 리뷰 관련 데이터를 업데이트하는 UseCase
 */
class IncrementSessionForReviewUseCase @Inject constructor(
    private val inAppReviewRepository: InAppReviewRepository
) {
    /**
     * 완료된 세션 수를 증가시킵니다.
     * 타이머 세션이 성공적으로 완료될 때 호출됩니다.
     */
    suspend operator fun invoke() {
        inAppReviewRepository.incrementCompletedSessionCount()
    }
}
