package com.jm.harufocus.common

/**
 * 타이머 서비스 액션 및 상수
 *
 * Service와 통신하기 위한 Intent Action 및 Key 정의
 * feature:timer와 feature:widget에서 공통으로 사용하기 위해 core:common으로 이동
 */
object TimerServiceAction {
    const val SERVICE_CLASS_NAME = "com.jm.harufocus.timer.service.TimerService"

    const val ACTION_START = "com.jm.harufocus.action.START"
    const val ACTION_PAUSE = "com.jm.harufocus.action.PAUSE"
    const val ACTION_RESUME = "com.jm.harufocus.action.RESUME"
    const val ACTION_STOP = "com.jm.harufocus.action.STOP"
    
    // Intent Extra Keys
    const val EXTRA_DURATION = "extra_duration"
    const val EXTRA_SESSION_ID = "extra_session_id"
    const val EXTRA_PRESET_ID = "extra_preset_id"
}
