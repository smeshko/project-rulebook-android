package com.rulebook.feature.rules

import com.rulebook.core.model.Game
import com.rulebook.core.model.Rules

/**
 * UI state for the Rules screen.
 *
 * Tracks the loaded game and rules data, along with loading and error states.
 *
 * @param game The game being displayed (null if not loaded yet).
 * @param rules The rules for the game (null if not loaded yet).
 * @param isLoading Whether data loading is in progress.
 * @param error Optional error message if loading failed.
 */
data class RulesUiState(
    val game: Game? = null,
    val rules: Rules? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /**
     * Whether the screen has successfully loaded all data.
     * True when both game and rules are available and no error occurred.
     */
    val isSuccess: Boolean get() = game != null && rules != null && !isLoading && error == null
}
