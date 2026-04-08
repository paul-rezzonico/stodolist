package fr.unilim.stodolist.models

import kotlinx.serialization.Serializable

/**
 * Data class representing a Category for organizing tasks.
 *
 * @property id Unique identifier for the category
 * @property name The display name of the category
 * @property colorHex The hex color code for the category (e.g., "#5C6BC0")
 * @property icon The emoji icon representing the category
 * @property isDefault Whether this is a predefined default category
 */
@Serializable
data class Category(
    val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String,
    val isDefault: Boolean = false
) {
    companion object {
        // Predefined default categories
        val WORK = Category(name = "Work", colorHex = "#5C6BC0", icon = "💼", isDefault = true)
        val PERSONAL = Category(name = "Personal", colorHex = "#26A69A", icon = "🏠", isDefault = true)
        val SHOPPING = Category(name = "Shopping", colorHex = "#EF5350", icon = "🛒", isDefault = true)
        val HEALTH = Category(name = "Health", colorHex = "#66BB6A", icon = "💪", isDefault = true)
        val FINANCE = Category(name = "Finance", colorHex = "#FFA726", icon = "💰", isDefault = true)
        val EDUCATION = Category(name = "Education", colorHex = "#42A5F5", icon = "📚", isDefault = true)
        val SOCIAL = Category(name = "Social", colorHex = "#EC407A", icon = "👥", isDefault = true)
        val OTHER = Category(name = "Other", colorHex = "#78909C", icon = "📌", isDefault = true)

        val defaultCategories = listOf(WORK, PERSONAL, SHOPPING, HEALTH, FINANCE, EDUCATION, SOCIAL, OTHER)
    }
}
