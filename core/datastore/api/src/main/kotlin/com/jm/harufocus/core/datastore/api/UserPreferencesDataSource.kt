package com.jm.harufocus.core.datastore.api

import kotlinx.coroutines.flow.Flow

/**
 * 사용자 관련 설정/상태 저장을 위한 DataSource 인터페이스
 */
interface UserPreferencesDataSource {
    /**
     * 온보딩 튜토리얼 완료 여부 Flow
     * null인 경우 설정되지 않음을 의미 (기존 사용자 판별용)
     */
    val isTutorialCompletedFlow: Flow<Boolean?>

    /**
     * 온보딩 튜토리얼 완료 여부를 업데이트합니다.
     * @param completed 완료 여부
     */
    suspend fun updateIsTutorialCompleted(completed: Boolean)

    companion object {
        const val DEFAULT_IS_TUTORIAL_COMPLETED = false
    }
}
