package com.rulebook.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.datastore.SortPreferencesSource
import com.rulebook.core.model.SortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
                    // When sort order changes, switch to the appropriate sorted Flow
                    gameRepository.getGamesSorted(sortOrder)
                }
                .combine(_sortOrder) { games, sortOrder ->
                    // Combine games with current sort order for UI state
                    games to sortOrder
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
            // Room Flows automatically re-emit on data changes
            // Just wait a moment for UI feedback, then clear refresh state
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
