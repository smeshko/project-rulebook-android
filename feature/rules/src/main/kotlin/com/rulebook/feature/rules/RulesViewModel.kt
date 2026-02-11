package com.rulebook.feature.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Rules screen.
 *
 * Manages the loading of game and rules data for display.
 * Loads both game metadata and rules content from the repository.
 *
 * @param gameId The ID of the game whose rules to display.
 * @param gameRepository Repository for accessing game and rules data.
 */
class RulesViewModel(
    private val gameId: String,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RulesUiState())
    val uiState: StateFlow<RulesUiState> = _uiState.asStateFlow()

    init {
        loadRules()
    }

    /**
     * Retries loading the rules after a failure.
     * Resets the error state and attempts to load again.
     */
    fun retry() {
        _uiState.update { it.copy(error = null, isLoading = true) }
        loadRules()
    }

    /**
     * Toggles the checked state of a setup item.
     * If the index is already checked, it will be unchecked, and vice versa.
     *
     * @param index The index of the setup item to toggle.
     */
    fun toggleSetupItem(index: Int) {
        _uiState.update { currentState ->
            val newCheckedItems = if (index in currentState.setupCheckedItems) {
                currentState.setupCheckedItems - index
            } else {
                currentState.setupCheckedItems + index
            }
            currentState.copy(setupCheckedItems = newCheckedItems)
        }
    }

    /**
     * Formats the current game and rules data for sharing.
     * Only produces share text when both game and rules are successfully loaded.
     *
     * @param onShareText Callback invoked with the formatted text when data is available.
     */
    fun shareRules(onShareText: (String) -> Unit) {
        val currentState = uiState.value
        if (currentState.isSuccess) {
            val game = currentState.game
            val rules = currentState.rules
            if (game != null && rules != null) {
                val formattedText = RulesShareFormatter.formatForSharing(game, rules)
                onShareText(formattedText)
            }
        }
    }

    private fun loadRules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load game data
            when (val gameResult = gameRepository.getGameById(gameId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(game = gameResult.data) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load game: ${gameResult.message}"
                        )
                    }
                    return@launch
                }
            }

            // Load rules data
            when (val rulesResult = gameRepository.getRulesForGame(gameId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            rules = rulesResult.data,
                            isLoading = false,
                            error = null
                        )
                    }

                    // Update last accessed timestamp in background (fire-and-forget)
                    viewModelScope.launch {
                        gameRepository.updateLastAccessed(gameId)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load rules: ${rulesResult.message}"
                        )
                    }
                }
            }
        }
    }
}
