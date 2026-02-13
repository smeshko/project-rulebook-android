package com.rulebook.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.CreditsDisplay
import com.rulebook.core.designsystem.component.CreditsDisplayVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.library.components.DeleteConfirmationDialog
import com.rulebook.feature.library.components.GameCard
import com.rulebook.feature.library.components.GameCardContextMenu
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
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LibraryEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    LibraryScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onNavigateToCamera = onNavigateToCamera,
        onNavigateToRules = onNavigateToRules,
        onRequestDelete = viewModel::requestDelete,
        onConfirmDelete = viewModel::confirmDelete,
        onCancelDelete = viewModel::cancelDelete,
        onShowContextMenu = viewModel::showContextMenu,
        onDismissContextMenu = viewModel::dismissContextMenu,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreenContent(
    uiState: LibraryUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToRules: (String) -> Unit,
    onRequestDelete: (com.rulebook.core.model.Game) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onShowContextMenu: (com.rulebook.core.model.Game) -> Unit,
    onDismissContextMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            RulebookHeaderBar(
                title = "Library",
                actions = {
                    CreditsDisplay(
                        creditCount = uiState.creditBalance,
                        variant = CreditsDisplayVariant.Header
                    )
                }
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = uiState.isRefreshing,
                        state = pullToRefreshState,
                        color = RulebookTheme.colors.pink,
                        containerColor = RulebookTheme.colors.surfacePrimary
                    )
                }
            ) {
                when {
                    uiState.error != null -> LibraryErrorState(
                        message = uiState.error,
                        onRetry = onRefresh
                    )
                    uiState.isEmpty -> LibraryEmptyState(onScanClick = onNavigateToCamera)
                    else -> LibraryContent(
                        games = uiState.games,
                        contextMenuGame = uiState.contextMenuGame,
                        onGameClick = onNavigateToRules,
                        onRequestDelete = onRequestDelete,
                        onShowContextMenu = onShowContextMenu,
                        onDismissContextMenu = onDismissContextMenu
                    )
                }
            }
        }

        // Snackbar host overlay
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Delete confirmation dialog
        uiState.deleteConfirmation?.let { game ->
            DeleteConfirmationDialog(
                gameName = game.title,
                onConfirm = onConfirmDelete,
                onDismiss = onCancelDelete
            )
        }
    }
}

/**
 * Content displaying the list of games in a 2-column grid.
 * Shows game cards with thumbnail and title, scrollable vertically.
 */
@Composable
private fun LibraryContent(
    games: List<com.rulebook.core.model.Game>,
    contextMenuGame: com.rulebook.core.model.Game?,
    onGameClick: (String) -> Unit,
    onRequestDelete: (com.rulebook.core.model.Game) -> Unit,
    onShowContextMenu: (com.rulebook.core.model.Game) -> Unit,
    onDismissContextMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(RulebookTheme.colors.surfaceSecondary),
        contentPadding = PaddingValues(RulebookTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(RulebookTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(RulebookTheme.spacing.md)
    ) {
        items(
            items = games,
            key = { game -> game.id }
        ) { game ->
            Box {
                GameCard(
                    game = game,
                    onClick = { onGameClick(game.id) },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShowContextMenu(game)
                    }
                )

                // Show context menu for this specific game
                GameCardContextMenu(
                    expanded = contextMenuGame?.id == game.id,
                    onDismiss = onDismissContextMenu,
                    onViewRules = { onGameClick(game.id) },
                    onDelete = { onRequestDelete(game) }
                )
            }
        }
    }
}

/**
 * Error state displayed when loading games fails.
 * Shows an error message with a retry button.
 */
@Composable
private fun LibraryErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        RulebookCard(
            modifier = Modifier.padding(RulebookTheme.spacing.md)
        ) {
            Column(
                modifier = Modifier.padding(RulebookTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Something went wrong",
                    style = RulebookTheme.typography.displayTitle2,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))

                Text(
                    text = message,
                    style = RulebookTheme.typography.body,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.lg))

                RulebookButton(
                    text = "Try Again",
                    onClick = onRetry,
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
private fun LibraryScreenEmptyLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Dark")
@Composable
private fun LibraryScreenEmptyDarkPreview() {
    RulebookTheme(darkTheme = true) {
        LibraryScreenContent(
            uiState = LibraryUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State - Light")
@Composable
private fun LibraryScreenLoadingLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Refreshing State - Light")
@Composable
private fun LibraryScreenRefreshingLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(isRefreshing = true),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Light")
@Composable
private fun LibraryScreenErrorLightPreview() {
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(error = "Failed to load games. Please check your connection."),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Dark")
@Composable
private fun LibraryScreenErrorDarkPreview() {
    RulebookTheme(darkTheme = true) {
        LibraryScreenContent(
            uiState = LibraryUiState(error = "Failed to load games. Please check your connection."),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Games Grid - Light")
@Composable
private fun LibraryScreenGamesLightPreview() {
    val sampleGames = listOf(
        com.rulebook.core.model.Game(
            id = "1",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "2",
            title = "Ticket to Ride",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "3",
            title = "Pandemic",
            thumbnailUrl = "https://example.com/pandemic.jpg",
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "4",
            title = "Azul",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "5",
            title = "7 Wonders",
            thumbnailUrl = "https://example.com/7wonders.jpg",
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "6",
            title = "Dominion",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
    )
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(games = sampleGames, creditBalance = 5),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Games Grid - Dark")
@Composable
private fun LibraryScreenGamesDarkPreview() {
    val sampleGames = listOf(
        com.rulebook.core.model.Game(
            id = "1",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "2",
            title = "Ticket to Ride",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "3",
            title = "Pandemic",
            thumbnailUrl = "https://example.com/pandemic.jpg",
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "4",
            title = "Azul",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "5",
            title = "7 Wonders",
            thumbnailUrl = "https://example.com/7wonders.jpg",
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        ),
        com.rulebook.core.model.Game(
            id = "6",
            title = "Dominion",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
    )
    RulebookTheme(darkTheme = true) {
        LibraryScreenContent(
            uiState = LibraryUiState(games = sampleGames),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}

@Preview(showBackground = true, name = "Delete Confirmation Dialog - Light")
@Composable
private fun LibraryScreenDeleteConfirmationPreview() {
    val sampleGame = com.rulebook.core.model.Game(
        id = "1",
        title = "Catan",
        thumbnailUrl = "https://example.com/catan.jpg",
        createdAt = System.currentTimeMillis(),
        lastAccessedAt = System.currentTimeMillis()
    )
    val sampleGames = listOf(
        sampleGame,
        com.rulebook.core.model.Game(
            id = "2",
            title = "Pandemic",
            thumbnailUrl = null,
            createdAt = System.currentTimeMillis(),
            lastAccessedAt = System.currentTimeMillis()
        )
    )
    RulebookTheme(darkTheme = false) {
        LibraryScreenContent(
            uiState = LibraryUiState(
                games = sampleGames,
                deleteConfirmation = sampleGame
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onNavigateToCamera = {},
            onNavigateToRules = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onCancelDelete = {},
            onShowContextMenu = {},
            onDismissContextMenu = {}
        )
    }
}
