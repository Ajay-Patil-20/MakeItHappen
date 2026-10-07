package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.TaskDao
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.data.preferences.PreferencesManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val preferencesManager: PreferencesManager
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun initializeDefaultsIfNeeded() {
        if (preferencesManager.isFirstLaunch()) {
            seedInitialData()
        }
    }

    suspend fun seedInitialData() {
        // Ensure default categories exist
        categoryDao.insertCategories(CategoryEntity.DEFAULT_CATEGORIES)

        // Seed realistic sample tasks
        val today = DateUtils.getTodayStartOfDay()
        val tomorrow = DateUtils.getTomorrowStartOfDay()

        val pastDate = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis

        val futureDate1 = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, 3)
        }.timeInMillis

        val futureDate2 = Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, 5)
        }.timeInMillis

        val sampleTasks = listOf(
            TaskEntity(
                title = "Review project documentation",
                description = "Go over architecture specs, API routes, and deployment checklist",
                dueDate = today,
                dueTime = "14:00",
                priority = "High",
                category = "Work",
                hasReminder = true,
                isCompleted = false,
                orderIndex = 0
            ),
            TaskEntity(
                title = "Complete Java practice",
                description = "Work through daily coding challenges on concurrency and streams",
                dueDate = today,
                dueTime = "17:30",
                priority = "Medium",
                category = "Study",
                hasReminder = false,
                isCompleted = false,
                orderIndex = 1
            ),
            TaskEntity(
                title = "Buy groceries",
                description = "Oat milk, fresh fruits, eggs, Greek yogurt, coffee beans",
                dueDate = tomorrow,
                dueTime = "10:00",
                priority = "Low",
                category = "Shopping",
                hasReminder = true,
                isCompleted = false,
                orderIndex = 2
            ),
            TaskEntity(
                title = "Prepare interview questions",
                description = "Draft behavioral and system design prompts for upcoming candidate interviews",
                dueDate = futureDate1,
                dueTime = "15:00",
                priority = "High",
                category = "Work",
                hasReminder = false,
                isCompleted = false,
                orderIndex = 3
            ),
            TaskEntity(
                title = "Call friend",
                description = "Catch up on weekend plans and dinner reservations",
                dueDate = futureDate2,
                dueTime = "19:00",
                priority = "Medium",
                category = "Personal",
                hasReminder = false,
                isCompleted = false,
                orderIndex = 4
            ),
            TaskEntity(
                title = "Submit expense report",
                description = "Attach receipts for flight and hotel booking",
                dueDate = pastDate,
                dueTime = "12:00",
                priority = "High",
                category = "Work",
                hasReminder = false,
                isCompleted = false, // Overdue example
                orderIndex = 5
            ),
            TaskEntity(
                title = "Morning 5k run",
                description = "Completed at neighborhood park track",
                dueDate = today,
                dueTime = "07:00",
                priority = "Medium",
                category = "Health",
                hasReminder = false,
                isCompleted = true,
                completedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4,
                orderIndex = 6
            )
        )

        taskDao.insertTasks(sampleTasks)
    }

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
    }

    suspend fun duplicateTask(task: TaskEntity) {
        val copy = task.copy(
            id = 0,
            title = "${task.title} (Copy)",
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis(),
            orderIndex = task.orderIndex + 1
        )
        taskDao.insertTask(copy)
    }

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    suspend fun clearAllData() {
        taskDao.clearAllTasks()
        categoryDao.clearCustomCategories()
    }

    suspend fun reorderTasks(tasks: List<TaskEntity>) {
        val updatedList = tasks.mapIndexed { index, task ->
            task.copy(orderIndex = index)
        }
        taskDao.updateTasks(updatedList)
    }

    suspend fun insertCategory(category: CategoryEntity) = categoryDao.insertCategory(category)

    suspend fun deleteCustomCategory(name: String) {
        categoryDao.deleteCustomCategoryByName(name)
        taskDao.reassignCategory(name, "Other")
    }
}
