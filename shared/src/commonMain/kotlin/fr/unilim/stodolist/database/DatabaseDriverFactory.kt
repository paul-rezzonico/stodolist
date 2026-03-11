package fr.unilim.stodolist.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory class for creating platform-specific SQLite drivers.
 * Each platform provides its own actual implementation.
 */
expect class DatabaseDriverFactory {
    /**
     * Creates a platform-specific SqlDriver for the StodolistDatabase.
     * @return SqlDriver instance configured for the current platform
     */
    fun createDriver(): SqlDriver
}
