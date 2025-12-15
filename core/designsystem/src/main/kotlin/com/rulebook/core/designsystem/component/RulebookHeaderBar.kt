package com.rulebook.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * RulebookHeaderBar - Brutalist-styled header bar for screen titles.
 *
 * A header bar component that follows the Rulebook brutalist design system,
 * featuring bold typography and a thick bottom border.
 *
 * @param title The title text to display in brutalist style (24sp Black).
 * @param modifier Modifier to be applied to the header bar.
 * @param onBackClick Optional callback for back navigation. When provided, displays a back arrow.
 * @param actions Optional composable content for action buttons on the trailing side.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulebookHeaderBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = RulebookTheme.typography.brutalistTitle
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.statusBars
            )
        )
        // Bottom border - 3dp thick black line (brutalist aesthetic)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RulebookTheme.spacing.borderWidth)
                .background(Color.Black)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Title Only - Light")
@Composable
private fun RulebookHeaderBarTitleOnlyLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookHeaderBar(title = "Library")
    }
}

@Preview(showBackground = true, name = "Title Only - Dark")
@Composable
private fun RulebookHeaderBarTitleOnlyDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookHeaderBar(title = "Library")
    }
}

@Preview(showBackground = true, name = "With Back Button - Light")
@Composable
private fun RulebookHeaderBarWithBackLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookHeaderBar(
            title = "Game Rules",
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "With Back Button - Dark")
@Composable
private fun RulebookHeaderBarWithBackDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookHeaderBar(
            title = "Game Rules",
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "With Actions - Light")
@Composable
private fun RulebookHeaderBarWithActionsLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookHeaderBar(
            title = "Game Rules",
            onBackClick = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "With Actions - Dark")
@Composable
private fun RulebookHeaderBarWithActionsDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookHeaderBar(
            title = "Game Rules",
            onBackClick = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
        )
    }
}
