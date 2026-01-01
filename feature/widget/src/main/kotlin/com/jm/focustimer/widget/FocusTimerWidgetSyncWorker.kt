package com.jm.focustimer.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Focus Timer Widget Sync Worker
 * 
 * 주기적으로 타이머 상태를 확인하고 위젯을 동기화하는 Worker
 * 
 * 참고: 실제로는 타이머 서비스에서 위젯을 직접 업데이트하는 것이 더 효율적이지만,
 * 모듈 간 의존성을 고려하여 Worker 방식도 고려할 수 있습니다.
 */
class FocusTimerWidgetSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetSyncWorkerEntryPoint {
        fun widgetUpdater(): FocusTimerWidgetUpdater
    }

    override suspend fun doWork(): Result {
        return try {
            // Hilt EntryPoint를 통해 의존성 주입
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                WidgetSyncWorkerEntryPoint::class.java
            )
            
            val widgetUpdater = entryPoint.widgetUpdater()
            
            // 위젯 동기화 로직
            // TODO: 타이머 상태를 가져와서 위젯 업데이트
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
