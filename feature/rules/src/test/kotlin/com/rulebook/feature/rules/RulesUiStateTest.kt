package com.rulebook.feature.rules

import com.rulebook.core.model.Game
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RulesUiState.
 */
class RulesUiStateTest {

    private val testGame = Game(
        id = "game-1",
        title = "Test Game",
        thumbnailUrl = null,
        createdAt = 1000L,
        lastAccessedAt = 2000L
    )

    private val testRules = Rules(
        gameId = "game-1",
        overview = RuleSection("Overview", "Test overview", null),
        setup = RuleSection("Setup", "Test setup", null),
        firstRound = RuleSection("First Round", "Test first round", null),
        advanced = RuleSection("Advanced", "Test advanced", null)
    )

    @Test
    fun `initial state is loading`() {
        val state = RulesUiState()

        assertTrue(state.isLoading)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `isSuccess is true when game and rules loaded without error`() {
        val state = RulesUiState(
            game = testGame,
            rules = testRules,
            isLoading = false,
            error = null
        )

        assertTrue(state.isSuccess)
    }

    @Test
    fun `isSuccess is false when loading`() {
        val state = RulesUiState(
            game = testGame,
            rules = testRules,
            isLoading = true,
            error = null
        )

        assertFalse(state.isSuccess)
    }

    @Test
    fun `isSuccess is false when error exists`() {
        val state = RulesUiState(
            game = testGame,
            rules = testRules,
            isLoading = false,
            error = "Test error"
        )

        assertFalse(state.isSuccess)
    }

    @Test
    fun `isSuccess is false when game is null`() {
        val state = RulesUiState(
            game = null,
            rules = testRules,
            isLoading = false,
            error = null
        )

        assertFalse(state.isSuccess)
    }

    @Test
    fun `isSuccess is false when rules is null`() {
        val state = RulesUiState(
            game = testGame,
            rules = null,
            isLoading = false,
            error = null
        )

        assertFalse(state.isSuccess)
    }
}
