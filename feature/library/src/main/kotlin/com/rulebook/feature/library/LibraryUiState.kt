package com.rulebook.feature.library

import com.rulebook.core.model.Game

/**
 * UI state for the Library screen.
 *
 * Tracks the list of saved games and loading/refresh states.
 * When the games list is empty and not loading, the empty state is displayed.
 *
 * @param games The list of saved games.
 * @param isLoading Whether initial data loading is in progress.
 * @param isRefreshing Whether a pull-to-refresh is in progress.
 * @param error Optional error message if loading failed.
 */
data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /**
     * Whether the empty state should be displayed.
     * True when there are no games and not currently loading.
     */
    val isEmpty: Boolean get() = games.isEmpty() && !isLoading
}
