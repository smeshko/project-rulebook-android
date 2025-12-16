package com.rulebook.feature.library

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
 * ViewModel for the Library screen.
 *
 * Manages the list of saved games and handles loading/refresh operations.
 * Currently loads an empty list as the repository is not yet connected to
 * a real data source (will be implemented in Epic 3).
 *
 * @param gameRepository Repository for accessing game data.
 */
class LibraryViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    /**
     * Refreshes the games list.
     * Called when the user performs a pull-to-refresh gesture.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            loadGamesInternal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            loadGamesInternal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadGamesInternal() {
        when (val result = gameRepository.getGames()) {
            is Result.Success -> _uiState.update {
                it.copy(games = result.data, error = null)
            }
            is Result.Error -> _uiState.update {
                it.copy(error = result.message)
            }
        }
    }
}
