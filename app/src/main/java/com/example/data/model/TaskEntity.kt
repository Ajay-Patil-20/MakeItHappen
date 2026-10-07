package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long? = null,        // Epoch millis of selected date (UTC midnight or local midnight)
    val dueTime: String? = null,       // e.g. "09:00", "14:30"
    val priority: String = "Medium",  // "Low", "Medium", "High"
    val category: String = "Personal",
    val hasReminder: Boolean = false,
    val reminderTime: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val orderIndex: Int = 0
)

enum class PriorityLevel(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    companion object {
        fun fromString(value: String): PriorityLevel {
            return entries.firstOrNull { it.label.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
