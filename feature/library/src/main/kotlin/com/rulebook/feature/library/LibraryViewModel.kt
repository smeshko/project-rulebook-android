package com.rulebook.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.datastore.SortPreferencesSource
import com.rulebook.core.model.Game
import com.rulebook.core.model.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Library screen.
 *
 * Manages the list of saved games with reactive sorting capabilities.
 * Games automatically re-sort when the sort order changes, and the list
 * updates reactively when underlying data changes (inserts/updates/deletes).
 *
 * @param gameRepository Repository for accessing game data.
 * @param sortPreferencesSource Preferences source for reading/writing sort order.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val gameRepository: GameRepository,
    private val sortPreferencesSource: SortPreferencesSource
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _events = Channel<LibraryEvent>()
    val events: Flow<LibraryEvent> = _events.receiveAsFlow()

    init {
        // Initialize sort order from preferences
        viewModelScope.launch {
            sortPreferencesSource.sortOrder.collect { order ->
                _sortOrder.value = order
            }
        }

        // Set up reactive game loading with flatMapLatest for sort order changes
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            _sortOrder
                .flatMapLatest { sortOrder ->
                    gameRepository.getGamesSorted(sortOrder)
                        .map { games -> games to sortOrder }
                        .catch { e ->
                            _uiState.update {
                                it.copy(
                                    error = e.message ?: "Failed to load games",
                                    isLoading = false
                                )
                            }
                        }
                }
                .collect { (games, sortOrder) ->
                    _uiState.update {
                        it.copy(
                            games = games,
                            sortOrder = sortOrder,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Changes the library sort order.
     * Updates DataStore and triggers reactive re-sort via flatMapLatest.
     */
    fun changeSortOrder(order: SortOrder) {
        viewModelScope.launch {
            sortPreferencesSource.setSortOrder(order)
            // _sortOrder updates via Flow collection in init block
        }
    }

    /**
     * Refreshes the games list.
     * Called when the user performs a pull-to-refresh gesture.
     * The reactive Flow automatically re-emits when underlying data changes.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            // Room Flows automatically re-emit on data changes.
            // Brief delay provides visual feedback for the refresh indicator.
            delay(REFRESH_INDICATOR_DELAY_MS)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Requests deletion of a game.
     * Sets the deleteConfirmation state to show the confirmation dialog.
     */
    fun requestDelete(game: Game) {
        _uiState.update { it.copy(deleteConfirmation = game) }
    }

    /**
     * Cancels the pending deletion.
     * Clears the deleteConfirmation state to hide the dialog.
     */
    fun cancelDelete() {
        _uiState.update { it.copy(deleteConfirmation = null) }
    }

    /**
     * Confirms and executes the pending deletion.
     * Deletes the game from the repository and emits a snackbar event.
     */
    fun confirmDelete() {
        viewModelScope.launch {
            val game = _uiState.value.deleteConfirmation ?: return@launch

            // Clear dialog immediately (optimistic UI)
            _uiState.update { it.copy(deleteConfirmation = null) }

            // Delete from repository
            when (gameRepository.deleteGame(game.id)) {
                is Result.Success -> {
                    _events.send(LibraryEvent.ShowSnackbar("Game deleted"))
                }
                is Result.Error -> {
                    _events.send(LibraryEvent.ShowSnackbar("Failed to delete game"))
                }
            }
        }
    }

    private companion object {
        const val REFRESH_INDICATOR_DELAY_MS = 300L
    }
}
