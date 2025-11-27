package com.jm.focustimer.util

import android.content.Context
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import com.jm.focustimer.common.model.NotificationSoundType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 소리 및 진동을 재생하는 유틸리티 클래스
 */
@Singleton
class NotificationSoundPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null

    /**
     * 알림 소리를 재생합니다
     *
     * @param soundType 재생할 소리 타입
     * @param vibrate 진동 여부
     */
    fun play(soundType: NotificationSoundType, vibrate: Boolean) {
        // 진동 재생
        if (vibrate) {
            playVibration()
        }

        // 소리 재생
        when (soundType) {
            NotificationSoundType.SILENT -> {
                // 무음 - 아무것도 재생하지 않음
            }

            NotificationSoundType.DEFAULT -> {
                playSound("notification_default")
            }

            NotificationSoundType.BELL -> {
                playSound("notification_bell")
            }

            NotificationSoundType.BUZZER -> {
                playSound("notification_buzzer")
            }

            NotificationSoundType.CUSTOM -> {
                // TODO: 커스텀 사운드 파일 경로에서 재생
                playSound("notification_default") // 임시로 기본 소리 재생
            }
        }
    }

    /**
     * 리소스 이름으로 소리를 재생합니다
     *
     * @param resourceName raw 폴더 내 리소스 파일명 (확장자 제외)
     */
    private fun playSound(resourceName: String) {
        try {
            // 기존 MediaPlayer가 있으면 해제
            mediaPlayer?.release()

            // 리소스 ID 가져오기
            val resourceId = context.resources.getIdentifier(
                resourceName,
                "raw",
                context.packageName
            )

            // 리소스가 존재하지 않으면 기본 알림 소리 사용
            mediaPlayer = if (resourceId == 0) {
                // 시스템 기본 알림 소리 사용
                MediaPlayer.create(
                    context,
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                )
            } else {
                MediaPlayer.create(context, resourceId)
            }

            mediaPlayer?.apply {
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
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
    private fun playVibration() {
        try {
            val vibrator = context.getSystemService<Vibrator>()
            vibrator?.let {
                // Android 8.0(API 26) 이상에서는 VibrationEffect 사용
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(
                        500, // 지속 시간 (밀리초)
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                    it.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(500)
                }
            }
        } catch (e: Exception) {
            // 진동 실패 시 로그 출력
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
    }
}
