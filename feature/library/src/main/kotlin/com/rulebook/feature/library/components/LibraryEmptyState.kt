package com.rulebook.feature.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Empty state component displayed when the user has no saved games.
 *
 * Shows an encouraging message with a game-related icon and a CTA button
 * to navigate to the camera for scanning the first game.
 *
 * @param onScanClick Callback invoked when the "Scan a Game" button is clicked.
 * @param modifier Modifier to be applied to the component.
 */
@Composable
fun LibraryEmptyState(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        RulebookCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RulebookTheme.spacing.md)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(RulebookTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SportsEsports,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.md))

                Text(
                    text = "No games yet",
                    style = RulebookTheme.typography.displayTitle2,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))

                Text(
                    text = "Scan your first game to get started",
                    style = RulebookTheme.typography.body,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.lg))

                RulebookButton(
                    text = "Scan a Game",
                    onClick = onScanClick,
                    variant = ButtonVariant.Primary
                )
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Empty State - Light")
@Composable
private fun LibraryEmptyStateLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryEmptyState(onScanClick = {})
    }
}

@Preview(showBackground = true, name = "Empty State - Dark")
@Composable
private fun LibraryEmptyStateDarkPreview() {
    RulebookTheme(darkTheme = true) {
        LibraryEmptyState(onScanClick = {})
    }
}
