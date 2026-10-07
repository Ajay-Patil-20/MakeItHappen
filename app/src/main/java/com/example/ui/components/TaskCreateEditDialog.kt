package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCreateEditDialog(
    initialTask: TaskEntity?,
    categories: List<CategoryEntity>,
    defaultCategory: String,
    defaultPriority: String,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        description: String,
        dueDate: Long?,
        dueTime: String?,
        priority: String,
        category: String,
        hasReminder: Boolean
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var dueDate by remember { mutableStateOf<Long?>(initialTask?.dueDate ?: DateUtils.getTodayStartOfDay()) }
    var dueTime by remember { mutableStateOf(initialTask?.dueTime ?: "12:00") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: defaultPriority) }
    var category by remember { mutableStateOf(initialTask?.category ?: defaultCategory) }
    var hasReminder by remember { mutableStateOf(initialTask?.hasReminder ?: false) }
    var titleError by remember { mutableStateOf(false) }

    val today = DateUtils.getTodayStartOfDay()
    val tomorrow = DateUtils.getTomorrowStartOfDay()
    val in2Days = remember {
        Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, 2)
        }.timeInMillis
    }
    val in3Days = remember {
        Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, 3)
        }.timeInMillis
    }
    val nextWeek = remember {
        Calendar.getInstance().apply {
            timeInMillis = today
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("task_dialog_surface"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "Create New Task" else "Edit Task",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError && it.isNotBlank()) titleError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Complete quarterly report") },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("Title cannot be empty", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_description_input"),
                    label = { Text("Notes / Description") },
                    placeholder = { Text("Add extra details, checklist, or links...") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Due Date Section
                Text(
                    text = "Due Date",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = dueDate == null,
                        onClick = { dueDate = null },
                        label = { Text("No Date") },
                        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = dueDate == today,
                        onClick = { dueDate = today },
                        label = { Text("Today") }
                    )
                    FilterChip(
                        selected = dueDate == tomorrow,
                        onClick = { dueDate = tomorrow },
                        label = { Text("Tomorrow") }
                    )
                    FilterChip(
                        selected = dueDate == in2Days,
                        onClick = { dueDate = in2Days },
                        label = { Text("+2 Days") }
                    )
                    FilterChip(
                        selected = dueDate == in3Days,
                        onClick = { dueDate = in3Days },
                        label = { Text("+3 Days") }
                    )
                    FilterChip(
                        selected = dueDate == nextWeek,
                        onClick = { dueDate = nextWeek },
                        label = { Text("Next Week") }
                    )
                    if (dueDate != null && dueDate != today && dueDate != tomorrow && dueDate != in2Days && dueDate != in3Days && dueDate != nextWeek) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("Selected: ${DateUtils.formatDueDate(dueDate)}") },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }

                if (dueDate != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = dueDate!!
                                    add(Calendar.DAY_OF_YEAR, -1)
                                }
                                dueDate = DateUtils.getStartOfDay(cal.timeInMillis)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("-1 Day")
                        }
                        OutlinedButton(
                            onClick = {
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = dueDate!!
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                                dueDate = DateUtils.getStartOfDay(cal.timeInMillis)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+1 Day")
                        }
                        Text(
                            text = DateUtils.formatDueDate(dueDate),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Due Time",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = dueTime.isBlank() || dueTime == "none",
                            onClick = { dueTime = "" },
                            label = { Text("No Time") }
                        )
                        listOf(
                            "09:00" to "9:00 AM",
                            "12:00" to "12:00 PM",
                            "14:00" to "2:00 PM",
                            "18:00" to "6:00 PM",
                            "21:00" to "9:00 PM"
                        ).forEach { (timeVal, label) ->
                            FilterChip(
                                selected = dueTime == timeVal,
                                onClick = { dueTime = timeVal },
                                label = { Text(label) },
                                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Selection
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Low" to PriorityLow, "Medium" to PriorityMedium, "High" to PriorityHigh).forEach { (prio, color) ->
                        val isSelected = priority.equals(prio, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { priority = prio },
                            label = { Text(prio) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color.copy(alpha = 0.15f),
                                selectedLabelColor = color
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category.equals(cat.name, ignoreCase = true)
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat.colorHex))
                        } catch (_: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat.name },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Task Reminder",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Send alert when due time arrives",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it },
                            modifier = Modifier.testTag("task_reminder_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldAccent,
                                checkedTrackColor = EmeraldAccent.copy(alpha = 0.4f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("task_cancel_button")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                            } else {
                                val cleanTime = if (dueDate == null || dueTime.isBlank() || dueTime == "none") null else dueTime.trim()
                                onSave(
                                    initialTask?.id ?: 0L,
                                    title,
                                    description,
                                    dueDate,
                                    cleanTime,
                                    priority,
                                    category,
                                    hasReminder
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("task_save_button")
                    ) {
                        Text(if (initialTask == null) "Create Task" else "Save Changes")
                    }
                }
            }
        }
    }
}
