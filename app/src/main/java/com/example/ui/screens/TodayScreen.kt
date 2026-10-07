package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddBar
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.viewmodel.DashboardMetrics
import com.example.util.DateUtils

@Composable
fun TodayScreen(
    tasks: List<TaskEntity>,
    overdueTasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    metrics: DashboardMetrics,
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
    val remainingToday = tasks.filter { !it.isCompleted }
    val completedToday = tasks.filter { it.isCompleted }

    val progressAnim by animateFloatAsState(
        targetValue = metrics.todayCompletionPercentage / 100f,
        animationSpec = tween(500),
        label = "todayProgress"
    )

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

        // Today's Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_progress_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Agenda",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${remainingToday.size} remaining • ${completedToday.size} completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${metrics.todayCompletionPercentage}%",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { progressAnim },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Overdue Alert Banner if applicable
        if (overdueTasks.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PriorityHighContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PriorityHigh
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                        Column {
                            Text(
                                text = "${overdueTasks.size} Overdue Task${if (overdueTasks.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PriorityHigh
                            )
                            Text(
                                text = "Past due date and pending completion",
                                style = MaterialTheme.typography.bodySmall,
                                color = PriorityHigh.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
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

        // Remaining Tasks Section
        item {
            Text(
                text = "Remaining Tasks (${remainingToday.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (remainingToday.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (completedToday.isNotEmpty()) "All done for today!" else "No tasks for today",
                    subtitle = if (completedToday.isNotEmpty()) "Terrific work completing all today's tasks!" else "Add tasks using the field above.",
                    icon = Icons.Default.Today,
                    actionButtonText = if (completedToday.isEmpty()) "Add Task for Today" else null,
                    onActionClick = onOpenCreateDialog
                )
            }
        } else {
            items(remainingToday, key = { it.id }) { task ->
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

        // Completed Section
        if (completedToday.isNotEmpty()) {
            item {
                Text(
                    text = "Completed Today (${completedToday.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(completedToday, key = { it.id }) { task ->
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

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
