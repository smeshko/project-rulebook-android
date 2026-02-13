package com.rulebook.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rulebook.core.designsystem.R
import com.rulebook.core.designsystem.theme.OrangeDark
import com.rulebook.core.designsystem.theme.RedDark
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * The state of the credit balance display.
 *
 * Used to determine the visual styling of the [CreditsDisplay] component.
 */
enum class CreditState {
    /** Normal state - user has 2+ credits. Uses default styling. */
    Normal,

    /** Low state - user has exactly 1 credit. Uses warning (orange) styling. */
    Low,

    /** Empty state - user has 0 credits. Uses error (red) styling. */
    Empty
}

/**
 * Display variant for the credit balance component.
 *
 * Determines the visual style and layout of the credit display.
 */
enum class CreditsDisplayVariant {
    /** Camera overlay variant - pill shape with semi-transparent background and white text. */
    Camera,

    /** Header variant - compact display for app bar trailing content with icon and caption text. */
    Header
}

/**
 * CreditsDisplay - Shows the user's current credit balance.
 *
 * Supports multiple display variants for different contexts:
 * - Camera: Pill-shaped overlay with semi-transparent background
 * - Header: Compact display for app bar with coin icon and caption text
 *
 * @param creditCount The number of credits to display.
 * @param variant The display variant to use (default: Camera for backward compatibility).
 * @param modifier Modifier to be applied to the component.
 */
@Composable
fun CreditsDisplay(
    creditCount: Int,
    variant: CreditsDisplayVariant = CreditsDisplayVariant.Camera,
    modifier: Modifier = Modifier
) {
    when (variant) {
        CreditsDisplayVariant.Camera -> CreditsCameraVariant(
            creditCount = creditCount,
            modifier = modifier
        )
        CreditsDisplayVariant.Header -> CreditsHeaderVariant(
            creditCount = creditCount,
            modifier = modifier
        )
    }
}

/**
 * Camera overlay variant - pill-shaped with semi-transparent background.
 */
@Composable
private fun CreditsCameraVariant(
    creditCount: Int,
    modifier: Modifier = Modifier
) {
    val creditState = when {
        creditCount == 0 -> CreditState.Empty
        creditCount == 1 -> CreditState.Low
        else -> CreditState.Normal
    }

    val backgroundColor = when (creditState) {
        CreditState.Empty -> RedDark.copy(alpha = 0.7f)
        CreditState.Low -> OrangeDark.copy(alpha = 0.7f)
        CreditState.Normal -> Color.Black.copy(alpha = 0.5f)
    }

    val textColor = Color.White

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (creditState == CreditState.Low || creditState == CreditState.Empty) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(
                        if (creditState == CreditState.Empty) {
                            R.string.credits_warning_empty
                        } else {
                            R.string.credits_warning_low
                        }
                    ),
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = pluralStringResource(
                    id = R.plurals.credits_count,
                    count = creditCount,
                    creditCount
                ),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Header variant - compact display with coin icon for app bar.
 */
@Composable
private fun CreditsHeaderVariant(
    creditCount: Int,
    modifier: Modifier = Modifier
) {
    val creditState = when {
        creditCount == 0 -> CreditState.Empty
        creditCount == 1 -> CreditState.Low
        else -> CreditState.Normal
    }

    val textColor = when (creditState) {
        CreditState.Empty -> RulebookTheme.colors.red
        CreditState.Low -> RulebookTheme.colors.orange
        CreditState.Normal -> RulebookTheme.colors.contentPrimary
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_credit_coin),
            contentDescription = null,
            tint = RulebookTheme.colors.orange,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = creditCount.toString(),
            style = RulebookTheme.typography.caption,
            color = textColor
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Normal - 3 credits")
@Composable
private fun CreditsDisplayNormalPreview() {
    RulebookTheme {
        CreditsDisplay(creditCount = 3)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Low - 1 credit")
@Composable
private fun CreditsDisplayLowPreview() {
    RulebookTheme {
        CreditsDisplay(creditCount = 1)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Empty - 0 credits")
@Composable
private fun CreditsDisplayEmptyPreview() {
    RulebookTheme {
        CreditsDisplay(creditCount = 0)
    }
}

@Preview(showBackground = true, name = "Header - Normal (3 credits)")
@Composable
private fun CreditsDisplayHeaderNormalPreview() {
    RulebookTheme {
        CreditsDisplay(creditCount = 3, variant = CreditsDisplayVariant.Header)
    }
}

@Preview(showBackground = true, name = "Header - Low (1 credit)")
@Composable
private fun CreditsDisplayHeaderLowPreview() {
    RulebookTheme {
        CreditsDisplay(creditCount = 1, variant = CreditsDisplayVariant.Header)
    }
}

@Preview(showBackground = true, name = "Header - Empty (0 credits)")
@Composable
private fun CreditsDisplayHeaderEmptyPreview() {
    RulebookTheme {
        CreditsDisplay(creditCount = 0, variant = CreditsDisplayVariant.Header)
    }
}
