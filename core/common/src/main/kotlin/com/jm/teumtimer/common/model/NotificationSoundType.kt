package com.jm.teumtimer.common.model

/**
 * 타이머 완료 알림 소리 타입
 *
 * @property displayName 사용자에게 표시될 이름
 * @property soundResName 리소스 파일명 (raw 폴더 내 파일명, 확장자 제외)
 */
enum class NotificationSoundType(
    val displayName: String,
    val soundResName: String?
) {
    DEFAULT("기본", "notification_default"),
    BELL("벨", "notification_bell"),
    BUZZER("버저", "notification_buzzer"),
    SILENT("무음", null),
    CUSTOM("커스텀", null); // 사용자 지정 소리 (향후 구현)

    companion object {
        /**
         * 문자열 값을 NotificationSoundType으로 변환
         * 매칭되는 타입이 없으면 DEFAULT 반환
         */
        fun fromString(value: String): NotificationSoundType {
            return entries.find { it.name == value } ?: DEFAULT
        }
    }
}
