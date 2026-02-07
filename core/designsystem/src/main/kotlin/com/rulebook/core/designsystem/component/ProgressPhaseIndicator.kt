package com.rulebook.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Status of a phase in the progress indicator.
 */
enum class PhaseStatus {
    /** Phase has not started yet. */
    PENDING,
    /** Phase is currently active/in-progress. */
    ACTIVE,
    /** Phase has been completed. */
    COMPLETED
}

/**
 * Data representing a single phase in the progress indicator.
 *
 * @param name The display name of the phase.
 * @param status The current status of this phase.
 * @param phaseNumber The 1-based index of this phase.
 */
data class PhaseItem(
    val name: String,
    val status: PhaseStatus,
    val phaseNumber: Int
)

/**
 * ProgressPhaseIndicator - Displays a vertical list of scan pipeline phases.
 *
 * Each phase shows its number, name, and current status (pending, active, completed).
 * The active phase has a pulsing animation to indicate ongoing work.
 * Completed phases show a checkmark with green styling.
 *
 * Uses the Rulebook brutalist design system tokens for consistent styling.
 *
 * @param phases The list of phases to display.
 * @param overallProgress The overall progress as a fraction (0.0-1.0).
 * @param currentMessage A descriptive message for the current action.
 * @param modifier Modifier to be applied to the component.
 */
@Composable
fun ProgressPhaseIndicator(
    phases: List<PhaseItem>,
    overallProgress: Float,
    currentMessage: String,
    modifier: Modifier = Modifier
) {
    val colors = RulebookTheme.colors
    val spacing = RulebookTheme.spacing

    // Animated progress for smooth transitions
    val animatedProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.xs)
    ) {
        // Phase list
        phases.forEach { phase ->
            PhaseRow(
                phase = phase,
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        // Current action message
        Text(
            text = currentMessage,
            style = RulebookTheme.typography.callout,
            color = colors.contentSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(spacing.sm))

        // Overall progress bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RectangleShape),
            color = colors.orange,
            trackColor = colors.surfaceTertiary,
        )

        Spacer(modifier = Modifier.height(spacing.xs))

        // Progress percentage
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            style = RulebookTheme.typography.brutalistButtonText,
            color = colors.contentPrimary,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A single row in the phase indicator showing phase number, name, and status.
 */
@Composable
private fun PhaseRow(
    phase: PhaseItem,
    colors: com.rulebook.core.designsystem.theme.RulebookExtendedColors
) {
    // Pulsing animation for active phase
    val pulseAlpha = if (phase.status == PhaseStatus.ACTIVE) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        alpha
    } else {
        1f
    }

    // Color transitions
    val circleColor by animateColorAsState(
        targetValue = when (phase.status) {
            PhaseStatus.COMPLETED -> colors.green
            PhaseStatus.ACTIVE -> colors.orange
            PhaseStatus.PENDING -> colors.contentTertiary
        },
        animationSpec = tween(300),
        label = "circleColor"
    )

    val textColor by animateColorAsState(
        targetValue = when (phase.status) {
            PhaseStatus.COMPLETED -> colors.contentSecondary
            PhaseStatus.ACTIVE -> colors.contentPrimary
            PhaseStatus.PENDING -> colors.contentTertiary
        },
        animationSpec = tween(300),
        label = "textColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .alpha(pulseAlpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Phase number circle or checkmark
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            if (phase.status == PhaseStatus.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = phase.phaseNumber.toString(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Phase name
        Text(
            text = phase.name,
            style = when (phase.status) {
                PhaseStatus.ACTIVE -> RulebookTheme.typography.brutalistSectionTitle
                else -> RulebookTheme.typography.callout
            },
            color = textColor
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Progress Phase Indicator - Phase 1")
@Composable
private fun ProgressPhaseIndicatorPhase1Preview() {
    RulebookTheme(darkTheme = false) {
        ProgressPhaseIndicator(
            phases = listOf(
                PhaseItem("Processing Image", PhaseStatus.ACTIVE, 1),
                PhaseItem("Analyzing Image", PhaseStatus.PENDING, 2),
                PhaseItem("Identifying Game", PhaseStatus.PENDING, 3),
                PhaseItem("Generating Rules", PhaseStatus.PENDING, 4),
                PhaseItem("Saving Rules", PhaseStatus.PENDING, 5),
            ),
            overallProgress = 0.08f,
            currentMessage = "Preparing your photo for analysis..."
        )
    }
}

@Preview(showBackground = true, name = "Progress Phase Indicator - Phase 3")
@Composable
private fun ProgressPhaseIndicatorPhase3Preview() {
    RulebookTheme(darkTheme = false) {
        ProgressPhaseIndicator(
            phases = listOf(
                PhaseItem("Processing Image", PhaseStatus.COMPLETED, 1),
                PhaseItem("Analyzing Image", PhaseStatus.COMPLETED, 2),
                PhaseItem("Identifying Game", PhaseStatus.ACTIVE, 3),
                PhaseItem("Generating Rules", PhaseStatus.PENDING, 4),
                PhaseItem("Saving Rules", PhaseStatus.PENDING, 5),
            ),
            overallProgress = 0.50f,
            currentMessage = "Finding your game in our database..."
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E, name = "Progress Phase Indicator - Dark")
@Composable
private fun ProgressPhaseIndicatorDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ProgressPhaseIndicator(
            phases = listOf(
                PhaseItem("Processing Image", PhaseStatus.COMPLETED, 1),
                PhaseItem("Analyzing Image", PhaseStatus.COMPLETED, 2),
                PhaseItem("Identifying Game", PhaseStatus.COMPLETED, 3),
                PhaseItem("Generating Rules", PhaseStatus.ACTIVE, 4),
                PhaseItem("Saving Rules", PhaseStatus.PENDING, 5),
            ),
            overallProgress = 0.75f,
            currentMessage = "Creating your personalized rulebook..."
        )
    }
}
