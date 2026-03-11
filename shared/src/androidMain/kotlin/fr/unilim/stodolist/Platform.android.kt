package fr.unilim.stodolist

import android.os.Build

/**
 * Android implementation of getPlatform().
 * Returns platform information including Android version details.
 */
actual fun getPlatform(): Platform {
    return Platform("Android ${Build.VERSION.SDK_INT}")
}
