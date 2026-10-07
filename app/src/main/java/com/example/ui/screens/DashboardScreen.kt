package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.QuickAddBar
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.SkyAccent
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.NavigationItem
import com.example.util.DateUtils

@Composable
fun DashboardScreen(
    metrics: DashboardMetrics,
    todayTasks: List<TaskEntity>,
    categories: List<CategoryEntity>,
    quickAddText: String,
    onQuickAddChange: (String) -> Unit,
    onQuickAddSubmit: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onNavigate: (NavigationItem) -> Unit,
    onToggleComplete: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDuplicateTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onMoveUp: (TaskEntity) -> Unit,
    onMoveDown: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColorMap = categories.associate { it.name to it.colorHex }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Quick Add Bar at the top of dashboard
            QuickAddBar(
                value = quickAddText,
                onValueChange = onQuickAddChange,
                onQuickAdd = onQuickAddSubmit,
                onOpenFullForm = onOpenCreateDialog
            )
        }

        // Hero Progress Banner
        item {
            ProgressHeroBanner(
                metrics = metrics,
                onAddClick = onOpenCreateDialog
            )
        }

        // Metrics Grid (5 Stat cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Tasks",
                        count = metrics.totalTasks,
                        icon = Icons.Default.FormatListBulleted,
                        accentColor = PrimaryIndigo,
                        onClick = { onNavigate(NavigationItem.ALL_TASKS) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active",
                        count = metrics.activeTasks,
                        icon = Icons.Default.HourglassBottom,
                        accentColor = SkyAccent,
                        onClick = { onNavigate(NavigationItem.ALL_TASKS) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Completed",
                        count = metrics.completedTasks,
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldAccent,
                        onClick = { onNavigate(NavigationItem.COMPLETED) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Today's Tasks",
                        count = metrics.todayTasks,
                        icon = Icons.Default.Today,
                        accentColor = PrimaryIndigo,
                        subtitle = "${metrics.todayCompletedTasks} done",
                        onClick = { onNavigate(NavigationItem.TODAY) },
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Overdue",
                        count = metrics.overdueTasks,
                        icon = Icons.Default.Warning,
                        accentColor = if (metrics.overdueTasks > 0) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant,
                        subtitle = if (metrics.overdueTasks > 0) "Needs attention" else "All up to date",
                        onClick = { onNavigate(NavigationItem.OVERDUE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Today's Priority Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Focus",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View All (${todayTasks.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigate(NavigationItem.TODAY) }
                )
            }
        }

        if (todayTasks.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Nothing scheduled for today",
                    subtitle = "Plan ahead or take a well-deserved breather!",
                    icon = Icons.Default.Today,
                    actionButtonText = "Schedule a Task for Today",
                    onActionClick = onOpenCreateDialog
                )
            }
        } else {
            items(todayTasks, key = { it.id }) { task ->
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
            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom bar / FAB
        }
    }
}

@Composable
fun ProgressHeroBanner(
    metrics: DashboardMetrics,
    onAddClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = metrics.todayCompletionPercentage / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "heroProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_progress_banner"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (metrics.todayCompletionPercentage == 100 && metrics.todayTasks > 0) {
                            "All Done For Today!"
                        } else if (metrics.todayTasks > 0) {
                            "You're ${metrics.todayCompletionPercentage}% done today!"
                        } else {
                            "Ready to Make It Happen?"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (metrics.todayTasks > 0) {
                            "${metrics.todayCompletedTasks} of ${metrics.todayTasks} tasks completed today"
                        } else {
                            "Add your tasks for today to begin tracking progress"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier.size(70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = EmeraldAccent,
                        strokeWidth = 7.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${metrics.todayCompletionPercentage}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("metric_card_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (title == "Overdue" && count > 0) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }
    }
}
