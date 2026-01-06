package com.jm.teumtimer.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 알림 소리 및 진동을 재생하는 유틸리티 클래스
 */
@Singleton
class NotificationSoundPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    /**
     * 타이머 완료 알람을 재생합니다 (반복 재생)
     *
     * @param vibrate 진동 여부
     * @param intervalSeconds 진동 반복 간격 (초 단위)
     */
    fun playTimerComplete(vibrate: Boolean, intervalSeconds: Duration = 0.5.seconds) {
        if (vibrate) {
            playRepeatingVibration(intervalSeconds)
        }

        playSound(isLoop = true)
    }

    /**
     *
     */
    private fun playSound(isLoop: Boolean = false) {
        try {
            // 기존 MediaPlayer가 있으면 해제
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer.create(context, R.raw.notification_default)
            mediaPlayer?.apply {
                isLooping = isLoop
                setOnCompletionListener {
                    if (!isLooping) {
                        it.release()
                        mediaPlayer = null
                    }
                }
                start()
            }
        } catch (e: Exception) {
            // 재생 실패 시 로그 출력
            e.printStackTrace()
        }
    }

    /**
     * 진동을 재생합니다
     */
    private fun playOneShotVibration() {
        try {
            getVibrator().let {
                // Android 8.0(API 26) 이상에서는 VibrationEffect 사용
                val vibrationEffect = VibrationEffect.createOneShot(
                    500, // 지속 시간 (밀리초)
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
                it.vibrate(vibrationEffect)
            }
        } catch (e: Exception) {
            // 진동 실패 시 로그 출력
            e.printStackTrace()
        }
    }

    /**
     * 반복 진동 재생 (Waveform 사용)
     *
     * @param intervalDuration 반복 간격
     */
    private fun playRepeatingVibration(intervalDuration: Duration) {
        try {
            getVibrator().let { vibrator ->
                val vibrationDuration = 500L // 0.5초 동안 진동
                val pauseDuration = intervalDuration.inWholeMilliseconds  // 대기

                // 패턴: [대기, 진동, 대기, 진동 ...] (ms 단위)
                // 0초 대기 후 -> 0.5초 진동 -> n초 대기
                val timings = longArrayOf(0, vibrationDuration, pauseDuration)

                // amplitudes: [0(대기), 진동세기, 0(대기)]
                // DEFAULT_AMPLITUDE는 -1이지만 waveform에서는 1~255 값을 명시하거나 0을 써야 함.
                // 편의상 createWaveform(timings, repeatIndex) 방식을 사용합니다.

                // repeatIndex: 패턴 배열에서 반복을 시작할 인덱스.
                // 0으로 설정하면 timings[0]부터 다시 시작 (즉, 계속 반복)
                val repeatIndex = 0

                val vibrationEffect = VibrationEffect.createWaveform(
                    timings,
                    repeatIndex
                )

                vibrator.vibrate(vibrationEffect)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Vibrator 시스템 서비스를 가져오는 헬퍼 함수
     */
    private fun getVibrator(): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    /**
     * 틱 소리를 재생합니다 (짧은 틱 사운드)
     * 매 초마다 호출될 수 있으므로 가볍게 처리
     */
    fun playTick() {
        try {
            // 틱 소리는 짧고 간단하게 재생
            // 리소스 ID 가져오기
            // TODO: jongmin, 틱 사운드 추가 필요함 
            val resourceId = context.resources.getIdentifier(
                "tick_sound",
                "raw",
                context.packageName
            )

            if (resourceId != 0) {
                // 틱 소리용 MediaPlayer (기존과 별도)
                MediaPlayer.create(context, resourceId)?.apply {
                    setOnCompletionListener {
                        it.release()
                    }
                    start()
                }
            }
        } catch (e: Exception) {
            // 재생 실패 시 무시 (틱 소리는 선택적 기능)
            e.printStackTrace()
        }
    }

    /**
     * 재생 중인 소리를 정지하고 리소스를 해제합니다
     */
    fun stop() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        try {
            getVibrator().cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
