package com.rulebook.feature.library

import com.rulebook.core.model.Game
import com.rulebook.core.model.SortOrder

/**
 * UI state for the Library screen.
 *
 * Tracks the list of saved games and loading/refresh states.
 * When the games list is empty and not loading, the empty state is displayed.
 *
 * @param games The list of saved games.
 * @param sortOrder The current sort order for the library.
 * @param isLoading Whether initial data loading is in progress.
 * @param isRefreshing Whether a pull-to-refresh is in progress.
 * @param error Optional error message if loading failed.
 * @param deleteConfirmation The game awaiting delete confirmation, or null if no dialog shown.
 * @param contextMenuGame The game whose context menu is shown, or null if no menu is displayed.
 */
data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val sortOrder: SortOrder = SortOrder.RECENT,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val deleteConfirmation: Game? = null,
    val contextMenuGame: Game? = null
) {
    /**
     * Whether the empty state should be displayed.
     * True when there are no games and no error occurred.
     * Note: Empty state is shown even during loading to provide visual feedback
     * rather than a blank screen. The PullToRefreshBox indicator handles refresh.
     */
    val isEmpty: Boolean get() = games.isEmpty() && error == null
}
