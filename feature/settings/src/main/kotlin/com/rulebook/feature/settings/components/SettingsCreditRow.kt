package com.rulebook.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.R
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Settings row displaying the user's credit balance.
 *
 * Displays credit count with a coin icon, label, and chevron for navigation.
 * Uses brutalist styling with 3dp black border and surface background.
 * Tapping navigates to the Paywall/Purchase screen.
 *
 * @param creditCount The number of credits to display.
 * @param onClick Callback when the row is clicked (navigates to paywall).
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsCreditRow(
    creditCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coin icon in a small box
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_credit_coin),
                contentDescription = null,
                tint = RulebookTheme.colors.orange,
                modifier = Modifier.size(24.dp)
            )
        }

        // Credit count label
        Text(
            text = pluralStringResource(
                id = R.plurals.credits_remaining,
                count = creditCount,
                creditCount
            ),
            style = RulebookTheme.typography.body,
            modifier = Modifier.weight(1f)
        )

        // Chevron accessory
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = RulebookTheme.colors.contentSecondary
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Credit Row - Normal (5 credits)")
@Composable
private fun SettingsCreditRowNormalPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsCreditRow(
            creditCount = 5,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Credit Row - Low (1 credit)")
@Composable
private fun SettingsCreditRowLowPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsCreditRow(
            creditCount = 1,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Credit Row - Empty (0 credits)")
@Composable
private fun SettingsCreditRowEmptyPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsCreditRow(
            creditCount = 0,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Credit Row - Dark")
@Composable
private fun SettingsCreditRowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsCreditRow(
            creditCount = 5,
            onClick = {}
        )
    }
}
