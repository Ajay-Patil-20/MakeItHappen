package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.CategoryManagementDialog
import com.example.ui.components.ClearAllDataConfirmDialog
import com.example.ui.components.ClearCompletedConfirmDialog
import com.example.ui.components.DeleteTaskConfirmDialog
import com.example.ui.components.DrawerContent
import com.example.ui.components.KeyboardShortcutsDialog
import com.example.ui.components.TaskCreateEditDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TaskListScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.UpcomingScreen
import com.example.ui.theme.MakeItHappenTheme
import com.example.ui.viewmodel.MakeItHappenViewModel
import com.example.ui.viewmodel.NavigationItem
import com.example.util.DateUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            MakeItHappenViewModel.Factory(application)
        )[MakeItHappenViewModel::class.java]

        setContent {
            val themeMode by viewModel.preferencesManager.themeMode.collectAsStateWithLifecycle()

            MakeItHappenTheme(themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(viewModel: MakeItHappenViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    // State collections
    val currentNav by viewModel.currentNav.collectAsStateWithLifecycle()
    val displayedTasks by viewModel.displayedTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val categories by viewModel.allCategories.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedPriority by viewModel.selectedPriority.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val quickAddTitle by viewModel.quickAddTitle.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    // Dialog state collections
    val isCreateEditDialogOpen by viewModel.isCreateEditDialogOpen.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val taskToDelete by viewModel.taskToDelete.collectAsStateWithLifecycle()
    val isClearCompletedConfirmOpen by viewModel.isClearCompletedConfirmOpen.collectAsStateWithLifecycle()
    val isClearAllConfirmOpen by viewModel.isClearAllConfirmOpen.collectAsStateWithLifecycle()
    val isCategoryManagementOpen by viewModel.isCategoryManagementOpen.collectAsStateWithLifecycle()
    val isKeyboardShortcutsOpen by viewModel.isKeyboardShortcutsOpen.collectAsStateWithLifecycle()

    // Notification Permission Launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showMessage("Notifications enabled")
        } else {
            viewModel.showMessage("Notification permission denied")
        }
    }

    // Trigger permission check once if notifications are enabled and Android 13+
    val notificationsEnabled by viewModel.preferencesManager.notificationsEnabled.collectAsStateWithLifecycle()
    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Handle Snackbars
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Back handling
    if (drawerState.isOpen) {
        BackHandler {
            coroutineScope.launch { drawerState.close() }
        }
    } else if (currentNav != NavigationItem.DASHBOARD) {
        BackHandler {
            viewModel.setNavigation(NavigationItem.DASHBOARD)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // Tablet / Desktop layout: Side Navigation Drawer + Main Content
            Row(modifier = Modifier.fillMaxSize()) {
                DrawerContent(
                    currentNav = currentNav,
                    onNavSelected = { viewModel.setNavigation(it) },
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    onOpenCategoryManagement = { viewModel.openCategoryManagement() },
                    metrics = metrics
                )

                Scaffold(
                    modifier = Modifier.weight(1f),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = when (currentNav) {
                                        NavigationItem.DASHBOARD -> "Dashboard"
                                        NavigationItem.ALL_TASKS -> if (selectedCategory != null) "Tasks • $selectedCategory" else "My Tasks"
                                        NavigationItem.TODAY -> "Today"
                                        NavigationItem.UPCOMING -> "Upcoming"
                                        NavigationItem.COMPLETED -> "Completed"
                                        NavigationItem.OVERDUE -> "Overdue Tasks"
                                        NavigationItem.SETTINGS -> "Settings"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            actions = {
                                IconButton(onClick = { viewModel.openKeyboardShortcuts() }) {
                                    Icon(Icons.Default.Keyboard, contentDescription = "Shortcuts")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    floatingActionButton = {
                        if (currentNav != NavigationItem.SETTINGS) {
                            FloatingActionButton(
                                onClick = { viewModel.openCreateDialog() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("floating_add_task_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add New Task")
                            }
                        }
                    }
                ) { innerPadding ->
                    ScreenContent(
                        currentNav = currentNav,
                        viewModel = viewModel,
                        displayedTasks = displayedTasks,
                        allTasks = allTasks,
                        categories = categories,
                        metrics = metrics,
                        searchQuery = searchQuery,
                        statusFilter = statusFilter,
                        selectedCategory = selectedCategory,
                        selectedPriority = selectedPriority,
                        sortOption = sortOption,
                        quickAddTitle = quickAddTitle,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        } else {
            // Mobile layout: Modal Navigation Drawer + Scaffold with Bottom Bar
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerContent(
                            currentNav = currentNav,
                            onNavSelected = {
                                viewModel.setNavigation(it)
                                coroutineScope.launch { drawerState.close() }
                            },
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = {
                                viewModel.setCategoryFilter(it)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onOpenCategoryManagement = {
                                viewModel.openCategoryManagement()
                                coroutineScope.launch { drawerState.close() }
                            },
                            metrics = metrics
                        )
                    }
                }
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = " MakeItHappen",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { coroutineScope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("open_drawer_button")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Open Navigation Menu")
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.openKeyboardShortcuts() }) {
                                    Icon(Icons.Default.Keyboard, contentDescription = "Shortcuts")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        AppBottomNavigationBar(
                            currentNav = currentNav,
                            onNavSelected = { viewModel.setNavigation(it) },
                            metrics = metrics
                        )
                    },
                    floatingActionButton = {
                        if (currentNav != NavigationItem.SETTINGS) {
                            FloatingActionButton(
                                onClick = { viewModel.openCreateDialog() },
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.testTag("floating_add_task_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add New Task")
                            }
                        }
                    }
                ) { innerPadding ->
                    ScreenContent(
                        currentNav = currentNav,
                        viewModel = viewModel,
                        displayedTasks = displayedTasks,
                        allTasks = allTasks,
                        categories = categories,
                        metrics = metrics,
                        searchQuery = searchQuery,
                        statusFilter = statusFilter,
                        selectedCategory = selectedCategory,
                        selectedPriority = selectedPriority,
                        sortOption = sortOption,
                        quickAddTitle = quickAddTitle,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (isCreateEditDialogOpen) {
        val defaultCat by viewModel.preferencesManager.defaultCategory.collectAsStateWithLifecycle()
        val defaultPrio by viewModel.preferencesManager.defaultPriority.collectAsStateWithLifecycle()

        TaskCreateEditDialog(
            initialTask = editingTask,
            categories = categories,
            defaultCategory = defaultCat,
            defaultPriority = defaultPrio,
            onDismiss = { viewModel.closeCreateEditDialog() },
            onSave = { id, title, desc, dueDate, dueTime, priority, category, hasReminder ->
                viewModel.saveTask(id, title, desc, dueDate, dueTime, priority, category, hasReminder)
            }
        )
    }

    taskToDelete?.let { task ->
        DeleteTaskConfirmDialog(
            task = task,
            onConfirm = { viewModel.executeDeleteTask() },
            onDismiss = { viewModel.dismissDeleteTask() }
        )
    }

    if (isClearCompletedConfirmOpen) {
        ClearCompletedConfirmDialog(
            onConfirm = { viewModel.clearCompletedTasks() },
            onDismiss = { viewModel.closeClearCompletedDialog() }
        )
    }

    if (isClearAllConfirmOpen) {
        ClearAllDataConfirmDialog(
            onConfirm = { viewModel.clearAllData() },
            onDismiss = { viewModel.closeClearAllDialog() }
        )
    }

    if (isCategoryManagementOpen) {
        CategoryManagementDialog(
            categories = categories,
            onAddCategory = { name, colorHex -> viewModel.addCustomCategory(name, colorHex) },
            onDeleteCategory = { name -> viewModel.deleteCustomCategory(name) },
            onDismiss = { viewModel.closeCategoryManagement() }
        )
    }

    if (isKeyboardShortcutsOpen) {
        KeyboardShortcutsDialog(onDismiss = { viewModel.closeKeyboardShortcuts() })
    }
}

@Composable
fun ScreenContent(
    currentNav: NavigationItem,
    viewModel: MakeItHappenViewModel,
    displayedTasks: List<com.example.data.model.TaskEntity>,
    allTasks: List<com.example.data.model.TaskEntity>,
    categories: List<com.example.data.model.CategoryEntity>,
    metrics: com.example.ui.viewmodel.DashboardMetrics,
    searchQuery: String,
    statusFilter: com.example.ui.viewmodel.StatusFilter,
    selectedCategory: String?,
    selectedPriority: String?,
    sortOption: com.example.ui.viewmodel.SortOption,
    quickAddTitle: String,
    modifier: Modifier = Modifier
) {
    val todayTasks = remember(allTasks) {
        allTasks.filter { DateUtils.isDueToday(it.dueDate) }
    }
    val overdueTasks = remember(allTasks) {
        allTasks.filter { DateUtils.isOverdue(it.dueDate, it.dueTime, it.isCompleted) }
    }
    val upcomingTasks = remember(allTasks) {
        allTasks.filter { DateUtils.isUpcoming(it.dueDate) }
    }

    when (currentNav) {
        NavigationItem.DASHBOARD -> {
            DashboardScreen(
                metrics = metrics,
                todayTasks = todayTasks,
                categories = categories,
                quickAddText = quickAddTitle,
                onQuickAddChange = { viewModel.setQuickAddTitle(it) },
                onQuickAddSubmit = { viewModel.quickAddTask() },
                onOpenCreateDialog = { viewModel.openCreateDialog() },
                onNavigate = { viewModel.setNavigation(it) },
                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                onEditTask = { viewModel.openEditDialog(it) },
                onDuplicateTask = { viewModel.duplicateTask(it) },
                onDeleteTask = { viewModel.confirmDeleteTask(it) },
                onMoveUp = { viewModel.moveTaskUp(it) },
                onMoveDown = { viewModel.moveTaskDown(it) },
                modifier = modifier
            )
        }

        NavigationItem.ALL_TASKS -> {
            TaskListScreen(
                tasks = displayedTasks,
                categories = categories,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                statusFilter = statusFilter,
                onStatusFilterChange = { viewModel.setStatusFilter(it) },
                selectedCategory = selectedCategory,
                onCategoryChange = { viewModel.setCategoryFilter(it) },
                selectedPriority = selectedPriority,
                onPriorityChange = { viewModel.setPriorityFilter(it) },
                sortOption = sortOption,
                onSortOptionChange = { viewModel.setSortOption(it) },
                quickAddText = quickAddTitle,
                onQuickAddChange = { viewModel.setQuickAddTitle(it) },
                onQuickAddSubmit = { viewModel.quickAddTask() },
                onOpenCreateDialog = { viewModel.openCreateDialog() },
                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                onEditTask = { viewModel.openEditDialog(it) },
                onDuplicateTask = { viewModel.duplicateTask(it) },
                onDeleteTask = { viewModel.confirmDeleteTask(it) },
                onMoveUp = { viewModel.moveTaskUp(it) },
                onMoveDown = { viewModel.moveTaskDown(it) },
                screenTitle = if (selectedCategory != null) "Tasks in $selectedCategory" else "My Tasks",
                modifier = modifier
            )
        }

        NavigationItem.TODAY -> {
            TodayScreen(
                tasks = displayedTasks,
                overdueTasks = overdueTasks,
                categories = categories,
                metrics = metrics,
                quickAddText = quickAddTitle,
                onQuickAddChange = { viewModel.setQuickAddTitle(it) },
                onQuickAddSubmit = { viewModel.quickAddTask() },
                onOpenCreateDialog = { viewModel.openCreateDialog() },
                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                onEditTask = { viewModel.openEditDialog(it) },
                onDuplicateTask = { viewModel.duplicateTask(it) },
                onDeleteTask = { viewModel.confirmDeleteTask(it) },
                onMoveUp = { viewModel.moveTaskUp(it) },
                onMoveDown = { viewModel.moveTaskDown(it) },
                modifier = modifier
            )
        }

        NavigationItem.UPCOMING -> {
            UpcomingScreen(
                tasks = displayedTasks,
                categories = categories,
                quickAddText = quickAddTitle,
                onQuickAddChange = { viewModel.setQuickAddTitle(it) },
                onQuickAddSubmit = { viewModel.quickAddTask() },
                onOpenCreateDialog = { viewModel.openCreateDialog() },
                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                onEditTask = { viewModel.openEditDialog(it) },
                onDuplicateTask = { viewModel.duplicateTask(it) },
                onDeleteTask = { viewModel.confirmDeleteTask(it) },
                onMoveUp = { viewModel.moveTaskUp(it) },
                onMoveDown = { viewModel.moveTaskDown(it) },
                modifier = modifier
            )
        }

        NavigationItem.COMPLETED -> {
            val completedList = displayedTasks.filter { it.isCompleted }
            TaskListScreen(
                tasks = completedList,
                categories = categories,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                statusFilter = com.example.ui.viewmodel.StatusFilter.COMPLETED,
                onStatusFilterChange = { viewModel.setStatusFilter(it) },
                selectedCategory = selectedCategory,
                onCategoryChange = { viewModel.setCategoryFilter(it) },
                selectedPriority = selectedPriority,
                onPriorityChange = { viewModel.setPriorityFilter(it) },
                sortOption = sortOption,
                onSortOptionChange = { viewModel.setSortOption(it) },
                quickAddText = quickAddTitle,
                onQuickAddChange = { viewModel.setQuickAddTitle(it) },
                onQuickAddSubmit = { viewModel.quickAddTask() },
                onOpenCreateDialog = { viewModel.openCreateDialog() },
                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                onEditTask = { viewModel.openEditDialog(it) },
                onDuplicateTask = { viewModel.duplicateTask(it) },
                onDeleteTask = { viewModel.confirmDeleteTask(it) },
                onMoveUp = { viewModel.moveTaskUp(it) },
                onMoveDown = { viewModel.moveTaskDown(it) },
                screenTitle = "Completed Tasks",
                modifier = modifier
            )
        }

        NavigationItem.OVERDUE -> {
            TaskListScreen(
                tasks = overdueTasks,
                categories = categories,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                statusFilter = statusFilter,
                onStatusFilterChange = { viewModel.setStatusFilter(it) },
                selectedCategory = selectedCategory,
                onCategoryChange = { viewModel.setCategoryFilter(it) },
                selectedPriority = selectedPriority,
                onPriorityChange = { viewModel.setPriorityFilter(it) },
                sortOption = sortOption,
                onSortOptionChange = { viewModel.setSortOption(it) },
                quickAddText = quickAddTitle,
                onQuickAddChange = { viewModel.setQuickAddTitle(it) },
                onQuickAddSubmit = { viewModel.quickAddTask() },
                onOpenCreateDialog = { viewModel.openCreateDialog() },
                onToggleComplete = { viewModel.toggleTaskCompletion(it) },
                onEditTask = { viewModel.openEditDialog(it) },
                onDuplicateTask = { viewModel.duplicateTask(it) },
                onDeleteTask = { viewModel.confirmDeleteTask(it) },
                onMoveUp = { viewModel.moveTaskUp(it) },
                onMoveDown = { viewModel.moveTaskDown(it) },
                screenTitle = "Overdue Tasks",
                modifier = modifier
            )
        }

        NavigationItem.SETTINGS -> {
            val themeMode by viewModel.preferencesManager.themeMode.collectAsStateWithLifecycle()
            val defaultPriorityPref by viewModel.preferencesManager.defaultPriority.collectAsStateWithLifecycle()
            val defaultCategoryPref by viewModel.preferencesManager.defaultCategory.collectAsStateWithLifecycle()
            val notifEnabledPref by viewModel.preferencesManager.notificationsEnabled.collectAsStateWithLifecycle()

            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = { viewModel.preferencesManager.setThemeMode(it) },
                defaultPriority = defaultPriorityPref,
                onDefaultPriorityChange = { viewModel.preferencesManager.setDefaultPriority(it) },
                defaultCategory = defaultCategoryPref,
                onDefaultCategoryChange = { viewModel.preferencesManager.setDefaultCategory(it) },
                notificationsEnabled = notifEnabledPref,
                onNotificationsEnabledChange = { viewModel.preferencesManager.setNotificationsEnabled(it) },
                categories = categories,
                onSendTestNotification = { viewModel.sendTestNotification() },
                onOpenClearCompletedConfirm = { viewModel.openClearCompletedDialog() },
                onOpenClearAllConfirm = { viewModel.openClearAllDialog() },
                onResetSampleTasks = { viewModel.resetToSampleTasks() },
                onOpenKeyboardShortcuts = { viewModel.openKeyboardShortcuts() },
                modifier = modifier
            )
        }
    }
}
