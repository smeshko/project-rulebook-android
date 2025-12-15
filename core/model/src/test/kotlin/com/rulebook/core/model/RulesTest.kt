package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RulesTest {

    private val overviewSection = RuleSection(
        title = "Overview",
        content = "Game overview content"
    )

    private val setupSection = RuleSection(
        title = "Setup",
        content = "Setup instructions",
        items = listOf("Place board", "Shuffle cards", "Deal pieces")
    )

    private val firstRoundSection = RuleSection(
        title = "First Round",
        content = "First round instructions"
    )

    private val advancedSection = RuleSection(
        title = "Advanced Rules",
        content = "Advanced gameplay options"
    )

    @Test
    fun `create Rules with all sections`() {
        val rules = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        assertEquals("game-123", rules.gameId)
        assertEquals(overviewSection, rules.overview)
        assertEquals(setupSection, rules.setup)
        assertEquals(firstRoundSection, rules.firstRound)
        assertEquals(advancedSection, rules.advanced)
    }

    @Test
    fun `Rules sections contain correct content`() {
        val rules = Rules(
            gameId = "catan-001",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        assertEquals("Overview", rules.overview.title)
        assertEquals("Setup", rules.setup.title)
        assertEquals(listOf("Place board", "Shuffle cards", "Deal pieces"), rules.setup.items)
        assertEquals("First Round", rules.firstRound.title)
        assertEquals("Advanced Rules", rules.advanced.title)
    }

    @Test
    fun `Rules data class equals works correctly`() {
        val rules1 = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )
        val rules2 = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        assertEquals(rules1, rules2)
        assertEquals(rules1.hashCode(), rules2.hashCode())
    }

    @Test
    fun `Rules data class copy works correctly`() {
        val original = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        val updatedOverview = RuleSection(
            title = "Updated Overview",
            content = "New content"
        )
        val updated = original.copy(overview = updatedOverview)

        assertEquals(original.gameId, updated.gameId)
        assertEquals(updatedOverview, updated.overview)
        assertEquals(original.setup, updated.setup)
        assertEquals(original.firstRound, updated.firstRound)
        assertEquals(original.advanced, updated.advanced)
        assertNotEquals(original, updated)
    }

    @Test
    fun `Rules with different gameId are not equal`() {
        val rules1 = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )
        val rules2 = Rules(
            gameId = "game-456",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        assertNotEquals(rules1, rules2)
    }
}
