package fr.unilim.stodolist

/**
 * Platform information class.
 * @property name The name of the platform (e.g., "Android", "iOS")
 */
class Platform(val name: String)

/**
 * Returns the current platform information.
 * This is an expect declaration that must be implemented for each target platform.
 */
expect fun getPlatform(): Platform
