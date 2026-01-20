package com.jm.harufocus.setting.model

import com.jm.harufocus.ui.util.UiText
import kotlinx.coroutines.flow.Flow

/**
 * 설정 화면의 개별 항목을 나타내는 데이터 클래스
 *
 * @param title 화면에 표시될 설정 이름
 * @param description 설정에 대한 자세한 설명
 * @param category 설정의 카테고리
 */
sealed class SettingType {
    abstract val title: UiText
    abstract val description: UiText?
    abstract val category: SettingCategory

    /**
     * 토글 스위치가 있는 설정 항목
     *
     * @param stateFlow 설정 값의 StateFlow
     * @param onToggle 설정 값 변경 시 호출될 콜백 함수
     * @param defaultValue 초기 로딩 시 보여줄 기본값
     */
    data class Toggle(
        override val title: UiText,
        override val description: UiText? = null,
        override val category: SettingCategory,
        val stateFlow: Flow<Boolean>,
        val onToggle: (Boolean) -> Unit,
        val defaultValue: Boolean
    ) : SettingType()

    /**
     * 드롭다운 선택이 있는 설정 항목
     *
     * @param T 선택 가능한 값의 타입
     * @param stateFlow 현재 선택된 값의 Flow
     * @param options 선택 가능한 옵션 목록
     * @param displayName 옵션을 화면에 표시할 때 사용할 함수
     * @param onSelect 옵션 선택 시 호출될 콜백 함수
     * @param defaultValue 초기 로딩 시 보여줄 기본값
     */
    data class Selector<T>(
        override val title: UiText,
        override val description: UiText? = null,
        override val category: SettingCategory,
        val stateFlow: Flow<T>,
        val options: List<T>,
        val displayName: (T) -> UiText,
        val onSelect: (T) -> Unit,
        val defaultValue: T
    ) : SettingType()

    /**
     * 클릭 가능한 설정 항목 (링크, 정보 표시 등)
     *
     * @param value 항목 옆에 표시될 값 (예: 버전 번호)
     * @param onClick 클릭 시 호출될 콜백 함수
     */
    data class Clickable(
        override val title: UiText,
        override val description: UiText? = null,
        override val category: SettingCategory,
        val value: String? = null,
        val onClick: (() -> Unit)? = null
    ) : SettingType()
}

/**
 * 설정 항목의 카테고리
 */
enum class SettingCategory {
    APPEARANCE,    // 외관 (다크 테마 등)
    NOTIFICATION,  // 알림 (진동, 사운드 등)
    TIMER,         // 타이머 (세션 기억, 시간 표시 등)
    INTERACTION,   // 상호작용 (햅틱, 컨트롤 등)
    APP_INFO       // 앱 정보 (버전, 개인정보처리방침 등)
}