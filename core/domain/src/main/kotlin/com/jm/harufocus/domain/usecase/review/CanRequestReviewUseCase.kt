package com.jm.harufocus.domain.usecase.review

import com.jm.harufocus.domain.repository.InAppReviewRepository
import javax.inject.Inject

/**
 * 리뷰 요청 가능 여부를 확인하는 UseCase
 */
class CanRequestReviewUseCase @Inject constructor(
    private val inAppReviewRepository: InAppReviewRepository
) {
    /**
     * 리뷰를 요청할 수 있는 조건인지 확인합니다.
     * @return 리뷰 요청 가능 여부
     */
    suspend operator fun invoke(): Boolean {
        return inAppReviewRepository.canRequestReview()
    }
}
