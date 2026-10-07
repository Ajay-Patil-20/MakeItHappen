package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityHighContainer
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityLowContainer
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.PriorityMediumContainer
import com.example.util.DateUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: TaskEntity,
    categoryColorHex: String?,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean = true,
    canMoveDown: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isOverdue = DateUtils.isOverdue(task.dueDate, task.dueTime, task.isCompleted)
    var showMenu by remember { mutableStateOf(false) }

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            task.isCompleted -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            isOverdue -> PriorityHigh.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        },
        label = "cardBorderColor"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.6f else 1.0f,
        label = "contentAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(cardBorderColor),
            width = if (isOverdue) 1.5.dp else 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .alpha(contentAlpha)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Interactive Custom Checkbox
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("task_checkbox_${task.id}")
                        .clickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    val checkboxScale by animateFloatAsState(
                        targetValue = if (task.isCompleted) 1.05f else 1.0f,
                        label = "checkboxScale"
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .scale(checkboxScale)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                if (task.isCompleted) EmeraldAccent else Color.Transparent
                            )
                            .border(
                                width = 2.dp,
                                color = if (task.isCompleted) EmeraldAccent else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(7.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title and notes
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("task_edit_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("task_delete_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Task",
                            tint = PriorityHigh,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Duplicate Task") },
                                onClick = {
                                    showMenu = false
                                    onDuplicate()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                }
                            )
                            if (canMoveUp) {
                                DropdownMenuItem(
                                    text = { Text("Move Up") },
                                    onClick = {
                                        showMenu = false
                                        onMoveUp()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                                    }
                                )
                            }
                            if (canMoveDown) {
                                DropdownMenuItem(
                                    text = { Text("Move Down") },
                                    onClick = {
                                        showMenu = false
                                        onMoveDown()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips Row (Priority, Due Date, Category, Reminder)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Priority Badge
                PriorityBadge(priority = task.priority)

                // Category Badge
                CategoryBadge(
                    categoryName = task.category,
                    colorHex = categoryColorHex ?: "#6366F1"
                )

                // Due Date Badge
                if (task.dueDate != null) {
                    DueDateBadge(
                        dueDate = task.dueDate,
                        dueTime = task.dueTime,
                        isOverdue = isOverdue
                    )
                }

                // Reminder Indicator
                if (task.hasReminder) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Reminder Set",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reminder",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (containerColor, textColor) = when (priority.lowercase()) {
        "high" -> PriorityHighContainer to PriorityHigh
        "medium" -> PriorityMediumContainer to PriorityMedium
        else -> PriorityLowContainer to PriorityLow
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = priority,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = textColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CategoryBadge(categoryName: String, colorHex: String) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = parsedColor.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = parsedColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun DueDateBadge(
    dueDate: Long,
    dueTime: String?,
    isOverdue: Boolean
) {
    val formattedDate = DateUtils.formatDueDate(dueDate)
    val formattedTime = DateUtils.formatDueTime(dueTime)
    val labelText = if (formattedTime.isNotBlank()) "$formattedDate, $formattedTime" else formattedDate

    val containerColor = if (isOverdue) PriorityHighContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isOverdue) PriorityHigh else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                contentDescription = if (isOverdue) "Overdue" else "Due date",
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isOverdue) "Overdue ($labelText)" else labelText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor,
                fontSize = 11.sp
            )
        }
    }
}
