package com.jm.harufocus.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 사용자 관련 데이터/설정 관리를 위한 Repository 인터페이스
 */
interface UserPreferencesRepository {
    /**
     * 튜토리얼 완료 여부 Flow
     * null: 설정되지 않음 (신규 여부 확인 필요)
     * true: 완료
     * false: 미완료 (온보딩 필요)
     */
    val isTutorialCompleted: Flow<Boolean?>

    /**
     * 튜토리얼 완료 여부를 업데이트합니다.
     * @param completed 완료 여부
     */
    suspend fun updateIsTutorialCompleted(completed: Boolean)

    /**
     * 통계 차트 설명 툴팁 노출 여부 Flow
     * true: 이미 노출됨
     * false: 아직 노출되지 않음
     */
    val isStatsChartTooltipShown: Flow<Boolean>

    /**
     * 통계 차트 설명 툴팁 노출 여부를 업데이트합니다.
     * @param shown 노출 여부
     */
    suspend fun updateIsStatsChartTooltipShown(shown: Boolean)
}
