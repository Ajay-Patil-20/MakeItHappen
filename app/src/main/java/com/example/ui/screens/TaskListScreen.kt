package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddBar
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.viewmodel.SortOption
import com.example.ui.viewmodel.StatusFilter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(
    tasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    statusFilter: StatusFilter,
    onStatusFilterChange: (StatusFilter) -> Unit,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit,
    selectedPriority: String?,
    onPriorityChange: (String?) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    quickAddText: String,
    onQuickAddChange: (String) -> Unit,
    onQuickAddSubmit: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDuplicateTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onMoveUp: (TaskEntity) -> Unit,
    onMoveDown: (TaskEntity) -> Unit,
    screenTitle: String = "My Tasks",
    modifier: Modifier = Modifier
) {
    val categoryColorMap = categories.associate { it.name to it.colorHex }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Quick Add Input
            QuickAddBar(
                value = quickAddText,
                onValueChange = onQuickAddChange,
                onQuickAdd = onQuickAddSubmit,
                onOpenFullForm = onOpenCreateDialog
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_bar"),
                placeholder = { Text("Search by title, notes, or category...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            )
        }

        // Filters and Sort Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Horizontally scrollable Status Filter Chips
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusFilter.entries.forEach { status ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { onStatusFilterChange(status) },
                                label = { Text(status.label) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort Button & Menu
                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { isSortMenuOpen = true }
                                .testTag("sort_menu_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort tasks",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sort",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isSortMenuOpen,
                            onDismissRequest = { isSortMenuOpen = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSortOptionChange(option)
                                        isSortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Priority & Category Filter Chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority filter chip toggles
                    listOf("High" to PriorityHigh, "Medium" to PriorityMedium, "Low" to PriorityLow).forEach { (prio, color) ->
                        val isSelected = selectedPriority == prio
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPriorityChange(if (isSelected) null else prio) },
                            label = { Text(prio) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Selected category pill indicator if active
                    if (selectedCategory != null) {
                        FilterChip(
                            selected = true,
                            onClick = { onCategoryChange(null) },
                            label = { Text("Category: $selectedCategory ✕") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Section Title & Counter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${tasks.size} task${if (tasks.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Task Items or Empty State
        if (tasks.isEmpty()) {
            item {
                if (searchQuery.isNotEmpty()) {
                    EmptyStateCard(
                        title = "No tasks found",
                        subtitle = "No tasks match \"$searchQuery\". Try adjusting your search term or filters.",
                        icon = Icons.Default.SearchOff,
                        actionButtonText = "Clear Search",
                        onActionClick = { onSearchQueryChange("") }
                    )
                } else if (statusFilter == StatusFilter.COMPLETED) {
                    EmptyStateCard(
                        title = "Your completed tasks will appear here",
                        subtitle = "Check off tasks as you finish them to track your achievements!",
                        icon = Icons.Default.TaskAlt
                    )
                } else {
                    EmptyStateCard(
                        title = "Nothing on your list. Make it happen!",
                        subtitle = "Add your first task using the quick add bar above or tap + to get started.",
                        icon = Icons.Default.TaskAlt,
                        actionButtonText = "Add Task",
                        onActionClick = onOpenCreateDialog
                    )
                }
            }
        } else {
            itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                TaskItemCard(
                    task = task,
                    categoryColorHex = categoryColorMap[task.category],
                    onToggleComplete = { onToggleComplete(task) },
                    onEdit = { onEditTask(task) },
                    onDuplicate = { onDuplicateTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onMoveUp = { onMoveUp(task) },
                    onMoveDown = { onMoveDown(task) },
                    canMoveUp = index > 0,
                    canMoveDown = index < tasks.size - 1
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
