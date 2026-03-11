package fr.unilim.stodolist

import platform.UIKit.UIDevice

/**
 * iOS implementation of getPlatform().
 * Returns platform information including iOS device and version details.
 */
actual fun getPlatform(): Platform {
    return Platform("${UIDevice.currentDevice.systemName()} ${UIDevice.currentDevice.systemVersion}")
}
