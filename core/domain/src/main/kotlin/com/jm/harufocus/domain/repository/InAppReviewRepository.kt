package com.jm.harufocus.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * In-App Review 관리를 위한 Repository 인터페이스
 * 리뷰 요청 조건 확인 및 데이터 관리를 담당합니다.
 */
interface InAppReviewRepository {

    /**
     * 완료된 세션 수 Flow
     */
    val completedSessionCount: Flow<Int>

    /**
     * 마지막으로 리뷰를 요청한 날짜 (epoch day) Flow
     */
    val lastReviewRequestDate: Flow<Long?>

    /**
     * 리뷰를 이미 완료했는지 여부 Flow
     */
    val hasUserReviewed: Flow<Boolean>

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
     * 리뷰 완료 상태를 업데이트합니다.
     * @param hasReviewed 리뷰 완료 여부
     */
    suspend fun updateHasUserReviewed(hasReviewed: Boolean)

    /**
     * 리뷰를 요청할 수 있는 조건인지 확인합니다.
     * 조건:
     * - 완료된 세션 수가 임계값 이상
     * - 마지막 리뷰 요청 후 일정 기간 경과
     * - 사용자가 아직 리뷰를 완료하지 않음
     * @return 리뷰 요청 가능 여부
     */
    suspend fun canRequestReview(): Boolean
}
