package com.jm.harufocus.core.datastore.api

import kotlinx.coroutines.flow.Flow

/**
 * In-App Review 관련 설정 저장을 위한 DataSource 인터페이스
 */
interface InAppReviewPreferencesDataSource {

    /**
     * 완료된 세션 수 Flow
     */
    val completedSessionCountFlow: Flow<Int>

    /**
     * 마지막 리뷰 요청 날짜 (epoch day) Flow
     */
    val lastReviewRequestDateFlow: Flow<Long?>

    /**
     * 사용자가 리뷰를 완료했는지 여부 Flow
     */
    val hasUserReviewedFlow: Flow<Boolean>

    /**
     * 완료된 세션 수를 증가시킵니다.
     */
    suspend fun incrementCompletedSessionCount()

    /**
     * 마지막 리뷰 요청 날짜를 업데이트합니다.
     * @param epochDay epoch day
     */
    suspend fun updateLastReviewRequestDate(epochDay: Long)

    /**
     * 사용자 리뷰 완료 상태를 업데이트합니다.
     * @param hasReviewed 리뷰 완료 여부
     */
    suspend fun updateHasUserReviewed(hasReviewed: Boolean)

    companion object {
        /** 리뷰 요청을 위한 최소 완료 세션 수 */
        const val MIN_SESSIONS_FOR_REVIEW = 5

        /** 리뷰 재요청까지의 최소 대기 일수 */
        const val MIN_DAYS_BETWEEN_REVIEWS = 30
    }
}
