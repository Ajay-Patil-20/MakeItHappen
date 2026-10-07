package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddBar
import com.example.ui.components.TaskItemCard
import com.example.util.DateUtils

@Composable
fun UpcomingScreen(
    tasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
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
    modifier: Modifier = Modifier
) {
    val categoryColorMap = categories.associate { it.name to it.colorHex }

    // Group tasks by their due date start-of-day
    val groupedTasks = tasks
        .filter { it.dueDate != null }
        .groupBy { DateUtils.getStartOfDay(it.dueDate!!) }
        .toSortedMap()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            QuickAddBar(
                value = quickAddText,
                onValueChange = onQuickAddChange,
                onQuickAdd = onQuickAddSubmit,
                onOpenFullForm = onOpenCreateDialog
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upcoming_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Column {
                        Text(
                            text = "Upcoming Timeline",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${tasks.size} future task${if (tasks.size == 1) "" else "s"} on your horizon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (groupedTasks.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No upcoming tasks",
                    subtitle = "Schedule future tasks to keep your goals organized and on track.",
                    icon = Icons.Default.CalendarMonth,
                    actionButtonText = "Schedule a Task",
                    onActionClick = onOpenCreateDialog
                )
            }
        } else {
            groupedTasks.forEach { (dateTimestamp, dateTasks) ->
                val dateLabel = DateUtils.formatDueDate(dateTimestamp)

                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "(${dateTasks.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(dateTasks, key = { it.id }) { task ->
                    TaskItemCard(
                        task = task,
                        categoryColorHex = categoryColorMap[task.category],
                        onToggleComplete = { onToggleComplete(task) },
                        onEdit = { onEditTask(task) },
                        onDuplicate = { onDuplicateTask(task) },
                        onDelete = { onDeleteTask(task) },
                        onMoveUp = { onMoveUp(task) },
                        onMoveDown = { onMoveDown(task) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
