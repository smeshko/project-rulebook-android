package com.rulebook.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Button variant enum defining the visual styles for RulebookButton.
 *
 * | Type        | Usage                 | Style                        |
 * |-------------|-----------------------|------------------------------|
 * | Primary     | Main CTA              | Filled pink, border, shadow  |
 * | Secondary   | Cancel, alternatives  | Outlined, no fill            |
 * | Destructive | Delete, clear         | Filled red, border, shadow   |
 */
enum class ButtonVariant {
    /** Main call-to-action button. Filled pink with border and shadow. */
    Primary,

    /** Secondary action button. Outlined with no fill or shadow. */
    Secondary,

    /** Destructive action button. Filled red with border and shadow. */
    Destructive
}

/**
 * RulebookButton - Brutalist-styled button component.
 *
 * A button component that follows the Rulebook brutalist design system,
 * featuring bold typography (Black weight), sharp corners, and offset shadows.
 *
 * @param text The button label text.
 * @param onClick Callback invoked when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param variant The visual style variant. Defaults to [ButtonVariant.Primary].
 * @param enabled Whether the button is enabled. Disabled state shows reduced opacity.
 */
@Composable
fun RulebookButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true
) {
    val colors = RulebookTheme.colors
    val spacing = RulebookTheme.spacing

    // Determine colors based on variant
    val backgroundColor = when (variant) {
        ButtonVariant.Primary -> colors.pink
        ButtonVariant.Secondary -> Color.Transparent
        ButtonVariant.Destructive -> colors.red
    }

    val contentColor = when (variant) {
        ButtonVariant.Primary -> Color.Black
        ButtonVariant.Secondary -> colors.contentPrimary
        ButtonVariant.Destructive -> Color.Black
    }

    // Shadow only for Primary and Destructive
    val hasShadow = variant != ButtonVariant.Secondary

    // Apply brutalist modifiers
    val buttonModifier = modifier
        .heightIn(min = 48.dp) // Min touch target
        .then(if (hasShadow) Modifier.brutalistShadow() else Modifier)
        .brutalistBorder()

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier,
        shape = RectangleShape,
        contentPadding = PaddingValues(
            horizontal = spacing.buttonPaddingHorizontal,
            vertical = 12.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            style = RulebookTheme.typography.brutalistButtonText
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Primary - Light")
@Composable
private fun RulebookButtonPrimaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookButton(
            text = "Primary Button",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Primary - Dark")
@Composable
private fun RulebookButtonPrimaryDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookButton(
            text = "Primary Button",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Secondary - Light")
@Composable
private fun RulebookButtonSecondaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookButton(
            text = "Secondary Button",
            onClick = {},
            variant = ButtonVariant.Secondary
        )
    }
}

@Preview(showBackground = true, name = "Secondary - Dark")
@Composable
private fun RulebookButtonSecondaryDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookButton(
            text = "Secondary Button",
            onClick = {},
            variant = ButtonVariant.Secondary
        )
    }
}

@Preview(showBackground = true, name = "Destructive - Light")
@Composable
private fun RulebookButtonDestructiveLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookButton(
            text = "Delete",
            onClick = {},
            variant = ButtonVariant.Destructive
        )
    }
}

@Preview(showBackground = true, name = "Destructive - Dark")
@Composable
private fun RulebookButtonDestructiveDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookButton(
            text = "Delete",
            onClick = {},
            variant = ButtonVariant.Destructive
        )
    }
}

@Preview(showBackground = true, name = "Disabled Primary - Light")
@Composable
private fun RulebookButtonDisabledPrimaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookButton(
            text = "Disabled",
            onClick = {},
            enabled = false
        )
    }
}

@Preview(showBackground = true, name = "Disabled Primary - Dark")
@Composable
private fun RulebookButtonDisabledPrimaryDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookButton(
            text = "Disabled",
            onClick = {},
            enabled = false
        )
    }
}

@Preview(showBackground = true, name = "Disabled Secondary - Light")
@Composable
private fun RulebookButtonDisabledSecondaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookButton(
            text = "Disabled",
            onClick = {},
            variant = ButtonVariant.Secondary,
            enabled = false
        )
    }
}

@Preview(showBackground = true, name = "Disabled Destructive - Light")
@Composable
private fun RulebookButtonDisabledDestructiveLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookButton(
            text = "Disabled",
            onClick = {},
            variant = ButtonVariant.Destructive,
            enabled = false
        )
    }
}
