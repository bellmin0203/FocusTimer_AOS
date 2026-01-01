package com.jm.focustimer.domain.usecase.session

/**
 * 세션 도메인 관련 예외
 */
sealed class SessionException(message: String? = null) : Exception(message) {
    /** 세션을 찾을 수 없음 */
    data class SessionNotFound(val sessionId: Long) : SessionException()
    
    /** 유효하지 않은 지속 시간 */
    class InvalidDuration : SessionException()
    
    /** 유효하지 않은 시간 범위 (종료 시간이 시작 시간보다 빠름) */
    class InvalidTimeRange : SessionException()
    
    /** 시작 시간이 누락됨 */
    class MissingStartTime : SessionException()
}
