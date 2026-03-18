package com.rulebook.feature.purchase.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import com.rulebook.core.designsystem.theme.RulebookTheme

private const val DotAnimationDurationMs = 400
private const val DotStaggerMs = 150

/**
 * AnimatedDots - Three staggered animated dots used as a loading indicator.
 *
 * Used in place of button text (e.g., "Restore Purchases") while a restore
 * operation is in progress. Each dot fades in and out with a staggered delay
 * to create a pulse effect.
 *
 * @param modifier Modifier applied to the Row container.
 */
@Composable
fun AnimatedDots(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "animatedDots")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = DotAnimationDurationMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1Alpha"
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = DotAnimationDurationMs, delayMillis = DotStaggerMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2Alpha"
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = DotAnimationDurationMs, delayMillis = DotStaggerMs * 2),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3Alpha"
    )

    Row(modifier = modifier) {
        Text(
            text = ".",
            style = RulebookTheme.typography.brutalistButtonText,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(dot1Alpha)
        )
        Text(
            text = ".",
            style = RulebookTheme.typography.brutalistButtonText,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(dot2Alpha)
        )
        Text(
            text = ".",
            style = RulebookTheme.typography.brutalistButtonText,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alpha(dot3Alpha)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Animated Dots")
@Composable
private fun AnimatedDotsPreview() {
    RulebookTheme(darkTheme = false) {
        AnimatedDots()
    }
}
