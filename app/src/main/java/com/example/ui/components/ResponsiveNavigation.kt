package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TaskEntity
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.PriorityHigh
import com.example.ui.viewmodel.DashboardMetrics
import com.example.ui.viewmodel.NavigationItem

fun getNavIcon(item: NavigationItem): ImageVector {
    return when (item) {
        NavigationItem.DASHBOARD -> Icons.Default.Dashboard
        NavigationItem.ALL_TASKS -> Icons.Default.CheckCircle
        NavigationItem.TODAY -> Icons.Default.Today
        NavigationItem.UPCOMING -> Icons.Default.CalendarMonth
        NavigationItem.COMPLETED -> Icons.Default.TaskAlt
        NavigationItem.OVERDUE -> Icons.Default.Warning
        NavigationItem.SETTINGS -> Icons.Default.Settings
    }
}

@Composable
fun DrawerContent(
    currentNav: NavigationItem,
    onNavSelected: (NavigationItem) -> Unit,
    categories: List<CategoryEntity>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onOpenCategoryManagement: () -> Unit,
    metrics: DashboardMetrics,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // App Branding Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "MakeItHappen",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Turn ideas into progress",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Main Navigation Items
            NavigationItem.entries.filter { it != NavigationItem.SETTINGS }.forEach { item ->
                val badgeCount = when (item) {
                    NavigationItem.DASHBOARD -> null
                    NavigationItem.ALL_TASKS -> metrics.activeTasks
                    NavigationItem.TODAY -> metrics.todayTasks
                    NavigationItem.UPCOMING -> null
                    NavigationItem.COMPLETED -> metrics.completedTasks
                    NavigationItem.OVERDUE -> if (metrics.overdueTasks > 0) metrics.overdueTasks else null
                    NavigationItem.SETTINGS -> null
                }

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = getNavIcon(item),
                            contentDescription = item.title,
                            tint = if (item == NavigationItem.OVERDUE && (badgeCount ?: 0) > 0) {
                                PriorityHigh
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontWeight = if (currentNav == item) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    badge = {
                        if (badgeCount != null && badgeCount > 0) {
                            Badge(
                                containerColor = if (item == NavigationItem.OVERDUE) PriorityHigh else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "$badgeCount",
                                    color = if (item == NavigationItem.OVERDUE) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    },
                    selected = currentNav == item && selectedCategory == null,
                    onClick = {
                        onCategorySelected(null)
                        onNavSelected(item)
                    },
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .testTag("nav_item_${item.name.lowercase()}"),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Categories Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORIES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onOpenCategoryManagement,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Manage Categories",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // All Categories button
            NavigationDrawerItem(
                icon = {
                    Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                label = { Text("All Categories") },
                selected = selectedCategory == null && currentNav == NavigationItem.ALL_TASKS,
                onClick = { onCategorySelected(null) },
                modifier = Modifier.padding(vertical = 1.dp)
            )

            // Category list
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat.name
                val catColor = try {
                    Color(android.graphics.Color.parseColor(cat.colorHex))
                } catch (_: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                NavigationDrawerItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                    },
                    label = { Text(cat.name) },
                    selected = isSelected,
                    onClick = {
                        onNavSelected(NavigationItem.ALL_TASKS)
                        onCategorySelected(cat.name)
                    },
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Settings Bottom Item
            NavigationDrawerItem(
                icon = {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                },
                label = { Text("Settings") },
                selected = currentNav == NavigationItem.SETTINGS,
                onClick = {
                    onCategorySelected(null)
                    onNavSelected(NavigationItem.SETTINGS)
                },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("nav_item_settings")
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentNav: NavigationItem,
    onNavSelected: (NavigationItem) -> Unit,
    metrics: DashboardMetrics,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        tonalElevation = 8.dp
    ) {
        val bottomNavItems = listOf(
            NavigationItem.DASHBOARD,
            NavigationItem.ALL_TASKS,
            NavigationItem.TODAY,
            NavigationItem.UPCOMING,
            NavigationItem.SETTINGS
        )

        bottomNavItems.forEach { item ->
            val isSelected = currentNav == item
            NavigationBarItem(
                icon = {
                    if (item == NavigationItem.TODAY && metrics.todayTasks > 0) {
                        BadgedBox(badge = {
                            Badge { Text("${metrics.todayTasks}") }
                        }) {
                            Icon(getNavIcon(item), contentDescription = item.title)
                        }
                    } else {
                        Icon(getNavIcon(item), contentDescription = item.title)
                    }
                },
                label = { Text(item.title) },
                selected = isSelected,
                onClick = { onNavSelected(item) },
                modifier = Modifier.testTag("bottom_nav_${item.name.lowercase()}")
            )
        }
    }
}
