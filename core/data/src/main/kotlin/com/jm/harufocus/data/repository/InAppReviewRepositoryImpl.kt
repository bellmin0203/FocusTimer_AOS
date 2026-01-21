package com.jm.harufocus.data.repository

import com.jm.harufocus.core.datastore.api.InAppReviewPreferencesDataSource
import com.jm.harufocus.core.datastore.api.InAppReviewPreferencesDataSource.Companion.MIN_DAYS_BETWEEN_REVIEWS
import com.jm.harufocus.core.datastore.api.InAppReviewPreferencesDataSource.Companion.MIN_SESSIONS_FOR_REVIEW
import com.jm.harufocus.domain.repository.InAppReviewRepository
import com.jm.logutil.LogUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * In-App Review Repository 구현체
 * 리뷰 요청 조건 확인 및 데이터 관리를 담당합니다.
 */
class InAppReviewRepositoryImpl @Inject constructor(
    private val inAppReviewPreferencesDataSource: InAppReviewPreferencesDataSource
) : InAppReviewRepository {

    override val completedSessionCount: Flow<Int> =
        inAppReviewPreferencesDataSource.completedSessionCountFlow

    override val lastReviewRequestDate: Flow<Long?> =
        inAppReviewPreferencesDataSource.lastReviewRequestDateFlow

    override val hasUserReviewed: Flow<Boolean> =
        inAppReviewPreferencesDataSource.hasUserReviewedFlow

    override suspend fun incrementCompletedSessionCount() {
        inAppReviewPreferencesDataSource.incrementCompletedSessionCount()
    }

    override suspend fun updateLastReviewRequestDate(epochDay: Long) {
        inAppReviewPreferencesDataSource.updateLastReviewRequestDate(epochDay)
    }

    override suspend fun updateHasUserReviewed(hasReviewed: Boolean) {
        inAppReviewPreferencesDataSource.updateHasUserReviewed(hasReviewed)
    }

    override suspend fun canRequestReview(): Boolean {
        // 이미 리뷰를 완료한 경우 더 이상 요청하지 않음
        val hasReviewed = hasUserReviewed.first()
        if (hasReviewed) {
            LogUtil.d("이미 리뷰를 완료함")
            return false
        }

        // 최소 세션 수 확인
        val sessionCount = completedSessionCount.first()
        if (sessionCount < MIN_SESSIONS_FOR_REVIEW) {
            LogUtil.d("세션 수 부족: $sessionCount < $MIN_SESSIONS_FOR_REVIEW")
            return false
        }

        // 마지막 리뷰 요청 후 일정 기간 경과 확인
        val lastRequestDate = lastReviewRequestDate.first()
        if (lastRequestDate != null) {
            val today = LocalDate.now().toEpochDay()
            val daysSinceLastRequest = today - lastRequestDate
            if (daysSinceLastRequest < MIN_DAYS_BETWEEN_REVIEWS) {
                LogUtil.d("마지막 리뷰 요청 후 경과 일수 부족: $daysSinceLastRequest < $MIN_DAYS_BETWEEN_REVIEWS")
                return false
            }
        }

        LogUtil.d("리뷰 요청 가능")
        return true
    }
}
