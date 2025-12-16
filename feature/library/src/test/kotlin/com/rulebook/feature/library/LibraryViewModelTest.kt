package com.rulebook.feature.library

import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.model.Game
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    fun `initial state shows empty list when repository returns empty`() = runTest {
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
    fun `initial state shows games when repository returns data`() = runTest {
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
    fun `refresh updates isRefreshing state`() = runTest {
        // Given
        val repository = FakeGameRepository(games = emptyList())
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // When
        viewModel.refresh()

        // Then - isRefreshing should be true during refresh
        assertTrue(viewModel.uiState.value.isRefreshing)

        // And - isRefreshing should be false after refresh completes
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `error state is captured when repository fails`() = runTest {
        // Given
        val repository = FakeGameRepository(error = "Network error")

        // When
        val viewModel = LibraryViewModel(repository)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Network error", state.error)
    }
}

/**
 * Fake implementation of GameRepository for testing.
 */
private class FakeGameRepository(
    private val games: List<Game> = emptyList(),
    private val error: String? = null
) : GameRepository {

    override suspend fun getGames(): Result<List<Game>> {
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
}
