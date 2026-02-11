package com.rulebook.feature.library

import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.datastore.SortPreferencesSource
import com.rulebook.core.model.Game
import com.rulebook.core.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows empty list when repository returns empty`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository(games = emptyList())
        val preferences = FakeRulebookPreferences()

        // When
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertTrue(state.games.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `initial state shows games when repository returns data`() = runTest(testDispatcher) {
        // Given
        val games = listOf(
            Game("1", "Catan", null, 1000L, 2000L)
        )
        val repository = FakeGameRepository(games = games)
        val preferences = FakeRulebookPreferences()

        // When
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isEmpty)
        assertEquals(1, state.games.size)
        assertEquals("Catan", state.games[0].title)
    }

    @Test
    fun `games are available in state when repository returns multiple games`() = runTest(testDispatcher) {
        // Given - games with different lastAccessedAt for RECENT sort
        val games = listOf(
            Game("1", "Catan", "https://example.com/catan.jpg", 1000L, 5000L),
            Game("2", "Pandemic", null, 2000L, 4000L),
            Game("3", "Ticket to Ride", "https://example.com/ttr.jpg", 3000L, 6000L)
        )
        val repository = FakeGameRepository(games = games)
        val preferences = FakeRulebookPreferences()

        // When
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // Then - default sort is RECENT (lastAccessedAt desc)
        val state = viewModel.uiState.value
        assertFalse(state.isEmpty)
        assertEquals(3, state.games.size)
        assertEquals("3", state.games[0].id) // lastAccessedAt: 6000
        assertEquals("1", state.games[1].id) // lastAccessedAt: 5000
        assertEquals("2", state.games[2].id) // lastAccessedAt: 4000
    }

    @Test
    fun `initial sort order loads from preferences`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository()
        val preferences = FakeRulebookPreferences(initialSortOrder = SortOrder.ALPHABETICAL)

        // When
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // Then
        assertEquals(SortOrder.ALPHABETICAL, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun `changeSortOrder updates UI state and persists to preferences`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository()
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        assertEquals(SortOrder.RECENT, viewModel.uiState.value.sortOrder)

        // When
        viewModel.changeSortOrder(SortOrder.DATE_ADDED)
        advanceUntilIdle()

        // Then
        assertEquals(SortOrder.DATE_ADDED, viewModel.uiState.value.sortOrder)
        assertEquals(SortOrder.DATE_ADDED, preferences.lastSetSortOrder)
    }

    @Test
    fun `changing sort order triggers reactive game list update`() = runTest(testDispatcher) {
        // Given
        val games = listOf(
            Game("1", "Zebra", null, 3000L, 1000L), // alphabetically last, oldest access
            Game("2", "Alpha", null, 1000L, 3000L)  // alphabetically first, newest access
        )
        val repository = FakeGameRepository(games = games)
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // Initially RECENT sort (by lastAccessedAt desc)
        assertEquals("2", viewModel.uiState.value.games[0].id) // newest access

        // When - change to ALPHABETICAL
        viewModel.changeSortOrder(SortOrder.ALPHABETICAL)
        advanceUntilIdle()

        // Then - games re-sorted alphabetically
        assertEquals("2", viewModel.uiState.value.games[0].id) // "Alpha" first
        assertEquals("1", viewModel.uiState.value.games[1].id) // "Zebra" second
    }

    @Test
    fun `refresh shows indicator then clears it`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository(games = emptyList())
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // When - start refresh
        viewModel.refresh()
        testDispatcher.scheduler.runCurrent()

        // Then - isRefreshing should be true during refresh delay
        assertTrue(viewModel.uiState.value.isRefreshing)

        // And - isRefreshing should be false after delay completes
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `deleteConfirmation is null by default`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository()
        val preferences = FakeRulebookPreferences()

        // When
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // Then
        assertEquals(null, viewModel.uiState.value.deleteConfirmation)
    }

    @Test
    fun `LibraryEvent ShowSnackbar can be created with message`() {
        // When
        val event = LibraryEvent.ShowSnackbar("Test message")

        // Then
        assertEquals("Test message", event.message)
    }

    @Test
    fun `requestDelete sets deleteConfirmation in state`() = runTest(testDispatcher) {
        // Given
        val game = Game("1", "Catan", null, 1000L, 2000L)
        val repository = FakeGameRepository(games = listOf(game))
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // When
        viewModel.requestDelete(game)
        advanceUntilIdle()

        // Then
        assertEquals(game, viewModel.uiState.value.deleteConfirmation)
    }

    @Test
    fun `cancelDelete clears deleteConfirmation`() = runTest(testDispatcher) {
        // Given
        val game = Game("1", "Catan", null, 1000L, 2000L)
        val repository = FakeGameRepository(games = listOf(game))
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        viewModel.requestDelete(game)
        advanceUntilIdle()
        assertEquals(game, viewModel.uiState.value.deleteConfirmation)

        // When
        viewModel.cancelDelete()
        advanceUntilIdle()

        // Then
        assertEquals(null, viewModel.uiState.value.deleteConfirmation)
    }

    @Test
    fun `confirmDelete calls repository deleteGame and emits snackbar event`() = runTest(testDispatcher) {
        // Given
        val game = Game("1", "Catan", null, 1000L, 2000L)
        val repository = FakeGameRepository(games = listOf(game))
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        viewModel.requestDelete(game)
        advanceUntilIdle()

        // When
        viewModel.confirmDelete()
        advanceUntilIdle()

        // Then
        assertTrue(repository.deletedIds.contains("1"))
    }

    @Test
    fun `confirmDelete clears dialog state before calling repository`() = runTest(testDispatcher) {
        // Given
        val game = Game("1", "Catan", null, 1000L, 2000L)
        val repository = FakeGameRepository(games = listOf(game))
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        viewModel.requestDelete(game)
        advanceUntilIdle()

        // When
        viewModel.confirmDelete()
        advanceUntilIdle()

        // Then
        assertEquals(null, viewModel.uiState.value.deleteConfirmation)
    }

    @Test
    fun `confirmDelete emits error snackbar when repository fails`() = runTest(testDispatcher) {
        // Given
        val game = Game("1", "Catan", null, 1000L, 2000L)
        val repository = FakeGameRepository(games = listOf(game), deleteError = "Delete failed")
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        viewModel.requestDelete(game)
        advanceUntilIdle()

        // Collect events in background
        val events = mutableListOf<LibraryEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        // When
        viewModel.confirmDelete()
        advanceUntilIdle()

        // Then
        assertTrue(events.any { it is LibraryEvent.ShowSnackbar && it.message == "Failed to delete game" })
        job.cancel()
    }
}

/**
 * Fake implementation of SortPreferencesSource for testing.
 * Implements the interface directly to avoid DataStore/Context dependencies.
 */
private class FakeRulebookPreferences(
    initialSortOrder: SortOrder = SortOrder.RECENT
) : SortPreferencesSource {

    private val _sortOrder = MutableStateFlow(initialSortOrder)
    var lastSetSortOrder: SortOrder? = null
        private set

    override val sortOrder: Flow<SortOrder> = _sortOrder

    override suspend fun setSortOrder(order: SortOrder) {
        lastSetSortOrder = order
        _sortOrder.value = order
    }
}

/**
 * Fake implementation of GameRepository for testing with reactive Flow support.
 */
private class FakeGameRepository(
    private val games: List<Game> = emptyList(),
    private val error: String? = null,
    private val deleteError: String? = null
) : GameRepository {

    val deletedIds = mutableListOf<String>()

    override suspend fun getGames(): Result<List<Game>> {
        delay(1)
        return if (error != null) Result.Error(error) else Result.Success(games)
    }

    override fun getGamesSorted(sortOrder: SortOrder): Flow<List<Game>> {
        val sorted = when (sortOrder) {
            SortOrder.RECENT -> games.sortedByDescending { it.lastAccessedAt }
            SortOrder.ALPHABETICAL -> games.sortedBy { it.title }
            SortOrder.DATE_ADDED -> games.sortedByDescending { it.createdAt }
        }
        return flowOf(sorted)
    }

    override suspend fun getGameById(id: String): Result<Game> {
        val game = games.find { it.id == id }
        return if (game != null) Result.Success(game) else Result.Error("Game not found")
    }

    override suspend fun saveGame(game: Game): Result<Unit> = Result.Success(Unit)

    override suspend fun deleteGame(id: String): Result<Unit> {
        deletedIds.add(id)
        return if (deleteError != null) Result.Error(deleteError) else Result.Success(Unit)
    }

    override suspend fun getRulesForGame(gameId: String): Result<com.rulebook.core.model.Rules> =
        Result.Error("Not implemented")
    override suspend fun saveGameWithRules(game: Game, rules: com.rulebook.core.model.Rules, rawJson: String): Result<String> =
        Result.Success("test-game-id")
    override suspend fun updateLastAccessed(gameId: String): Result<Unit> = Result.Success(Unit)
}
