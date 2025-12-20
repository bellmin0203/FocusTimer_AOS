package com.jm.focustimer.timer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jm.focustimer.timer.R
import kotlin.time.Duration

/**
 * 타이머 알림 생성 및 관리 Helper
 */
class TimerNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "timer_service_channel"
        
        private const val REQUEST_CODE_PAUSE = 100
        private const val REQUEST_CODE_RESUME = 101
        private const val REQUEST_CODE_STOP = 102
        private const val REQUEST_CODE_CONTENT = 103
    }

    /**
     * 알림 채널 생성 (Android 8.0 이상)
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW // LOW로 설정하여 소리 없이 표시
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                setShowBadge(false) // 배지 표시 안 함
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 타이머 실행 중 알림 생성
     */
    fun createRunningNotification(remainingTime: Duration, isRunning: Boolean): Notification {
        val contentText = if (isRunning) {
            context.getString(
                R.string.notification_content_running,
                remainingTime.toFormattedString()
            )
        } else {
            context.getString(
                R.string.notification_content_paused,
                remainingTime.toFormattedString()
            )
        }

        // 알림 클릭 시 앱 열기
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_timer_notification) // 알림 아이콘 (생성 필요)
            .setOngoing(true) // 스와이프로 삭제 불가
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW) // 낮은 우선순위
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // 타이머가 실행 중이면 일시정지 버튼, 일시정지 상태면 재개 버튼
        if (isRunning) {
            builder.addAction(
                R.drawable.ic_pause,
                context.getString(R.string.notification_action_pause),
                createActionPendingIntent(TimerServiceAction.ACTION_PAUSE, REQUEST_CODE_PAUSE)
            )
        } else {
            builder.addAction(
                R.drawable.ic_play,
                context.getString(R.string.notification_action_resume),
                createActionPendingIntent(TimerServiceAction.ACTION_RESUME, REQUEST_CODE_RESUME)
            )
        }

        // 정지 버튼은 항상 표시
        builder.addAction(
            R.drawable.ic_stop,
            context.getString(R.string.notification_action_stop),
            createActionPendingIntent(TimerServiceAction.ACTION_STOP, REQUEST_CODE_STOP)
        )

        return builder.build()
    }

    /**
     * 타이머 완료 알림 생성
     */
    fun createCompletedNotification(overtime: Duration): Notification {
        val contentText = context.getString(
            R.string.notification_content_completed,
            overtime.toFormattedString()
        )

        // 알림 클릭 시 앱 열기
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.timer_completed_label))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_timer_notification)
            .setOngoing(false) // 스와이프로 삭제 가능
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 높은 우선순위 (완료 알림)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true) // 클릭 시 자동 삭제
            .addAction(
                R.drawable.ic_stop,
                context.getString(R.string.notification_action_stop),
                createActionPendingIntent(TimerServiceAction.ACTION_STOP, REQUEST_CODE_STOP)
            )
            .build()
    }

    /**
     * 액션 버튼용 PendingIntent 생성
     */
    private fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Duration을 "HH:MM:SS" 또는 "MM:SS" 형식으로 변환
     */
    private fun Duration.toFormattedString(): String {
        val hours = inWholeHours
        val minutes = (inWholeMinutes % 60)
        val seconds = (inWholeSeconds % 60)

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}
