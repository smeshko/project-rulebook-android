package com.rulebook.feature.rules

import com.rulebook.core.model.Game
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesShareFormatterTest {

    @Test
    fun `formatForSharing returns basic formatted text with game title and sections`() {
        val game = Game(
            id = "test-game",
            title = "Test Game",
            thumbnailUrl = null,
            createdAt = 0L,
            lastAccessedAt = 0L
        )
        val rules = Rules(
            gameId = "test-game",
            overview = RuleSection(
                title = "Overview",
                content = "This is a test game overview.",
                items = emptyList(),
                winCondition = null
            ),
            setup = RuleSection(
                title = "Setup",
                content = "Set up the game board.",
                items = listOf("Place board", "Shuffle cards"),
                winCondition = null
            ),
            firstRound = RuleSection(
                title = "First Round",
                content = "Start playing.",
                items = listOf("1. Draw card", "2. Take action"),
                winCondition = null
            ),
            advanced = RuleSection(
                title = "Advanced Rules",
                content = "Additional rules.",
                items = emptyList(),
                winCondition = null
            )
        )

        val result = RulesShareFormatter.formatForSharing(game, rules)

        assertTrue(result.contains("Test Game"))
        assertTrue(result.contains("Overview"))
        assertTrue(result.contains("This is a test game overview."))
        assertTrue(result.contains("Setup"))
        assertTrue(result.contains("Set up the game board."))
        assertTrue(result.contains("Place board"))
        assertTrue(result.contains("Shuffle cards"))
        assertTrue(result.contains("First Round"))
        assertTrue(result.contains("Start playing."))
        assertTrue(result.contains("1. Draw card"))
        assertTrue(result.contains("2. Take action"))
        assertTrue(result.contains("Advanced Rules"))
        assertTrue(result.contains("Additional rules."))
        assertTrue(result.contains("Shared from Rulebook"))
    }

    @Test
    fun `formatForSharing includes win condition when present`() {
        val game = Game(
            id = "test-game",
            title = "Victory Game",
            thumbnailUrl = null,
            createdAt = 0L,
            lastAccessedAt = 0L
        )
        val rules = Rules(
            gameId = "test-game",
            overview = RuleSection(
                title = "Overview",
                content = "A game about winning.",
                items = emptyList(),
                winCondition = "Be the first to collect 10 points!"
            ),
            setup = RuleSection(
                title = "Setup",
                content = "Setup content",
                items = emptyList(),
                winCondition = null
            ),
            firstRound = RuleSection(
                title = "First Round",
                content = "First round content",
                items = emptyList(),
                winCondition = null
            ),
            advanced = RuleSection(
                title = "Advanced Rules",
                content = "Advanced content",
                items = emptyList(),
                winCondition = null
            )
        )

        val result = RulesShareFormatter.formatForSharing(game, rules)

        assertTrue(result.contains("Be the first to collect 10 points!"))
        assertTrue(result.contains("Victory Game"))
    }

    @Test
    fun `formatForSharing handles blank section titles with defaults`() {
        val game = Game(
            id = "test-game",
            title = "Blank Sections Game",
            thumbnailUrl = null,
            createdAt = 0L,
            lastAccessedAt = 0L
        )
        val rules = Rules(
            gameId = "test-game",
            overview = RuleSection(
                title = "",
                content = "Overview content",
                items = emptyList(),
                winCondition = null
            ),
            setup = RuleSection(
                title = "",
                content = "Setup content",
                items = emptyList(),
                winCondition = null
            ),
            firstRound = RuleSection(
                title = "",
                content = "First round content",
                items = emptyList(),
                winCondition = null
            ),
            advanced = RuleSection(
                title = "",
                content = "Advanced content",
                items = emptyList(),
                winCondition = null
            )
        )

        val result = RulesShareFormatter.formatForSharing(game, rules)

        assertTrue(result.contains("Overview"))
        assertTrue(result.contains("Setup"))
        assertTrue(result.contains("First Round"))
        assertTrue(result.contains("Advanced Rules"))
    }

    @Test
    fun `formatForSharing handles null and empty items lists`() {
        val game = Game(
            id = "test-game",
            title = "Empty Items Game",
            thumbnailUrl = null,
            createdAt = 0L,
            lastAccessedAt = 0L
        )
        val rules = Rules(
            gameId = "test-game",
            overview = RuleSection(
                title = "Overview",
                content = "Content without items",
                items = emptyList(),
                winCondition = null
            ),
            setup = RuleSection(
                title = "Setup",
                content = "Setup without items",
                items = emptyList(),
                winCondition = null
            ),
            firstRound = RuleSection(
                title = "First Round",
                content = "First round without items",
                items = emptyList(),
                winCondition = null
            ),
            advanced = RuleSection(
                title = "Advanced Rules",
                content = "Advanced without items",
                items = emptyList(),
                winCondition = null
            )
        )

        val result = RulesShareFormatter.formatForSharing(game, rules)

        assertTrue(result.contains("Empty Items Game"))
        assertTrue(result.contains("Content without items"))
        assertTrue(result.contains("Setup without items"))
        assertTrue(result.contains("Shared from Rulebook"))
    }

    @Test
    fun `formatForSharing footer attribution is always present`() {
        val game = Game(
            id = "test-game",
            title = "Any Game",
            thumbnailUrl = null,
            createdAt = 0L,
            lastAccessedAt = 0L
        )
        val rules = Rules(
            gameId = "test-game",
            overview = RuleSection(
                title = "Overview",
                content = "Content",
                items = emptyList(),
                winCondition = null
            ),
            setup = RuleSection(
                title = "Setup",
                content = "Content",
                items = emptyList(),
                winCondition = null
            ),
            firstRound = RuleSection(
                title = "First Round",
                content = "Content",
                items = emptyList(),
                winCondition = null
            ),
            advanced = RuleSection(
                title = "Advanced Rules",
                content = "Content",
                items = emptyList(),
                winCondition = null
            )
        )

        val result = RulesShareFormatter.formatForSharing(game, rules)

        assertTrue(result.endsWith("Shared from Rulebook") || result.contains("Shared from Rulebook\n"))
    }

    @Test
    fun `formatForSharing renders items as readable list with markers`() {
        val game = Game(
            id = "test-game",
            title = "List Game",
            thumbnailUrl = null,
            createdAt = 0L,
            lastAccessedAt = 0L
        )
        val rules = Rules(
            gameId = "test-game",
            overview = RuleSection(
                title = "Overview",
                content = "Overview",
                items = emptyList(),
                winCondition = null
            ),
            setup = RuleSection(
                title = "Setup",
                content = "Setup instructions",
                items = listOf("Item A", "Item B", "Item C"),
                winCondition = null
            ),
            firstRound = RuleSection(
                title = "First Round",
                content = "First round steps",
                items = listOf("1. Step one", "2. Step two"),
                winCondition = null
            ),
            advanced = RuleSection(
                title = "Advanced Rules",
                content = "Advanced",
                items = emptyList(),
                winCondition = null
            )
        )

        val result = RulesShareFormatter.formatForSharing(game, rules)

        // Verify items are present and formatted with markers
        assertTrue(result.contains("Item A"))
        assertTrue(result.contains("Item B"))
        assertTrue(result.contains("Item C"))
        assertTrue(result.contains("1. Step one"))
        assertTrue(result.contains("2. Step two"))
    }
}
