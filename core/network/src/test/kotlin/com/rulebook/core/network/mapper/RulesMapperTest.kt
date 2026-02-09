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
}
