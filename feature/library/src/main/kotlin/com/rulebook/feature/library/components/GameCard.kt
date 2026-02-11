package com.rulebook.feature.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.BrutalistShadowOffset
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.core.model.Game

/**
 * GameCard - Displays a game in a brutalist card with thumbnail and title.
 *
 * Shows a game's thumbnail (or placeholder) and title in a 2-column grid layout.
 * Uses brutalist styling with 4dp shadow offset, 3dp border, and sharp corners.
 *
 * Layout:
 * - Min height: 200dp
 * - Cover area: AsyncImage filling top portion
 * - Title area: Game title in cardTitle style (17sp SemiBold), max 2 lines
 *
 * @param game The game to display
 * @param onClick Callback when the card is tapped
 * @param onLongClick Optional callback when the card is long-pressed
 * @param modifier Modifier to be applied to the card
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null
) {
    val cardModifier = modifier
        .heightIn(min = 200.dp)
        .brutalistShadow(offset = BrutalistShadowOffset)
        .brutalistBorder()
        .background(MaterialTheme.colorScheme.surface)
        .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple()
        )
        .padding(RulebookTheme.spacing.md)

    Box(modifier = cardModifier) {
        Column {
            // Cover area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        if (game.thumbnailUrl == null) {
                            RulebookTheme.colors.surfaceTertiary
                        } else {
                            RulebookTheme.colors.surfaceSecondary
                        }
                    )
            ) {
                if (game.thumbnailUrl != null) {
                    AsyncImage(
                        model = game.thumbnailUrl,
                        contentDescription = "${game.title} thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(com.rulebook.core.designsystem.R.drawable.ic_game_placeholder),
                        error = painterResource(com.rulebook.core.designsystem.R.drawable.ic_game_placeholder)
                    )
                } else {
                    // Show placeholder icon when no thumbnail URL
                    androidx.compose.foundation.Image(
                        painter = painterResource(com.rulebook.core.designsystem.R.drawable.ic_game_placeholder),
                        contentDescription = "${game.title} placeholder",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Title area
            Text(
                text = game.title,
                style = RulebookTheme.typography.cardTitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = RulebookTheme.spacing.sm)
            )
        }
    }
}
