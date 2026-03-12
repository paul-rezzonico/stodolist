package fr.unilim.stodolist

import android.os.Build

/**
 * Android implementation of getPlatform().
 * Returns platform information including Android version details.
 */
actual fun getPlatform(): Platform {
    return Platform("Android ${Build.VERSION.SDK_INT}")
}

/**
 * Android implementation of currentTimeMillis().
 * Returns the current time in milliseconds since Unix epoch.
 */
actual fun currentTimeMillis(): Long {
    return System.currentTimeMillis()
}
