import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 시간 포맷팅 유틸리티
 */
object TimeFormatter {
    const val SECONDS_PER_MINUTE = 60L
    const val SECONDS_PER_HOUR = 3600L
    const val MILLIS_PER_SECOND = 1000L

    /**
     * 초를 "HH시간 MM분 SS초" 형식으로 변환
     */
    fun formatDuration(seconds: Int): String {
        val absSeconds = abs(seconds)
        val hours = absSeconds / 3600
        val minutes = (absSeconds % 3600) / 60
        val seconds = absSeconds % 3600 % 60

        return when {
            hours > 0 && minutes > 0 && seconds > 0 -> "${hours}시간 ${minutes}분 ${seconds}초"
            hours > 0 && minutes > 0 -> "${hours}시간 ${minutes}분"
            hours > 0 -> "${hours}시간"
            minutes > 0 -> "${minutes}분"
            else -> "${seconds}초"
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
     * Timestamp를 "2025년 10월 25일" 형식으로 변환
     */
    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * 타이머용 "25:00" 형식
     */
    fun formatTimer(seconds: Int): String {
        val absSeconds = abs(seconds)
        val minutes = absSeconds / 60
        val secs = absSeconds % 60
        return String.Companion.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }

    /**
     * 퍼센트 포맷팅
     */
    fun formatPercentage(value: Float): String {
        return String.Companion.format(Locale.getDefault(), "%.0f%%", value * 100)
    }


    fun Int.secondsToMillis(): Long = this * 1000L
    fun Int.minutesToMillis(): Long = this * 60 * 1.secondsToMillis()
    fun Int.hoursToMillis(): Long = this * 60 * 1.minutesToMillis()
}