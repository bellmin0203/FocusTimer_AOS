import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Duration

/**
 * 시간 포맷팅 유틸리티
 */
object TimeFormatter {
    const val SECONDS_PER_MINUTE = 60L
    const val SECONDS_PER_HOUR = 3600L
    const val MILLIS_PER_SECOND = 1000L

    fun formatDuration(duration: Duration): String {
        duration.toComponents { hours, minutes, seconds, _ ->
            val locale = Locale.getDefault()
            val isKorean = locale.language == "ko"

            if (isKorean) {
                val formattedHours = if (hours > 0) "${hours}시간 " else ""
                val formattedMinutes = if (minutes > 0) "${minutes}분 " else ""
                val formattedSeconds = if (seconds > 0) "${seconds}초" else ""
                return "$formattedHours$formattedMinutes$formattedSeconds"
            } else {
                val formattedHours = if (hours > 0) "${hours}h " else ""
                val formattedMinutes = if (minutes > 0) "${minutes}m " else ""
                val formattedSeconds = if (seconds > 0) "${seconds}s" else ""
                return "$formattedHours$formattedMinutes$formattedSeconds"
            }
        }
    }


    /**
     * 초를 "HH시간 MM분 SS초" 형식으로 변환 (Locale 대응)
     */
    fun formatDuration(seconds: Int): String {
        val absSeconds = abs(seconds)
        val hours = absSeconds / 3600
        val minutes = (absSeconds % 3600) / 60
        val secs = absSeconds % 3600 % 60 // 변수명 충돌 방지

        val locale = Locale.getDefault()
        val isKorean = locale.language == "ko"

        val hUnit = if (isKorean) "시간" else "h"
        val mUnit = if (isKorean) "분" else "m"
        val sUnit = if (isKorean) "초" else "s"

        return when {
            hours > 0 && minutes > 0 && secs > 0 -> "$hours$hUnit $minutes$mUnit $secs$sUnit"
            hours > 0 && minutes > 0 -> "$hours$hUnit $minutes$mUnit"
            hours > 0 -> "$hours$hUnit"
            minutes > 0 && secs > 0 -> "$minutes$mUnit $secs$sUnit"
            minutes > 0 -> "$minutes$mUnit"
            else -> "$secs$sUnit"
        }
    }

    /**
     * 초를 "01:30:00" 형식으로 변환
     */
    fun formatDurationWithColon(seconds: Int): String {
        val absSeconds = abs(seconds)
        val hours = absSeconds / 3600
        val minutes = (absSeconds % 3600) / 60
        val secs = absSeconds % 60

        return when {
            hours > 0 -> String.Companion.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
            else -> String.Companion.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
        }
    }

    /**
     * Timestamp를 "9:30 AM" 형식으로 변환
     */
    fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Timestamp를 "2025년 10월 25일" (KO) 또는 "Oct 25, 2025" (EN) 형식으로 변환
     */
    fun formatDate(timestamp: Long): String {
        val locale = Locale.getDefault()
        val pattern = if (locale.language == "ko") {
            "yyyy년 MM월 dd일"
        } else {
            "MMM dd, yyyy"
        }
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(Date(timestamp))
    }

    /**
     * 타이머용 "25:00" 형식
     */
    fun formatTimer(seconds: Int): String {
        val absSeconds = abs(seconds)
        val minutes = absSeconds / 60
        val secs = absSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }

    /**
     * 퍼센트 포맷팅
     */
    fun formatPercentage(value: Float): String {
        return String.format(Locale.getDefault(), "%.0f%%", value * 100)
    }


    fun Int.secondsToMillis(): Long = this * 1000L
    fun Int.minutesToMillis(): Long = this * 60 * 1.secondsToMillis()
    fun Int.hoursToMillis(): Long = this * 60 * 1.minutesToMillis()
}