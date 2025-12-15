package com.rulebook.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.BrutalistShadowOffset
import com.rulebook.core.designsystem.theme.BrutalistShadowOffsetMedium
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * RulebookCard - Brutalist-styled card component.
 *
 * A card component that follows the Rulebook brutalist design system,
 * featuring a solid offset shadow, thick border, and sharp corners.
 *
 * Key styling characteristics:
 * - Surface background color from theme
 * - 3dp black border
 * - 4dp offset shadow (configurable)
 * - 0dp corner radius (sharp corners)
 * - 16dp internal padding
 *
 * @param modifier Modifier to be applied to the card.
 * @param onClick Optional callback for click handling. When provided, displays ripple effect.
 * @param shadowOffset The distance to offset the shadow. Defaults to 4dp.
 * @param content The content to display inside the card.
 */
@Composable
fun RulebookCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shadowOffset: Dp = BrutalistShadowOffset,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .brutalistShadow(offset = shadowOffset)
        .brutalistBorder()
        .background(MaterialTheme.colorScheme.surface)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    onClick = onClick,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                )
            } else {
                Modifier
            }
        )
        .padding(RulebookTheme.spacing.md)

    Box(modifier = cardModifier) {
        content()
    }
}

/**
 * ElevatedRulebookCard - Brutalist-styled card with larger shadow offset.
 *
 * A variant of RulebookCard with a more prominent shadow (8dp offset),
 * used for emphasized cards or expanded states.
 *
 * @param modifier Modifier to be applied to the card.
 * @param onClick Optional callback for click handling. When provided, displays ripple effect.
 * @param content The content to display inside the card.
 */
@Composable
fun ElevatedRulebookCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    RulebookCard(
        modifier = modifier,
        onClick = onClick,
        shadowOffset = BrutalistShadowOffsetMedium,
        content = content
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Non-Clickable Card - Light")
@Composable
private fun RulebookCardNonClickableLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookCard {
            Text("Card content")
        }
    }
}

@Preview(showBackground = true, name = "Non-Clickable Card - Dark")
@Composable
private fun RulebookCardNonClickableDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookCard {
            Text("Card content")
        }
    }
}

@Preview(showBackground = true, name = "Clickable Card - Light")
@Composable
private fun RulebookCardClickableLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookCard(onClick = {}) {
            Text("Tappable card")
        }
    }
}

@Preview(showBackground = true, name = "Clickable Card - Dark")
@Composable
private fun RulebookCardClickableDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookCard(onClick = {}) {
            Text("Tappable card")
        }
    }
}

@Preview(showBackground = true, name = "Elevated Card - Light")
@Composable
private fun ElevatedRulebookCardLightPreview() {
    RulebookTheme(darkTheme = false) {
        ElevatedRulebookCard {
            Text("Emphasized card")
        }
    }
}

@Preview(showBackground = true, name = "Elevated Card - Dark")
@Composable
private fun ElevatedRulebookCardDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ElevatedRulebookCard {
            Text("Emphasized card")
        }
    }
}
