package com.jm.harufocus.domain.usecase.review

import com.jm.harufocus.domain.repository.InAppReviewRepository
import javax.inject.Inject

/**
 * In-App Review 요청 후 상태를 업데이트하는 UseCase
 * 실제 Review UI 실행은 data 레이어의 InAppReviewManager에서 처리합니다.
 */
class UpdateReviewRequestedUseCase @Inject constructor(
    private val inAppReviewRepository: InAppReviewRepository
) {
    /**
     * 리뷰가 요청되었음을 기록합니다.
     * @param epochDay 요청된 날짜의 epoch day
     */
    suspend operator fun invoke(epochDay: Long) {
        inAppReviewRepository.updateLastReviewRequestDate(epochDay)
    }
}
