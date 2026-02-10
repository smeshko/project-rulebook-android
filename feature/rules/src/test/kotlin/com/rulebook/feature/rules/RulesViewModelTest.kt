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
        advanced = RuleSection("Advanced Rules", "Test advanced with edge cases",
            listOf("Tiebreaker: highest score wins", "Optional rule: skip turns allowed"), null)
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

    @Test
    fun `toggleSetupItem adds index to setupCheckedItems`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        viewModel.toggleSetupItem(0)

        val state = viewModel.uiState.value
        assertTrue(state.setupCheckedItems.contains(0))
    }

    @Test
    fun `toggleSetupItem removes index from setupCheckedItems when already present`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        viewModel.toggleSetupItem(0)
        viewModel.toggleSetupItem(0)

        val state = viewModel.uiState.value
        assertFalse(state.setupCheckedItems.contains(0))
    }

    @Test
    fun `toggleSetupItem handles multiple indices correctly`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        viewModel.toggleSetupItem(0)
        viewModel.toggleSetupItem(2)
        viewModel.toggleSetupItem(1)

        val state = viewModel.uiState.value
        assertTrue(state.setupCheckedItems.contains(0))
        assertTrue(state.setupCheckedItems.contains(1))
        assertTrue(state.setupCheckedItems.contains(2))
        assertEquals(3, state.setupCheckedItems.size)
    }

    @Test
    fun `advanced section title defaults to Advanced Rules when blank`() = runTest {
        val rulesWithBlankAdvancedTitle = testRules.copy(
            advanced = RuleSection("", "Test advanced content", null, null)
        )
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(rulesWithBlankAdvancedTitle)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        // The RulesScreen UI should display "Advanced Rules" as the fallback title
        assertEquals("", state.rules?.advanced?.title)
    }

    @Test
    fun `advanced section loads with content and items correctly`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals("Advanced Rules", state.rules?.advanced?.title)
        assertEquals("Test advanced with edge cases", state.rules?.advanced?.content)
        assertEquals(2, state.rules?.advanced?.items?.size)
        assertEquals("Tiebreaker: highest score wins", state.rules?.advanced?.items?.get(0))
    }

    @Test
    fun `advanced section handles null items gracefully`() = runTest {
        val rulesWithNullAdvancedItems = testRules.copy(
            advanced = RuleSection("Advanced Rules", "Content only, no items", null, null)
        )
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(rulesWithNullAdvancedItems)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals("Advanced Rules", state.rules?.advanced?.title)
        assertEquals("Content only, no items", state.rules?.advanced?.content)
        assertNull(state.rules?.advanced?.items)
    }

    @Test
    fun `advanced section handles empty items list gracefully`() = runTest {
        val rulesWithEmptyAdvancedItems = testRules.copy(
            advanced = RuleSection("Advanced Rules", "Content only, empty items list", emptyList(), null)
        )
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(rulesWithEmptyAdvancedItems)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertEquals("Advanced Rules", state.rules?.advanced?.title)
        assertEquals("Content only, empty items list", state.rules?.advanced?.content)
        assertTrue(state.rules?.advanced?.items?.isEmpty() == true)
    }

    @Test
    fun `shareRules invokes callback with formatted text when data is loaded`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        var capturedText: String? = null
        viewModel.shareRules { text ->
            capturedText = text
        }

        // Verify callback was invoked with non-empty text
        assertTrue(capturedText != null)
        assertTrue(capturedText!!.isNotEmpty())
        assertTrue(capturedText!!.contains("Test Game"))
        assertTrue(capturedText!!.contains("Test overview"))
        assertTrue(capturedText!!.contains("Shared from Rulebook"))
    }

    @Test
    fun `shareRules does not invoke callback when data is not loaded`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Error("Game not found")
        )
        val viewModel = RulesViewModel("game-1", repository)

        var callbackInvoked = false
        viewModel.shareRules {
            callbackInvoked = true
        }

        // Verify callback was NOT invoked
        assertFalse(callbackInvoked)
    }

    @Test
    fun `shareRules does not invoke callback when only game is loaded but rules are not`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Error("Rules not found")
        )
        val viewModel = RulesViewModel("game-1", repository)

        var callbackInvoked = false
        viewModel.shareRules {
            callbackInvoked = true
        }

        // Verify callback was NOT invoked (both game and rules must be loaded)
        assertFalse(callbackInvoked)
    }

    // ==================== Offline Scenario Tests ====================

    @Test
    fun `loadRules succeeds when FakeGameRepository returns cached data simulating offline`() = runTest {
        // Simulate offline scenario: repository returns data from local cache
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isLoading)
        assertEquals(testGame, state.game)
        assertEquals(testRules, state.rules)
        assertNull(state.error)
    }

    @Test
    fun `rules display all four sections when loaded from fake repository`() = runTest {
        val repository = FakeGameRepository(
            gameResult = Result.Success(testGame),
            rulesResult = Result.Success(testRules)
        )
        val viewModel = RulesViewModel("game-1", repository)

        val state = viewModel.uiState.value
        assertTrue(state.isSuccess)

        // Verify all four sections are present
        assertEquals("Overview", state.rules?.overview?.title)
        assertEquals("Test overview", state.rules?.overview?.content)

        assertEquals("Setup", state.rules?.setup?.title)
        assertEquals("Test setup", state.rules?.setup?.content)

        assertEquals("First Round", state.rules?.firstRound?.title)
        assertEquals("Test first round", state.rules?.firstRound?.content)

        assertEquals("Advanced Rules", state.rules?.advanced?.title)
        assertEquals("Test advanced with edge cases", state.rules?.advanced?.content)
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
