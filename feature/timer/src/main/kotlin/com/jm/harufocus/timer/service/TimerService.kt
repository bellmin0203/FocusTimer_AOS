package com.jm.harufocus.timer.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.jm.harufocus.common.TimerServiceAction
import com.jm.harufocus.domain.usecase.session.ManageTimerSessionUseCase
import com.jm.harufocus.timer.TimerManager
import com.jm.harufocus.timer.usecase.TimerStatus
import com.jm.harufocus.util.CrashReporter
import com.jm.harufocus.util.CrashReportingExceptionHandler
import com.jm.harufocus.widget.HaruFocusWidgetUpdater
import com.jm.logutil.LogUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 타이머 Foreground Service
 *
 * 백그라운드에서도 타이머가 계속 동작하도록 하는 서비스입니다.
 * 알림을 통해 현재 타이머 상태를 표시하고, 사용자가 알림에서 직접 제어할 수 있습니다.
 * 타이머 세션의 생명주기도 관리합니다.
 */
@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerManager: TimerManager

    @Inject
    lateinit var widgetUpdater: HaruFocusWidgetUpdater

    @Inject
    lateinit var manageTimerSessionUseCase: ManageTimerSessionUseCase

    private lateinit var notificationHelper: TimerNotificationHelper
    private val serviceScope = CoroutineScope(
        Dispatchers.Main + SupervisorJob() + CrashReportingExceptionHandler.withTag("TimerService")
    )

    private var timerStateJob: Job? = null

    // 타이머 완료 처리 중복 방지 플래그
    private var isCompletionHandled: Boolean = false

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
        LogUtil.d("Timer State: ${timerManager.timerState.value}")

        when (intent?.action) {
            TimerServiceAction.ACTION_START -> {
                val durationMillis = intent.getLongExtra(TimerServiceAction.EXTRA_DURATION, 0)
                val duration = durationMillis.milliseconds
                handleStart(duration)
            }

            TimerServiceAction.ACTION_PAUSE -> handlePause()
            TimerServiceAction.ACTION_RESUME -> handleResume()
            TimerServiceAction.ACTION_STOP -> handleStop()
            TimerServiceAction.ACTION_COMPLETE -> handleComplete()
        }

        return START_STICKY // 시스템에 의해 종료되면 재시작
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * 타이머 시작
     *
     * 세션을 DB에 저장한 후 타이머를 시작합니다.
     */
    private fun handleStart(duration: Duration) {
        LogUtil.d("TimerService handleStart: duration=$duration")
        CrashReporter.log("타이머 시작: duration=$duration")
        CrashReporter.setTimerStatus("RUNNING")

        // 완료 처리 플래그 초기화
        isCompletionHandled = false

        val selectedPreset = timerManager.timerState.value.selectedPreset

        // 세션을 DB에 저장
        serviceScope.launch {
            val sessionResult = manageTimerSessionUseCase.startSession(
                presetId = selectedPreset?.id ?: 0,
                duration = duration
            )

            sessionResult.fold(
                onSuccess = { sessionId ->
                    LogUtil.d("세션 저장 성공, sessionId=$sessionId")
                    timerManager.setSession(sessionId, Instant.now())
                },
                onFailure = { error ->
                    LogUtil.e("세션 저장 실패", error)
                    CrashReporter.recordException(error, "세션 저장 실패")
                    // 세션 저장 실패해도 타이머는 계속 동작
                }
            )
        }

        // 타이머 시작
        timerManager.start(
            initialDuration = duration,
            duration = duration,
        )

        // Foreground Service 시작
        val notification = notificationHelper.createRunningNotification(
            remainingTime = duration,
            isRunning = true
        )
        startForeground(TimerNotificationHelper.NOTIFICATION_ID, notification)

        // 위젯 업데이트: 타이머 시작
        widgetUpdater.onTimerStarted(duration, selectedPreset)
    }

    /**
     * 타이머 일시정지
     */
    private fun handlePause() {
        LogUtil.d("TimerService handlePause")
        CrashReporter.log("타이머 일시정지")
        CrashReporter.setTimerStatus("PAUSED")
        timerManager.pause()

        // 위젯 업데이트: 타이머 일시정지
        val state = timerManager.timerState.value
        widgetUpdater.onTimerPaused(state.remainingTime, state.selectedPreset)
    }

    /**
     * 타이머 재개
     */
    private fun handleResume() {
        LogUtil.d("TimerService handleResume")
        CrashReporter.log("타이머 재개")
        CrashReporter.setTimerStatus("RUNNING")
        timerManager.resume()

        // 위젯 업데이트: 타이머 재개
        val state = timerManager.timerState.value
        widgetUpdater.onTimerResumed(state.remainingTime, state.selectedPreset)
    }

    /**
     * 타이머 정지 및 서비스 종료
     *
     * 진행 중인 세션이 있으면 미완료 상태로 업데이트합니다.
     */
    private fun handleStop() {
        LogUtil.d("TimerService handleStop")
        CrashReporter.log("타이머 중지")
        CrashReporter.setTimerStatus("STOPPED")
        val state = timerManager.timerState.value
        val selectedPreset = state.selectedPreset

        // 진행 중인 세션이 있으면 미완료 상태로 업데이트
        state.currentSessionId?.let { sessionId ->
            serviceScope.launch {
                val updateResult = manageTimerSessionUseCase.stopSession(
                    sessionId = sessionId,
                    presetId = selectedPreset?.id,
                    startTime = state.sessionStartTime,
                    initialDuration = state.initialDuration
                )

                updateResult.fold(
                    onSuccess = {
                        LogUtil.d("세션 업데이트 성공 (미완료), sessionId=$sessionId")
                    },
                    onFailure = { error ->
                        LogUtil.e("세션 업데이트 실패", error)
                        CrashReporter.recordException(error, "세션 업데이트 실패 (미완료)")
                    }
                )
            }
        }

        timerManager.stop(state.initialDuration)

        // 위젯 업데이트: 타이머 중지
        widgetUpdater.onTimerStopped(selectedPreset)

        // Foreground 상태 해제 및 서비스 종료
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 타이머 완료 확인 처리
     *
     * 사용자가 완료 버튼을 클릭했을 때 호출됩니다.
     * 초과 시간이 있는 경우 세션에 초과 시간을 업데이트합니다.
     */
    private fun handleComplete() {
        LogUtil.d("TimerService handleComplete")
        CrashReporter.log("타이머 완료 확인")
        CrashReporter.setTimerStatus("COMPLETED")
        val state = timerManager.timerState.value

        // 완료 상태가 아니면 무시
        if (!state.isCompleted && !state.isOvertimeStatus) {
            LogUtil.w("완료 상태가 아님")
            return
        }

        val selectedPreset = state.selectedPreset

        // 초과 시간이 있는 경우에만 세션 업데이트
        if (state.overtime > Duration.ZERO) {
            state.currentSessionId?.let { sessionId ->
                serviceScope.launch {
                    val updateResult = manageTimerSessionUseCase.completeSession(
                        sessionId = sessionId,
                        presetId = selectedPreset?.id,
                        startTime = state.sessionStartTime,
                        initialDuration = state.initialDuration,
                        overtime = state.overtime
                    )

                    updateResult.fold(
                        onSuccess = {
                            LogUtil.d("세션 초과 시간 업데이트 성공, sessionId=$sessionId, overtime=${state.overtime}")
                        },
                        onFailure = { error ->
                            LogUtil.e("세션 초과 시간 업데이트 실패", error)
                            CrashReporter.recordException(error, "세션 초과 시간 업데이트 실패")
                        }
                    )
                }
            }
        } else {
            LogUtil.d("초과 시간 없음, 세션 업데이트 건너뜀")
        }

        // 타이머를 초기 상태로 리셋
        timerManager.stop(state.initialDuration)

        // 위젯 업데이트: 타이머 중지
        widgetUpdater.onTimerStopped(selectedPreset)

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
//                        widgetUpdater.onTimerTick(
//                            remainingTime = state.remainingTime,
//                            overtime = state.overtime,
//                            presetColorIndex = state.presetColorIndex
//                        )
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
                            preset = state.selectedPreset
                        )
                    }

                    is TimerStatus.Completed -> {
                        // 타이머가 완료되었을 때 알림 업데이트
                        val notification = notificationHelper.createCompletedNotification(
                            overtime = Duration.ZERO
                        )
                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(
                            TimerNotificationHelper.NOTIFICATION_ID,
                            notification
                        )

                        // 위젯 업데이트: 타이머 완료
                        widgetUpdater.onTimerCompleted(
                            preset = state.selectedPreset
                        )

                        // 세션 완료 처리 (최초 완료 시점에만)
                        if (!isCompletionHandled) {
                            isCompletionHandled = true
                            state.currentSessionId?.let { sessionId ->
                                serviceScope.launch {
                                    val updateResult = manageTimerSessionUseCase.completeSession(
                                        sessionId = sessionId,
                                        presetId = state.selectedPreset?.id,
                                        startTime = state.sessionStartTime,
                                        initialDuration = state.initialDuration,
                                        overtime = Duration.ZERO
                                    )

                                    updateResult.fold(
                                        onSuccess = {
                                            LogUtil.d("세션 자동 완료 업데이트 성공, sessionId=$sessionId")
                                        },
                                        onFailure = { error ->
                                            LogUtil.e("세션 자동 완료 업데이트 실패", error)
                                            CrashReporter.recordException(error, "세션 자동 완료 업데이트 실패")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    is TimerStatus.Overtime -> {
                        // 초과 시간 진행 중 알림 업데이트
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

                        // 위젯 업데이트: 초과 시간 진행 중
                        widgetUpdater.onTimerOvertime(
                            overtime = state.overtime,
                            preset = state.selectedPreset
                        )
                    }

                    is TimerStatus.Idle -> {
                        // Idle 상태면 서비스 종료 (이미 stop에서 처리되지만 안전장치)
                        LogUtil.d("TimerService Idle 상태 감지, 서비스 종료 확인")

                        // 위젯 업데이트: 타이머 중지
                        widgetUpdater.onTimerStopped(preset = state.selectedPreset)
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
