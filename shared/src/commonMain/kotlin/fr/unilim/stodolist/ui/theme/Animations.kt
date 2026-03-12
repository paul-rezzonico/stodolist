package fr.unilim.stodolist.ui.theme

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.TransformOrigin

// =============================================================================
// Animation Duration Constants
// =============================================================================

/**
 * Standard animation duration values for consistent timing throughout the app.
 * Use these values to ensure animations feel cohesive and purposeful.
 */
object AnimationDuration {
    /** Instant - no animation (0ms) */
    const val instant = 0

    /** Fast animations - quick micro-interactions (150ms) */
    const val fast = 150

    /** Medium animations - standard transitions (300ms) */
    const val medium = 300

    /** Slow animations - emphasis transitions (500ms) */
    const val slow = 500

    /** Extra slow animations - dramatic effects (700ms) */
    const val extraSlow = 700
}

// =============================================================================
// Transition Specs - Basic Building Blocks
// =============================================================================

/**
 * Standard fade in transition with medium duration.
 * Use for smooth element appearance.
 */
fun defaultFadeIn(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = AnimationDuration.medium,
        easing = FastOutSlowInEasing
    )
)

/**
 * Standard fade out transition with medium duration.
 * Use for smooth element disappearance.
 */
fun defaultFadeOut(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = AnimationDuration.medium,
        easing = FastOutSlowInEasing
    )
)

/**
 * Fast fade in transition.
 * Use for quick micro-interactions.
 */
fun fastFadeIn(): EnterTransition = fadeIn(
    animationSpec = tween(
        durationMillis = AnimationDuration.fast,
        easing = FastOutSlowInEasing
    )
)

/**
 * Fast fade out transition.
 * Use for quick micro-interactions.
 */
fun fastFadeOut(): ExitTransition = fadeOut(
    animationSpec = tween(
        durationMillis = AnimationDuration.fast,
        easing = FastOutSlowInEasing
    )
)

/**
 * Slide in from right with spring physics.
 * Use for forward navigation transitions.
 */
fun springSlideInFromRight(): EnterTransition = slideInHorizontally(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    initialOffsetX = { fullWidth -> fullWidth }
)

/**
 * Slide out to left with spring physics.
 * Use for forward navigation transitions.
 */
fun springSlideOutToLeft(): ExitTransition = slideOutHorizontally(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    targetOffsetX = { fullWidth -> -fullWidth }
)

/**
 * Slide in from left with spring physics.
 * Use for back navigation transitions.
 */
fun springSlideInFromLeft(): EnterTransition = slideInHorizontally(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    initialOffsetX = { fullWidth -> -fullWidth }
)

/**
 * Slide out to right with spring physics.
 * Use for back navigation transitions.
 */
fun springSlideOutToRight(): ExitTransition = slideOutHorizontally(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    targetOffsetX = { fullWidth -> fullWidth }
)

/**
 * Scale in transition for dialogs and popups.
 * Scales from center with a subtle overshoot.
 */
fun dialogScaleIn(): EnterTransition = scaleIn(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    ),
    initialScale = 0.8f,
    transformOrigin = TransformOrigin.Center
)

/**
 * Scale out transition for dialogs and popups.
 * Scales to center with smooth deceleration.
 */
fun dialogScaleOut(): ExitTransition = scaleOut(
    animationSpec = tween(
        durationMillis = AnimationDuration.fast,
        easing = FastOutSlowInEasing
    ),
    targetScale = 0.8f,
    transformOrigin = TransformOrigin.Center
)

// =============================================================================
// Screen Transition Presets
// =============================================================================

/**
 * Screen enter transition - slide in from right with fade.
 * Use when navigating forward to a new screen.
 */
val screenEnterTransition: EnterTransition
    get() = slideInHorizontally(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        ),
        initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        )
    )

/**
 * Screen exit transition - slide out to left with fade.
 * Use when navigating forward (current screen exits).
 */
val screenExitTransition: ExitTransition
    get() = slideOutHorizontally(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        ),
        targetOffsetX = { fullWidth -> -fullWidth / 3 }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        )
    )

/**
 * Screen pop enter transition - slide in from left with fade.
 * Use when navigating back (previous screen enters).
 */
val screenPopEnterTransition: EnterTransition
    get() = slideInHorizontally(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        ),
        initialOffsetX = { fullWidth -> -fullWidth / 3 }
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        )
    )

/**
 * Screen pop exit transition - slide out to right with fade.
 * Use when navigating back (current screen exits).
 */
val screenPopExitTransition: ExitTransition
    get() = slideOutHorizontally(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        ),
        targetOffsetX = { fullWidth -> fullWidth }
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDuration.medium,
            easing = FastOutSlowInEasing
        )
    )

// =============================================================================
// Component Animations
// =============================================================================

/**
 * Creates a pulse animation state for attention-grabbing effects.
 * Returns an animated scale value that pulses between 1.0 and the specified scale.
 *
 * @param minScale The minimum scale value (default 0.97f for subtle effect)
 * @param maxScale The maximum scale value (default 1.03f for subtle effect)
 * @param durationMillis The duration of one pulse cycle
 * @return Animated float state representing the current scale
 */
@Composable
fun pulseAnimation(
    minScale: Float = 0.97f,
    maxScale: Float = 1.03f,
    durationMillis: Int = 1000
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    return infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
}

/**
 * Creates a shimmer animation state for loading effects.
 * Returns an animated float value that moves from 0 to 1 for shimmer offset.
 *
 * @param durationMillis The duration of one shimmer cycle
 * @return Animated float state representing the shimmer progress (0 to 1)
 */
@Composable
fun shimmerAnimation(
    durationMillis: Int = 1200
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmerTransition")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
}

/**
 * Creates a bounce animation state for playful success effects.
 * Returns an animated scale value that bounces for celebratory feedback.
 *
 * @param targetScale The peak scale of the bounce (default 1.2f)
 * @param durationMillis The duration of one bounce cycle
 * @return Animated float state representing the current scale
 */
@Composable
fun bounceAnimation(
    targetScale: Float = 1.2f,
    durationMillis: Int = 600
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "bounceTransition")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis / 2,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceScale"
    )
}

// =============================================================================
// Animation Spec Utilities
// =============================================================================

/**
 * Standard spring animation spec for general use.
 */
fun <T> standardSpring(): AnimationSpec<T> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Snappy spring animation spec for quick, responsive animations.
 */
fun <T> snappySpring(): AnimationSpec<T> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

/**
 * Gentle spring animation spec for smooth, relaxed animations.
 */
fun <T> gentleSpring(): AnimationSpec<T> = spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessLow
)
