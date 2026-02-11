package com.rulebook.feature.library

import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.model.Game
import com.rulebook.core.model.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
        // Given
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

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isEmpty)
        assertEquals(3, state.games.size)
        assertEquals("1", state.games[0].id)
        assertEquals("Catan", state.games[0].title)
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
    fun `refresh updates isRefreshing state`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository(games = emptyList())
        val preferences = FakeRulebookPreferences()
        val viewModel = LibraryViewModel(repository, preferences)
        advanceUntilIdle()

        // When
        viewModel.refresh()
        testDispatcher.scheduler.runCurrent()

        // Then - isRefreshing should be true during refresh
        assertTrue(viewModel.uiState.value.isRefreshing)

        // And - isRefreshing should be false after refresh completes
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}

/**
 * Fake implementation of RulebookPreferences for testing.
 * Extends RulebookPreferences but overrides all DataStore-dependent properties
 * to avoid requiring Android Context in unit tests.
 */
@Suppress("UNUSED_PARAMETER", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
private class FakeRulebookPreferences(
    initialSortOrder: SortOrder = SortOrder.RECENT
) : com.rulebook.core.datastore.RulebookPreferences(
    context = null!! // Never accessed in tests since we override all DataStore properties
) {

    private val _sortOrder = MutableStateFlow(initialSortOrder)
    var lastSetSortOrder: SortOrder? = null
        private set

    override val sortOrder: Flow<SortOrder> = _sortOrder

    override suspend fun setSortOrder(order: SortOrder) {
        lastSetSortOrder = order
        _sortOrder.value = order
    }

    // Override other DataStore properties to avoid NullPointerException
    override val hasCompletedOnboarding: Flow<Boolean> = flowOf(false)
    override suspend fun setOnboardingCompleted(completed: Boolean) {}
    override suspend fun completeOnboardingWithCredits(creditAmount: Int) = false

    override val creditBalance: Flow<Int> = flowOf(0)
    override suspend fun awardInitialCreditsIfNeeded(amount: Int) = false
    override suspend fun deductCredit() = false

    override val themeMode: Flow<ThemeMode> = flowOf(ThemeMode.SYSTEM)
    override val hapticsEnabled: Flow<Boolean> = flowOf(true)
}

/**
 * Fake implementation of GameRepository for testing with reactive Flow support.
 */
private class FakeGameRepository(
    private val games: List<Game> = emptyList(),
    private val error: String? = null
) : GameRepository {

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
    override suspend fun deleteGame(id: String): Result<Unit> = Result.Success(Unit)
    override suspend fun getRulesForGame(gameId: String): Result<com.rulebook.core.model.Rules> =
        Result.Error("Not implemented")
    override suspend fun saveGameWithRules(game: Game, rules: com.rulebook.core.model.Rules, rawJson: String): Result<String> =
        Result.Success("test-game-id")
    override suspend fun updateLastAccessed(gameId: String): Result<Unit> = Result.Success(Unit)
}
