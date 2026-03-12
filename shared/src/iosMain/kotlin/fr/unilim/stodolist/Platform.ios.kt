package fr.unilim.stodolist

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of getPlatform().
 * Returns platform information including iOS device and version details.
 */
actual fun getPlatform(): Platform {
    return Platform("${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}")
}

/**
 * iOS implementation of currentTimeMillis().
 * Returns the current time in milliseconds since Unix epoch.
 */
actual fun currentTimeMillis(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}
