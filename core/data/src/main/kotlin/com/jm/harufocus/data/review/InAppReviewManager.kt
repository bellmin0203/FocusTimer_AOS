package com.jm.harufocus.data.review

import android.app.Activity
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.jm.harufocus.domain.repository.InAppReviewRepository
import com.jm.harufocus.util.CrashReporter
import com.jm.logutil.LogUtil
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * In-App Review API를 실행하는 Manager 클래스
 * Android Activity에 의존하므로 data 레이어에 위치합니다.
 */
@Singleton
class InAppReviewManager @Inject constructor(
    private val inAppReviewRepository: InAppReviewRepository
) {
    /**
     * In-App Review 플로우를 실행합니다.
     * @param activity 현재 Activity
     * @return 리뷰 플로우 시작 성공 여부
     */
    suspend fun launchReviewFlow(activity: Activity): Result<Unit> {
        return try {
            val reviewManager = ReviewManagerFactory.create(activity)

            // ReviewInfo 요청
            val reviewInfo = suspendCancellableCoroutine { continuation ->
                val request = reviewManager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(task.result))
                    } else {
                        val exception = task.exception
                        if (exception is ReviewException) {
                            LogUtil.e("ReviewInfo 요청 실패: ${exception.errorCode}")
                        }
                        continuation.resume(Result.failure(exception ?: Exception("Unknown error")))
                    }
                }
            }

            // ReviewInfo 획득 실패 시 반환
            if (reviewInfo.isFailure) {
                return Result.failure(reviewInfo.exceptionOrNull() ?: Exception("Failed to get ReviewInfo"))
            }

            // Review Flow 실행
            val launchResult = suspendCancellableCoroutine { continuation ->
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo.getOrThrow())
                flow.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Result.success(Unit))
                    } else {
                        continuation.resume(Result.failure(task.exception ?: Exception("Unknown error")))
                    }
                }
            }

            // 리뷰 플로우 완료 후 날짜 기록 (성공 여부와 관계없이)
            val today = LocalDate.now().toEpochDay()
            inAppReviewRepository.updateLastReviewRequestDate(today)

            // Note: Google Play는 사용자가 실제로 리뷰를 남겼는지 알려주지 않지만,
            // Google 자체적으로 노출 빈도를 제한함

            launchResult
        } catch (e: Exception) {
            LogUtil.e("In-App Review 실행 실패", e)
            CrashReporter.recordException(e, "In-App Review 실행 실패")
            Result.failure(e)
        }
    }
}
