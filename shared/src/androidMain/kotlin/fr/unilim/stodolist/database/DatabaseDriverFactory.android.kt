package fr.unilim.stodolist.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of DatabaseDriverFactory.
 * Uses AndroidSqliteDriver for SQLite database operations.
 *
 * @property context Android application context required for database creation
 */
actual class DatabaseDriverFactory(private val context: Context) {
    /**
     * Creates an Android-specific SqlDriver using AndroidSqliteDriver.
     * @return SqlDriver instance configured for Android
     */
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(StodolistDatabase.Schema, context, "stodolist.db")
    }
}
