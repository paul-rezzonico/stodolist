package fr.unilim.stodolist.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of DatabaseDriverFactory.
 * Uses NativeSqliteDriver for SQLite database operations on iOS.
 */
actual class DatabaseDriverFactory {
    /**
     * Creates an iOS-specific SqlDriver using NativeSqliteDriver.
     * @return SqlDriver instance configured for iOS/Native
     */
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(StodolistDatabase.Schema, "stodolist.db")
    }
}
