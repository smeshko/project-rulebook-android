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
import androidx.compose.ui.res.pluralStringResource
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
 * CreditsDisplay - Shows the user's current credit balance.
 *
 * A compact, pill-shaped display designed for camera overlay usage.
 * Features semi-transparent background for readability over camera preview
 * and warning/error states for low/zero credit situations.
 *
 * @param creditCount The number of credits to display.
 * @param modifier Modifier to be applied to the component.
 */
@Composable
fun CreditsDisplay(
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
                    contentDescription = null,
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
