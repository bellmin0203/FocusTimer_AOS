package com.jm.focustimer.timer.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.jm.focustimer.timer.TimerManager
import com.jm.focustimer.timer.usecase.TimerStatus
import com.jm.focustimer.widget.FocusTimerWidgetUpdater
import com.jm.logutil.LogUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 타이머 Foreground Service
 *
 * 백그라운드에서도 타이머가 계속 동작하도록 하는 서비스입니다.
 * 알림을 통해 현재 타이머 상태를 표시하고, 사용자가 알림에서 직접 제어할 수 있습니다.
 */
@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerManager: TimerManager

    @Inject
    lateinit var widgetUpdater: FocusTimerWidgetUpdater

    private lateinit var notificationHelper: TimerNotificationHelper
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var timerStateJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        LogUtil.d("TimerService onCreate")

        // 알림 헬퍼 초기화
        notificationHelper = TimerNotificationHelper(this)
        notificationHelper.createNotificationChannel()

        // 타이머 상태 관찰 시작
        observeTimerState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtil.d("TimerService onStartCommand: action=${intent?.action}")

        when (intent?.action) {
            TimerServiceAction.ACTION_START -> {
                val durationMillis = intent.getLongExtra(TimerServiceAction.EXTRA_DURATION, 0)
                val duration = durationMillis.milliseconds
                handleStart(duration)
            }

            TimerServiceAction.ACTION_PAUSE -> handlePause()
            TimerServiceAction.ACTION_RESUME -> handleResume()
            TimerServiceAction.ACTION_STOP -> handleStop()
        }

        return START_STICKY // 시스템에 의해 종료되면 재시작
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * 타이머 시작
     */
    private fun handleStart(duration: Duration) {
        LogUtil.d("TimerService handleStart: duration=$duration")

        // Foreground Service 시작
        val notification = notificationHelper.createRunningNotification(
            remainingTime = duration,
            isRunning = true
        )
        startForeground(TimerNotificationHelper.NOTIFICATION_ID, notification)

        // 위젯 업데이트: 타이머 시작
        val presetColorIndex = timerManager.timerState.value.presetColorIndex
        widgetUpdater.onTimerStarted(duration, presetColorIndex)
    }

    /**
     * 타이머 일시정지
     */
    private fun handlePause() {
        LogUtil.d("TimerService handlePause")
        timerManager.pause()

        // 위젯 업데이트: 타이머 일시정지
        val state = timerManager.timerState.value
        widgetUpdater.onTimerPaused(state.remainingTime, state.presetColorIndex)
    }

    /**
     * 타이머 재개
     */
    private fun handleResume() {
        LogUtil.d("TimerService handleResume")
        timerManager.resume()

        // 위젯 업데이트: 타이머 재개
        val state = timerManager.timerState.value
        widgetUpdater.onTimerResumed(state.remainingTime, state.presetColorIndex)
    }

    /**
     * 타이머 정지 및 서비스 종료
     */
    private fun handleStop() {
        LogUtil.d("TimerService handleStop")
        val presetColorIndex = timerManager.timerState.value.presetColorIndex
        timerManager.stop(timerManager.timerState.value.initialDuration)

        // 위젯 업데이트: 타이머 중지
        widgetUpdater.onTimerStopped(presetColorIndex)

        // Foreground 상태 해제 및 서비스 종료
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 타이머 상태 관찰
     *
     * 타이머 상태가 변경될 때마다 알림과 위젯을 업데이트합니다.
     */
    private fun observeTimerState() {
        timerStateJob = timerManager.timerState
            .onEach { state ->
                LogUtil.d("TimerService observeTimerState: $state")

                when (state.status) {
                    is TimerStatus.Running -> {
                        // 타이머가 실행 중일 때 알림 업데이트
                        val notification = notificationHelper.createRunningNotification(
                            remainingTime = state.remainingTime,
                            isRunning = true,
                            overtime = state.overtime
                        )
                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            TimerNotificationHelper.NOTIFICATION_ID,
                            notification
                        )

                        // 위젯 업데이트: 타이머 틱
                        widgetUpdater.onTimerTick(
                            remainingTime = state.remainingTime,
                            overtime = state.overtime,
                            presetColorIndex = state.presetColorIndex
                        )
                    }

                    is TimerStatus.Paused -> {
                        // 타이머가 일시정지 상태일 때 알림 업데이트
                        val notification = notificationHelper.createRunningNotification(
                            remainingTime = state.remainingTime,
                            isRunning = false,
                            overtime = state.overtime
                        )
                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            TimerNotificationHelper.NOTIFICATION_ID,
                            notification
                        )

                        // 위젯 업데이트: 일시정지 (이미 handlePause에서 처리되지만 안전장치)
                        widgetUpdater.onTimerPaused(
                            remainingTime = state.remainingTime,
                            presetColorIndex = state.presetColorIndex
                        )
                    }

                    is TimerStatus.Completed -> {
                        // 타이머가 완료되었을 때 알림 업데이트
                        val notification = notificationHelper.createCompletedNotification(
                            overtime = state.overtime
                        )
                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            TimerNotificationHelper.NOTIFICATION_ID,
                            notification
                        )

                        // 위젯 업데이트: 타이머 완료
                        widgetUpdater.onTimerCompleted(
                            overtime = state.overtime,
                            presetColorIndex = state.presetColorIndex
                        )
                    }

                    is TimerStatus.Idle -> {
                        // Idle 상태면 서비스 종료 (이미 stop에서 처리되지만 안전장치)
                        LogUtil.d("TimerService Idle 상태 감지, 서비스 종료 확인")

                        // 위젯 업데이트: 타이머 중지
                        widgetUpdater.onTimerStopped(presetColorIndex = state.presetColorIndex)
                    }
                }
            }
            .launchIn(serviceScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d("TimerService onDestroy")

        // 타이머 상태 관찰 중단
        timerStateJob?.cancel()

        // 코루틴 스코프 취소
        serviceScope.cancel()
    }
}
