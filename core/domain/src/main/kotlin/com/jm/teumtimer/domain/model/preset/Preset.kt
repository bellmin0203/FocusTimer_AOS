package com.jm.teumtimer.domain.model.preset

import java.time.Instant
import kotlin.time.Duration

/**
 * 타이머 프리셋 정보를 담는 데이터 클래스
 *
 * @param id 프리셋의 고유 식별자 (Primary key)
 * @param name 프리셋 이름
 * @param duration 타이머 지속 시간
 * @param colorIndex 타이머 컬러 인덱스 (0-5)
 * @param createdAt 프리셋 생성 시간
 */
data class Preset(
    val id: Int,
    val name: String,
    val duration: Duration,
    val colorIndex: Int = 0,
    val createdAt: Instant
)