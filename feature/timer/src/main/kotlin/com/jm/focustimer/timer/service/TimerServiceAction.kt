package com.jm.focustimer.timer.service

/**
 * 타이머 서비스 액션
 *
 * Service와 통신하기 위한 Intent Action 정의
 */
object TimerServiceAction {
    const val ACTION_START = "com.jm.focustimer.action.START"
    const val ACTION_PAUSE = "com.jm.focustimer.action.PAUSE"
    const val ACTION_RESUME = "com.jm.focustimer.action.RESUME"
    const val ACTION_STOP = "com.jm.focustimer.action.STOP"
    
    // Intent Extra Keys
    const val EXTRA_DURATION = "extra_duration"
    const val EXTRA_SESSION_ID = "extra_session_id"
    const val EXTRA_PRESET_ID = "extra_preset_id"
}
