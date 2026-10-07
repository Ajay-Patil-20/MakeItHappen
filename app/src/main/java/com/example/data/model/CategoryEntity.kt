package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val name: String,
    val colorHex: String,
    val iconName: String = "folder",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity("Personal", "#6366F1", "person", isDefault = true),
            CategoryEntity("Work", "#3B82F6", "work", isDefault = true),
            CategoryEntity("Study", "#8B5CF6", "school", isDefault = true),
            CategoryEntity("Shopping", "#EC4899", "shopping_cart", isDefault = true),
            CategoryEntity("Health", "#10B981", "favorite", isDefault = true),
            CategoryEntity("Other", "#64748B", "more_horiz", isDefault = true)
        )
    }
}
