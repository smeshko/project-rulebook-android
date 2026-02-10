package com.rulebook.feature.rules

import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.model.Game
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

/**
 * Unit tests for RulesViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RulesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testGame = Game(
        id = "game-1",
        title = "Test Game",
        thumbnailUrl = "http://example.com/test.jpg",
        createdAt = 1000L,
        lastAccessedAt = 2000L
    )

    private val testRules = Rules(
        gameId = "game-1",
        overview = RuleSection("Overview", "Test overview", null, "Win by having the most points"),
        setup = RuleSection("Setup", "Test setup", null, null),
        firstRound = RuleSection("First Round", "Test first round", null, null),
        advanced = RuleSection("Advanced", "Test advanced", null, null)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads game and rules successfully`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertEquals("Test Game", state.game?.title)
        assertEquals("Test overview", state.rules?.overview?.content)
        assertNull(state.error)
    }

    @Test
    fun `handles game load error`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Error("Game not found")
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertTrue(state.error?.contains("Failed to load game") == true)
    }

    @Test
    fun `handles rules load error`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Error("Rules not found")
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals(testGame, state.game)
        assertNull(state.rules)
        assertTrue(state.error?.contains("Failed to load rules") == true)
    }

    @Test
    fun `retry clears error and reloads`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Error("Initial error")
        )
        val viewModel = RulesViewModel("game-1", repository)

        // Verify initial error state
        assertTrue(viewModel.uiState.value.error != null)

        // Update repository to return success
        repository.gameResult = Result.Success(testGame)
        repository.rulesResult = Result.Success(testRules)

        // Retry
        viewModel.retry()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `loads overview win condition correctly`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals("Win by having the most points", state.rules?.overview?.winCondition)
        assertNull(state.rules?.setup?.winCondition)
        assertNull(state.rules?.firstRound?.winCondition)
        assertNull(state.rules?.advanced?.winCondition)
    }
}

/**
 * Fake implementation of GameRepository for testing.
 */
private class FakeGameRepository(
    var gameResult: Result<Game> = Result.Error("Not configured"),
    var rulesResult: Result<Rules> = Result.Error("Not configured")
) : GameRepository {
    override suspend fun getGames(): Result<List<Game>> {
        throw NotImplementedError()
    }

    override suspend fun getGameById(id: String): Result<Game> {
        return gameResult
    }

    override suspend fun saveGame(game: Game): Result<Unit> {
        throw NotImplementedError()
    }

    override suspend fun deleteGame(id: String): Result<Unit> {
        throw NotImplementedError()
    }

    override suspend fun getRulesForGame(gameId: String): Result<Rules> {
        return rulesResult
    }

    override suspend fun saveGameWithRules(game: Game, rules: Rules, rawJson: String): Result<String> {
        throw NotImplementedError()
    }
}
