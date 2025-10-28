import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * 시간 포맷팅 유틸리티
 */
object TimeFormatter {
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
     * 초를 "1:30:00" 형식으로 변환
     */
    fun formatDurationWithColon(seconds: Int): String {
        val absSeconds = abs(seconds)
        val hours = absSeconds / 3600
        val minutes = (absSeconds % 3600) / 60
        val secs = absSeconds % 60

        return when {
            hours > 0 -> String.Companion.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
            else -> String.Companion.format(Locale.getDefault(), "%d:%02d", minutes, secs)
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
     * Timestamp를 "Oct 20, 2025" 형식으로 변환
     */
    fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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
     * 분을 "25 min" 형식으로 변환
     */
    fun formatMinutes(minutes: Int): String {
        return "$minutes min"
    }

    /**
     * 초를 분 단위로 변환 (소수점 첫째자리)
     */
    fun secondsToMinutes(seconds: Int): String {
        val minutes = seconds / 60.0
        return String.Companion.format(Locale.getDefault(), "%.1f", minutes)
    }

    /**
     * 퍼센트 포맷팅
     */
    fun formatPercentage(value: Float): String {
        return String.Companion.format(Locale.getDefault(), "%.0f%%", value * 100)
    }

    fun Int.seconds(): Long = this * 1000L
    fun Int.minutes(): Long = this * 60 * 1.seconds()
    fun Int.hours(): Long = this * 60 * 1.minutes()
}