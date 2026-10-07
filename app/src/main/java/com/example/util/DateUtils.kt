package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getTodayStartOfDay(): Long = getStartOfDay(System.currentTimeMillis())

    fun getTomorrowStartOfDay(): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun formatDueDate(timestamp: Long?): String {
        if (timestamp == null) return "No due date"
        val startOfTarget = getStartOfDay(timestamp)
        val todayStart = getTodayStartOfDay()
        val tomorrowStart = getTomorrowStartOfDay()

        val dayDiff = ((startOfTarget - todayStart) / (1000 * 60 * 60 * 24)).toInt()

        return when (dayDiff) {
            0 -> "Today"
            1 -> "Tomorrow"
            -1 -> "Yesterday"
            in 2..6 -> {
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                sdf.format(cal.time)
            }
            else -> {
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val targetYear = cal.get(Calendar.YEAR)
                val pattern = if (currentYear == targetYear) "MMM d" else "MMM d, yyyy"
                SimpleDateFormat(pattern, Locale.getDefault()).format(cal.time)
            }
        }
    }

    fun formatDueTime(timeString: String?): String {
        if (timeString.isNullOrBlank()) return ""
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val min = parts[1].toInt()
                val isPm = hour >= 12
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                String.format(Locale.getDefault(), "%d:%02d %s", displayHour, min, if (isPm) "PM" else "AM")
            } else {
                timeString
            }
        } catch (e: Exception) {
            timeString
        }
    }

    fun isOverdue(dueDate: Long?, dueTime: String?, isCompleted: Boolean): Boolean {
        if (isCompleted || dueDate == null) return false

        val now = Calendar.getInstance()
        val todayStart = getTodayStartOfDay()
        val targetDayStart = getStartOfDay(dueDate)

        if (targetDayStart < todayStart) {
            return true
        }

        if (targetDayStart == todayStart && !dueTime.isNullOrBlank()) {
            try {
                val parts = dueTime.split(":")
                if (parts.size >= 2) {
                    val hour = parts[0].toInt()
                    val min = parts[1].toInt()
                    val nowHour = now.get(Calendar.HOUR_OF_DAY)
                    val nowMin = now.get(Calendar.MINUTE)
                    if (hour < nowHour || (hour == nowHour && min < nowMin)) {
                        return true
                    }
                }
            } catch (_: Exception) {}
        }

        return false
    }

    fun isDueToday(dueDate: Long?): Boolean {
        if (dueDate == null) return false
        return getStartOfDay(dueDate) == getTodayStartOfDay()
    }

    fun isDueTomorrow(dueDate: Long?): Boolean {
        if (dueDate == null) return false
        return getStartOfDay(dueDate) == getTomorrowStartOfDay()
    }

    fun isUpcoming(dueDate: Long?): Boolean {
        if (dueDate == null) return false
        return getStartOfDay(dueDate) > getTodayStartOfDay()
    }

    fun formatFullDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
