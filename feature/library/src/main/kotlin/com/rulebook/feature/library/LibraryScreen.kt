package com.rulebook.feature.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.library.components.LibraryEmptyState
import org.koin.androidx.compose.koinViewModel

/**
 * Library screen displaying the user's saved games.
 *
 * Shows an empty state when no games are saved, with a CTA to scan a game.
 * Supports pull-to-refresh for future content refresh functionality.
 *
 * @param onNavigateToCamera Callback invoked when the user wants to scan a game.
 * @param onNavigateToRules Callback invoked when a game is selected, with the game ID.
 * @param viewModel The ViewModel managing library state.
 * @param modifier Modifier to be applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToRules: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreenContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToRules = onNavigateToRules,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreenContent(
    uiState: LibraryUiState,
    onRefresh: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToRules: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        RulebookHeaderBar(title = "Library")

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isEmpty -> LibraryEmptyState(onScanClick = onNavigateToCamera)
                else -> LibraryContent(
                    games = uiState.games,
                    onGameClick = onNavigateToRules
                )
            }
        }
    }
}

/**
 * Content displaying the list of games when the library is not empty.
 * Currently a placeholder as game list UI will be implemented in Epic 7.
 */
@Composable
private fun LibraryContent(
    games: List<com.rulebook.core.model.Game>,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement game list grid in Epic 7
    // For now, this is a placeholder that won't be shown since we always return empty list
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Empty State - Light")
@Composable
private fun LibraryScreenEmptyLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(),
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Dark")
@Composable
private fun LibraryScreenEmptyDarkPreview() {
    RulebookTheme(darkTheme = true) {
        LibraryScreenContent(
            uiState = LibraryUiState(),
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State - Light")
@Composable
private fun LibraryScreenLoadingLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(isLoading = true),
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {}
        )
    }
}

@Preview(showBackground = true, name = "Refreshing State - Light")
@Composable
private fun LibraryScreenRefreshingLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(isRefreshing = true),
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {}
        )
    }
}
