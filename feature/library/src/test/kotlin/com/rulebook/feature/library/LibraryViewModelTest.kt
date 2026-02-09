package com.rulebook.feature.library

import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

        // When
        val viewModel = LibraryViewModel(repository)
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
            Game(
                id = "1",
                title = "Catan",
                thumbnailUrl = null,
                createdAt = 1000L,
                lastAccessedAt = 2000L
            )
        )
        val repository = FakeGameRepository(games = games)

        // When
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isEmpty)
        assertEquals(1, state.games.size)
        assertEquals("Catan", state.games[0].title)
    }

    @Test
    fun `refresh updates isRefreshing state`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository(games = emptyList())
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // When - start refresh (this queues the coroutine)
        viewModel.refresh()
        // Advance scheduler to execute the first state update (isRefreshing = true)
        testDispatcher.scheduler.runCurrent()

        // Then - isRefreshing should be true during refresh
        assertTrue(viewModel.uiState.value.isRefreshing)

        // And - isRefreshing should be false after refresh completes
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `error state is captured when repository fails`() = runTest(testDispatcher) {
        // Given
        val repository = FakeGameRepository(error = "Network error")

        // When
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Network error", state.error)
    }

    @Test
    fun `isEmpty is false when error state is present`() = runTest(testDispatcher) {
        // Given - repository returns error with no games
        val repository = FakeGameRepository(error = "Network error")

        // When
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // Then - isEmpty should be false because error is present
        // (we want to show error UI, not empty state)
        val state = viewModel.uiState.value
        assertFalse(state.isEmpty)
        assertTrue(state.games.isEmpty())
        assertEquals("Network error", state.error)
    }

    @Test
    fun `refresh clears error state immediately`() = runTest(testDispatcher) {
        // Given - repository returns error
        val repository = FakeGameRepository(error = "Network error")
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // Verify initial error state
        assertEquals("Network error", viewModel.uiState.value.error)

        // When - start refresh (this queues the coroutine)
        viewModel.refresh()
        // Advance scheduler to execute the first state update
        testDispatcher.scheduler.runCurrent()

        // Then - error should be cleared immediately (before refresh completes)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertNull(viewModel.uiState.value.error)

        // After refresh completes, error returns (since repository still returns error)
        advanceUntilIdle()
        assertEquals("Network error", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}

/**
 * Fake implementation of GameRepository for testing.
 *
 * Uses delay(1) to force actual suspension, allowing tests to observe
 * intermediate states (like isRefreshing = true) before operations complete.
 */
private class FakeGameRepository(
    private val games: List<Game> = emptyList(),
    private val error: String? = null
) : GameRepository {

    override suspend fun getGames(): Result<List<Game>> {
        delay(1) // Force suspension to allow intermediate state observation
        return if (error != null) {
            Result.Error(error)
        } else {
            Result.Success(games)
        }
    }

    override suspend fun getGameById(id: String): Result<Game> {
        val game = games.find { it.id == id }
        return if (game != null) {
            Result.Success(game)
        } else {
            Result.Error("Game not found")
        }
    }

    override suspend fun saveGame(game: Game): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun deleteGame(id: String): Result<Unit> {
        return Result.Success(Unit)
    }

    override suspend fun getRulesForGame(gameId: String): Result<com.rulebook.core.model.Rules> {
        return Result.Error("Not implemented")
    }

    override suspend fun saveGameWithRules(
        game: Game,
        rules: com.rulebook.core.model.Rules,
        rawJson: String
    ): Result<String> {
        return Result.Success("test-game-id")
    }
}
