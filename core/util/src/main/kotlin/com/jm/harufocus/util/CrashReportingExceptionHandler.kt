package com.jm.harufocus.util

import com.jm.logutil.LogUtil
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * Crashlytics와 통합된 CoroutineExceptionHandler를 제공합니다.
 *
 * 사용 예시:
 * ```
 * private val serviceScope = CoroutineScope(
 *     Dispatchers.Main + SupervisorJob() + CrashReportingExceptionHandler.handler
 * )
 * ```
 */
object CrashReportingExceptionHandler {

    /**
     * 코루틴 예외를 Crashlytics에 기록하는 핸들러
     */
    val handler = CoroutineExceptionHandler { _, throwable ->
        LogUtil.e("Uncaught coroutine exception", throwable)
        CrashReporter.recordException(throwable, "Uncaught coroutine exception")
    }

    /**
     * 태그와 함께 예외를 기록하는 핸들러를 생성합니다.
     *
     * @param tag 로그 태그
     * @return CoroutineExceptionHandler
     */
    fun withTag(tag: String) = CoroutineExceptionHandler { _, throwable ->
        LogUtil.e("[$tag] Uncaught coroutine exception", throwable)
        CrashReporter.log("Coroutine exception in: $tag")
        CrashReporter.recordException(throwable, "[$tag] Uncaught coroutine exception")
    }
}
