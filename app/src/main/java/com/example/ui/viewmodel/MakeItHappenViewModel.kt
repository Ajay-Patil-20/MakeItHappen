package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.MakeItHappenDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.PriorityLevel
import com.example.data.model.TaskEntity
import com.example.data.preferences.PreferencesManager
import com.example.data.repository.TaskRepository
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationItem(val title: String) {
    DASHBOARD("Dashboard"),
    ALL_TASKS("My Tasks"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    OVERDUE("Overdue"),
    SETTINGS("Settings")
}

enum class StatusFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}

enum class SortOption(val label: String) {
    MANUAL("Custom Order"),
    DUE_DATE_ASC("Due Date (Earliest)"),
    DUE_DATE_DESC("Due Date (Latest)"),
    PRIORITY_HIGH_FIRST("Priority (High to Low)"),
    PRIORITY_LOW_FIRST("Priority (Low to High)"),
    CREATION_NEWEST("Newest Created"),
    CREATION_OLDEST("Oldest Created"),
    ALPHABETICAL_ASC("Title (A to Z)"),
    ALPHABETICAL_DESC("Title (Z to A)")
}

data class DashboardMetrics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val activeTasks: Int = 0,
    val overdueTasks: Int = 0,
    val todayTasks: Int = 0,
    val todayCompletedTasks: Int = 0,
    val todayCompletionPercentage: Int = 0,
    val overallCompletionPercentage: Int = 0
)

class MakeItHappenViewModel(
    application: Application,
    private val repository: TaskRepository,
    val preferencesManager: PreferencesManager,
    private val notificationHelper: NotificationHelper
) : AndroidViewModel(application) {

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state filters
    private val _currentNav = MutableStateFlow(NavigationItem.DASHBOARD)
    val currentNav: StateFlow<NavigationItem> = _currentNav.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow(StatusFilter.ALL)
    val statusFilter: StateFlow<StatusFilter> = _statusFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedPriority = MutableStateFlow<String?>(null)
    val selectedPriority: StateFlow<String?> = _selectedPriority.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.MANUAL)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Dialog & Feedback States
    private val _isCreateEditDialogOpen = MutableStateFlow(false)
    val isCreateEditDialogOpen: StateFlow<Boolean> = _isCreateEditDialogOpen.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingTask: StateFlow<TaskEntity?> = _editingTask.asStateFlow()

    private val _taskToDelete = MutableStateFlow<TaskEntity?>(null)
    val taskToDelete: StateFlow<TaskEntity?> = _taskToDelete.asStateFlow()

    private val _isClearCompletedConfirmOpen = MutableStateFlow(false)
    val isClearCompletedConfirmOpen: StateFlow<Boolean> = _isClearCompletedConfirmOpen.asStateFlow()

    private val _isClearAllConfirmOpen = MutableStateFlow(false)
    val isClearAllConfirmOpen: StateFlow<Boolean> = _isClearAllConfirmOpen.asStateFlow()

    private val _isCategoryManagementOpen = MutableStateFlow(false)
    val isCategoryManagementOpen: StateFlow<Boolean> = _isCategoryManagementOpen.asStateFlow()

    private val _isKeyboardShortcutsOpen = MutableStateFlow(false)
    val isKeyboardShortcutsOpen: StateFlow<Boolean> = _isKeyboardShortcutsOpen.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Quick add field
    private val _quickAddTitle = MutableStateFlow("")
    val quickAddTitle: StateFlow<String> = _quickAddTitle.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    // Dashboard Metrics
    val dashboardMetrics: StateFlow<DashboardMetrics> = allTasks.combine(_currentNav) { tasks, _ ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val active = total - completed
        val overdue = tasks.count { DateUtils.isOverdue(it.dueDate, it.dueTime, it.isCompleted) }
        val todayTasksList = tasks.filter { DateUtils.isDueToday(it.dueDate) }
        val todayTotal = todayTasksList.size
        val todayCompleted = todayTasksList.count { it.isCompleted }

        val todayPercent = if (todayTotal > 0) ((todayCompleted.toFloat() / todayTotal) * 100).toInt() else if (total > 0 && active == 0) 100 else 0
        val overallPercent = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0

        DashboardMetrics(
            totalTasks = total,
            completedTasks = completed,
            activeTasks = active,
            overdueTasks = overdue,
            todayTasks = todayTotal,
            todayCompletedTasks = todayCompleted,
            todayCompletionPercentage = todayPercent,
            overallCompletionPercentage = overallPercent
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    // Filtered and Sorted Tasks for Current View
    val displayedTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        _currentNav,
        _searchQuery,
        _statusFilter,
        _selectedCategory,
        _selectedPriority,
        _sortOption
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val tasks = args[0] as List<TaskEntity>
        val nav = args[1] as NavigationItem
        val query = args[2] as String
        val status = args[3] as StatusFilter
        val catFilter = args[4] as String?
        val priorityFilter = args[5] as String?
        val sort = args[6] as SortOption

        tasks.filter { task ->
            // Navigation section filtering
            val matchesNav = when (nav) {
                NavigationItem.DASHBOARD -> true
                NavigationItem.ALL_TASKS -> true
                NavigationItem.TODAY -> DateUtils.isDueToday(task.dueDate)
                NavigationItem.UPCOMING -> DateUtils.isUpcoming(task.dueDate)
                NavigationItem.COMPLETED -> task.isCompleted
                NavigationItem.OVERDUE -> DateUtils.isOverdue(task.dueDate, task.dueTime, task.isCompleted)
                NavigationItem.SETTINGS -> true
            }

            // Search query filter
            val matchesQuery = if (query.isBlank()) true else {
                task.title.contains(query, ignoreCase = true) ||
                task.description.contains(query, ignoreCase = true) ||
                task.category.contains(query, ignoreCase = true)
            }

            // Status filter (Active/Completed)
            val matchesStatus = when (status) {
                StatusFilter.ALL -> true
                StatusFilter.ACTIVE -> !task.isCompleted
                StatusFilter.COMPLETED -> task.isCompleted
            }

            // Category filter
            val matchesCategory = catFilter == null || task.category.equals(catFilter, ignoreCase = true)

            // Priority filter
            val matchesPriority = priorityFilter == null || task.priority.equals(priorityFilter, ignoreCase = true)

            matchesNav && matchesQuery && matchesStatus && matchesCategory && matchesPriority
        }.let { list ->
            // Sort
            when (sort) {
                SortOption.MANUAL -> list.sortedWith(compareBy({ it.isCompleted }, { it.orderIndex }))
                SortOption.DUE_DATE_ASC -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenBy(nullsLast()) { it.dueDate }
                        .thenBy { it.dueTime ?: "99:99" }
                )
                SortOption.DUE_DATE_DESC -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenByDescending(nullsLast()) { it.dueDate }
                        .thenByDescending { it.dueTime ?: "" }
                )
                SortOption.PRIORITY_HIGH_FIRST -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenBy {
                            when (it.priority) {
                                "High" -> 0
                                "Medium" -> 1
                                else -> 2
                            }
                        }
                )
                SortOption.PRIORITY_LOW_FIRST -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenBy {
                            when (it.priority) {
                                "Low" -> 0
                                "Medium" -> 1
                                else -> 2
                            }
                        }
                )
                SortOption.CREATION_NEWEST -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenByDescending { it.createdAt }
                )
                SortOption.CREATION_OLDEST -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenBy { it.createdAt }
                )
                SortOption.ALPHABETICAL_ASC -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenBy { it.title.lowercase() }
                )
                SortOption.ALPHABETICAL_DESC -> list.sortedWith(
                    compareBy<TaskEntity> { it.isCompleted }
                        .thenByDescending { it.title.lowercase() }
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and Filters
    fun setNavigation(nav: NavigationItem) {
        _currentNav.value = nav
        if (nav == NavigationItem.COMPLETED) {
            _statusFilter.value = StatusFilter.COMPLETED
        } else if (_statusFilter.value == StatusFilter.COMPLETED) {
            _statusFilter.value = StatusFilter.ALL
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: StatusFilter) {
        _statusFilter.value = status
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setPriorityFilter(priority: String?) {
        _selectedPriority.value = priority
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun setQuickAddTitle(title: String) {
        _quickAddTitle.value = title
    }

    // Task CRUD Operations
    fun quickAddTask() {
        val title = _quickAddTitle.value.trim()
        if (title.isBlank()) return

        viewModelScope.launch {
            val defaultCat = preferencesManager.defaultCategory.value
            val defaultPrio = preferencesManager.defaultPriority.value
            val currentList = allTasks.value
            val maxOrder = currentList.maxOfOrNull { it.orderIndex } ?: 0

            val task = TaskEntity(
                title = title,
                category = defaultCat,
                priority = defaultPrio,
                dueDate = DateUtils.getTodayStartOfDay(),
                orderIndex = maxOrder + 1
            )
            repository.insertTask(task)
            _quickAddTitle.value = ""
            showMessage("Task \"$title\" added!")
        }
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        description: String,
        dueDate: Long?,
        dueTime: String?,
        priority: String,
        category: String,
        hasReminder: Boolean
    ) {
        if (title.isBlank()) {
            showMessage("Task title is required")
            return
        }

        viewModelScope.launch {
            if (id == 0L) {
                // New task
                val currentList = allTasks.value
                val maxOrder = currentList.maxOfOrNull { it.orderIndex } ?: 0
                val task = TaskEntity(
                    title = title.trim(),
                    description = description.trim(),
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority,
                    category = category,
                    hasReminder = hasReminder,
                    orderIndex = maxOrder + 1
                )
                val newId = repository.insertTask(task)
                showMessage("Task created successfully")
                if (hasReminder && preferencesManager.notificationsEnabled.value) {
                    notificationHelper.showReminderNotification(
                        taskId = newId,
                        title = "Reminder: $title",
                        message = if (description.isNotBlank()) description else "Scheduled for $category"
                    )
                }
            } else {
                // Update task
                val existing = _editingTask.value
                val updated = existing?.copy(
                    title = title.trim(),
                    description = description.trim(),
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority,
                    category = category,
                    hasReminder = hasReminder
                ) ?: TaskEntity(
                    id = id,
                    title = title.trim(),
                    description = description.trim(),
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority,
                    category = category,
                    hasReminder = hasReminder
                )
                repository.updateTask(updated)
                showMessage("Task updated successfully")
            }
            closeCreateEditDialog()
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
            val message = if (!task.isCompleted) "Task completed! Great job!" else "Task marked as active"
            showMessage(message)
        }
    }

    fun duplicateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.duplicateTask(task)
            showMessage("Task duplicated")
        }
    }

    fun confirmDeleteTask(task: TaskEntity) {
        _taskToDelete.value = task
    }

    fun dismissDeleteTask() {
        _taskToDelete.value = null
    }

    fun executeDeleteTask() {
        val task = _taskToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteTask(task)
            _taskToDelete.value = null
            showMessage("Task deleted")
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
            _isClearCompletedConfirmOpen.value = false
            showMessage("Completed tasks cleared")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _isClearAllConfirmOpen.value = false
            showMessage("All tasks and custom categories cleared")
        }
    }

    fun resetToSampleTasks() {
        viewModelScope.launch {
            repository.seedInitialData()
            showMessage("Sample tasks restored!")
        }
    }

    fun moveTaskUp(task: TaskEntity) {
        val list = allTasks.value.sortedBy { it.orderIndex }.toMutableList()
        val index = list.indexOfFirst { it.id == task.id }
        if (index > 0) {
            val prev = list[index - 1]
            list[index - 1] = task
            list[index] = prev
            _sortOption.value = SortOption.MANUAL
            viewModelScope.launch {
                repository.reorderTasks(list)
            }
        }
    }

    fun moveTaskDown(task: TaskEntity) {
        val list = allTasks.value.sortedBy { it.orderIndex }.toMutableList()
        val index = list.indexOfFirst { it.id == task.id }
        if (index >= 0 && index < list.size - 1) {
            val next = list[index + 1]
            list[index + 1] = task
            list[index] = next
            _sortOption.value = SortOption.MANUAL
            viewModelScope.launch {
                repository.reorderTasks(list)
            }
        }
    }

    // Categories
    fun addCustomCategory(name: String, colorHex: String, iconName: String = "folder") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val cat = CategoryEntity(
                name = name.trim(),
                colorHex = colorHex,
                iconName = iconName,
                isDefault = false
            )
            repository.insertCategory(cat)
            showMessage("Category \"$name\" created")
        }
    }

    fun deleteCustomCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCustomCategory(name)
            if (_selectedCategory.value == name) {
                _selectedCategory.value = null
            }
            showMessage("Category \"$name\" deleted")
        }
    }

    // Dialog Toggles
    fun openCreateDialog() {
        _editingTask.value = null
        _isCreateEditDialogOpen.value = true
    }

    fun openEditDialog(task: TaskEntity) {
        _editingTask.value = task
        _isCreateEditDialogOpen.value = true
    }

    fun closeCreateEditDialog() {
        _editingTask.value = null
        _isCreateEditDialogOpen.value = false
    }

    fun openClearCompletedDialog() {
        _isClearCompletedConfirmOpen.value = true
    }

    fun closeClearCompletedDialog() {
        _isClearCompletedConfirmOpen.value = false
    }

    fun openClearAllDialog() {
        _isClearAllConfirmOpen.value = true
    }

    fun closeClearAllDialog() {
        _isClearAllConfirmOpen.value = false
    }

    fun openCategoryManagement() {
        _isCategoryManagementOpen.value = true
    }

    fun closeCategoryManagement() {
        _isCategoryManagementOpen.value = false
    }

    fun openKeyboardShortcuts() {
        _isKeyboardShortcutsOpen.value = true
    }

    fun closeKeyboardShortcuts() {
        _isKeyboardShortcutsOpen.value = false
    }

    // Test Notification
    fun sendTestNotification(): Boolean {
        val sent = notificationHelper.sendTestNotification()
        if (sent) {
            showMessage("Test notification sent!")
        } else {
            showMessage("Notifications disabled or permission not granted")
        }
        return sent
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = MakeItHappenDatabase.getDatabase(application)
            val prefs = PreferencesManager(application)
            val repo = TaskRepository(db.taskDao(), db.categoryDao(), prefs)
            val notif = NotificationHelper(application)
            return MakeItHappenViewModel(application, repo, prefs, notif) as T
        }
    }
}
