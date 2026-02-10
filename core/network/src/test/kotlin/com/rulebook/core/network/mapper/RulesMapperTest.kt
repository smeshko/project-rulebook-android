package com.rulebook.core.network.mapper

import com.rulebook.core.network.model.GenerateResponse
import com.rulebook.core.network.model.RulesSection
import org.junit.Test
import kotlin.test.assertEquals

class RulesMapperTest {

    @Test
    fun `toDomain maps all sections by title match`() {
        // Arrange
        val response = GenerateResponse(
            gameTitle = "Catan",
            rulesSummary = "A trading game",
            rulesSections = listOf(
                RulesSection(title = "Overview", content = "This is the overview"),
                RulesSection(title = "Setup", content = "This is the setup"),
                RulesSection(title = "First Round", content = "This is the first round"),
                RulesSection(title = "Advanced", content = "These are advanced rules")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Catan", rules.gameId)
        assertEquals("Overview", rules.overview.title)
        assertEquals("This is the overview", rules.overview.content)
        assertEquals("Setup", rules.setup.title)
        assertEquals("This is the setup", rules.setup.content)
        assertEquals("First Round", rules.firstRound.title)
        assertEquals("This is the first round", rules.firstRound.content)
        assertEquals("Advanced", rules.advanced.title)
        assertEquals("These are advanced rules", rules.advanced.content)
    }

    @Test
    fun `toDomain handles case-insensitive title matching`() {
        // Arrange
        val response = GenerateResponse(
            gameTitle = "Risk",
            rulesSummary = "A strategy game",
            rulesSections = listOf(
                RulesSection(title = "OVERVIEW", content = "Overview content"),
                RulesSection(title = "setup", content = "Setup content"),
                RulesSection(title = "First ROUND", content = "First round content"),
                RulesSection(title = "advanced", content = "Advanced content")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("OVERVIEW", rules.overview.title)
        assertEquals("setup", rules.setup.title)
        assertEquals("First ROUND", rules.firstRound.title)
        assertEquals("advanced", rules.advanced.title)
    }

    @Test
    fun `toDomain uses fallback titles when sections are missing`() {
        // Arrange - Empty sections list, rulesSummary used as overview fallback
        val response = GenerateResponse(
            gameTitle = "Chess",
            rulesSummary = "The classic game",
            rulesSections = emptyList()
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Chess", rules.gameId)
        assertEquals("Overview", rules.overview.title)
        assertEquals("The classic game", rules.overview.content)
        assertEquals("Setup", rules.setup.title)
        assertEquals("", rules.setup.content)
        assertEquals("First round", rules.firstRound.title)
        assertEquals("", rules.firstRound.content)
        assertEquals("Advanced", rules.advanced.title)
        assertEquals("", rules.advanced.content)
    }

    @Test
    fun `toDomain uses positional mapping when some sections are missing`() {
        // Arrange - Only 2 sections provided
        val response = GenerateResponse(
            gameTitle = "Monopoly",
            rulesSummary = "A property game",
            rulesSections = listOf(
                RulesSection(title = "Introduction", content = "Intro content"),
                RulesSection(title = "Game Setup", content = "Setup content")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        // First two should use positional fallback since titles don't match standard keys
        assertEquals("Introduction", rules.overview.title)
        assertEquals("Intro content", rules.overview.content)
        assertEquals("Game Setup", rules.setup.title)
        assertEquals("Setup content", rules.setup.content)
        // Last two should use empty defaults
        assertEquals("First round", rules.firstRound.title)
        assertEquals("", rules.firstRound.content)
        assertEquals("Advanced", rules.advanced.title)
        assertEquals("", rules.advanced.content)
    }

    @Test
    fun `toDomain handles alternate title variations`() {
        // Arrange - Using alternate titles that should match
        val response = GenerateResponse(
            gameTitle = "Ticket to Ride",
            rulesSummary = "A train game",
            rulesSections = listOf(
                RulesSection(title = "Summary", content = "Summary content"),
                RulesSection(title = "Preparation", content = "Preparation content"),
                RulesSection(title = "How to Play", content = "How to play content"),
                RulesSection(title = "Detailed Rules", content = "Detailed rules content")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Summary", rules.overview.title)
        assertEquals("Summary content", rules.overview.content)
        assertEquals("Preparation", rules.setup.title)
        assertEquals("Preparation content", rules.setup.content)
        assertEquals("How to Play", rules.firstRound.title)
        assertEquals("How to play content", rules.firstRound.content)
        assertEquals("Detailed Rules", rules.advanced.title)
        assertEquals("Detailed rules content", rules.advanced.content)
    }

    @Test
    fun `toDomain handles extra sections by ignoring them`() {
        // Arrange - More than 4 sections
        val response = GenerateResponse(
            gameTitle = "Pandemic",
            rulesSummary = "A cooperative game",
            rulesSections = listOf(
                RulesSection(title = "Overview", content = "Overview content"),
                RulesSection(title = "Setup", content = "Setup content"),
                RulesSection(title = "First Round", content = "First round content"),
                RulesSection(title = "Advanced", content = "Advanced content"),
                RulesSection(title = "Expansions", content = "Expansion rules"),
                RulesSection(title = "FAQ", content = "FAQ content")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert - Should map only the first 4 that match
        assertEquals("Overview", rules.overview.title)
        assertEquals("Setup", rules.setup.title)
        assertEquals("First Round", rules.firstRound.title)
        assertEquals("Advanced", rules.advanced.title)
    }

    @Test
    fun `toDomain uses rulesSummary as overview fallback when sections are empty`() {
        // Arrange - No sections but rulesSummary is provided
        val response = GenerateResponse(
            gameTitle = "Chess",
            rulesSummary = "A classic strategy game for two players",
            rulesSections = emptyList()
        )

        // Act
        val rules = response.toDomain()

        // Assert - Overview should use rulesSummary as fallback content
        assertEquals("Overview", rules.overview.title)
        assertEquals("A classic strategy game for two players", rules.overview.content)
        // Other sections still get empty defaults
        assertEquals("", rules.setup.content)
        assertEquals("", rules.firstRound.content)
        assertEquals("", rules.advanced.content)
    }

    @Test
    fun `toDomain extracts winCondition from overview content with 'win by' pattern`() {
        // Arrange - Overview with explicit win condition using "win by"
        val response = GenerateResponse(
            gameTitle = "Catan",
            rulesSummary = "A trading game",
            rulesSections = listOf(
                RulesSection(
                    title = "Overview",
                    content = "Catan is a game about building settlements and cities. Win by being the first player to reach 10 victory points."
                ),
                RulesSection(title = "Setup", content = "Setup instructions"),
                RulesSection(title = "First Round", content = "First round instructions"),
                RulesSection(title = "Advanced", content = "Advanced rules")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Catan is a game about building settlements and cities.", rules.overview.content)
        assertEquals("Win by being the first player to reach 10 victory points.", rules.overview.winCondition)
    }

    @Test
    fun `toDomain extracts winCondition from overview content with 'wins' pattern`() {
        // Arrange - Overview with win condition using "player who wins"
        val response = GenerateResponse(
            gameTitle = "Risk",
            rulesSummary = "A strategy game",
            rulesSections = listOf(
                RulesSection(
                    title = "Overview",
                    content = "Risk is a strategy game of conquest. The player who eliminates all opponents wins the game."
                ),
                RulesSection(title = "Setup", content = "Setup"),
                RulesSection(title = "First Round", content = "First"),
                RulesSection(title = "Advanced", content = "Advanced")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Risk is a strategy game of conquest.", rules.overview.content)
        assertEquals("The player who eliminates all opponents wins the game.", rules.overview.winCondition)
    }

    @Test
    fun `toDomain extracts winCondition with 'win condition' pattern`() {
        // Arrange
        val response = GenerateResponse(
            gameTitle = "Pandemic",
            rulesSummary = "A cooperative game",
            rulesSections = listOf(
                RulesSection(
                    title = "Overview",
                    content = "Pandemic is a cooperative game where players work together to stop diseases. Win condition: Cure all four diseases before running out of time."
                ),
                RulesSection(title = "Setup", content = "Setup"),
                RulesSection(title = "First Round", content = "First"),
                RulesSection(title = "Advanced", content = "Advanced")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Pandemic is a cooperative game where players work together to stop diseases.", rules.overview.content)
        assertEquals("Win condition: Cure all four diseases before running out of time.", rules.overview.winCondition)
    }

    @Test
    fun `toDomain extracts winCondition with 'goal is to' pattern`() {
        // Arrange
        val response = GenerateResponse(
            gameTitle = "Ticket to Ride",
            rulesSummary = "A train game",
            rulesSections = listOf(
                RulesSection(
                    title = "Overview",
                    content = "Ticket to Ride is a railway-themed board game. Your goal is to score the most points by completing routes on the map."
                ),
                RulesSection(title = "Setup", content = "Setup"),
                RulesSection(title = "First Round", content = "First"),
                RulesSection(title = "Advanced", content = "Advanced")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Ticket to Ride is a railway-themed board game.", rules.overview.content)
        assertEquals("Your goal is to score the most points by completing routes on the map.", rules.overview.winCondition)
    }

    @Test
    fun `toDomain extracts winCondition with 'objective is' pattern`() {
        // Arrange
        val response = GenerateResponse(
            gameTitle = "Azul",
            rulesSummary = "A tile-laying game",
            rulesSections = listOf(
                RulesSection(
                    title = "Overview",
                    content = "Azul is a beautiful tile-laying game. The objective is to create the most beautiful wall decoration and score the most points."
                ),
                RulesSection(title = "Setup", content = "Setup"),
                RulesSection(title = "First Round", content = "First"),
                RulesSection(title = "Advanced", content = "Advanced")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert
        assertEquals("Azul is a beautiful tile-laying game.", rules.overview.content)
        assertEquals("The objective is to create the most beautiful wall decoration and score the most points.", rules.overview.winCondition)
    }

    @Test
    fun `toDomain leaves winCondition null when no pattern found`() {
        // Arrange - Overview without any win condition patterns
        val response = GenerateResponse(
            gameTitle = "Chess",
            rulesSummary = "A strategy game",
            rulesSections = listOf(
                RulesSection(
                    title = "Overview",
                    content = "Chess is a classic strategy game for two players on an 8x8 board."
                ),
                RulesSection(title = "Setup", content = "Setup"),
                RulesSection(title = "First Round", content = "First"),
                RulesSection(title = "Advanced", content = "Advanced")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert - No extraction, full content remains, winCondition is null
        assertEquals("Chess is a classic strategy game for two players on an 8x8 board.", rules.overview.content)
        assertEquals(null, rules.overview.winCondition)
    }

    @Test
    fun `toDomain does not extract winCondition from non-overview sections`() {
        // Arrange - Setup section with win-related text should not extract
        val response = GenerateResponse(
            gameTitle = "Game",
            rulesSummary = "A game",
            rulesSections = listOf(
                RulesSection(title = "Overview", content = "This is the overview."),
                RulesSection(
                    title = "Setup",
                    content = "Place the board. The player who sets up fastest wins bragging rights."
                ),
                RulesSection(title = "First Round", content = "First"),
                RulesSection(title = "Advanced", content = "Advanced")
            )
        )

        // Act
        val rules = response.toDomain()

        // Assert - Setup winCondition should be null (only overview extracts)
        assertEquals(null, rules.setup.winCondition)
        assertEquals("Place the board. The player who sets up fastest wins bragging rights.", rules.setup.content)
    }
}
