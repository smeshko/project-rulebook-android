package com.rulebook.core.database.mapper

import com.rulebook.core.database.entity.RulesEntity
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesMapperTest {

    private val overviewSection = RuleSection(
        title = "Overview",
        content = "Game overview content",
        items = null
    )

    private val setupSection = RuleSection(
        title = "Setup",
        content = "Setup instructions",
        items = listOf("Place board", "Shuffle cards")
    )

    private val firstRoundSection = RuleSection(
        title = "First Round",
        content = "First round instructions",
        items = null
    )

    private val advancedSection = RuleSection(
        title = "Advanced",
        content = "Advanced rules",
        items = listOf("Rule 1", "Rule 2", "Rule 3")
    )

    @Test
    fun `Rules toEntity creates entity with JSON sections`() {
        val rules = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        val entity = rules.toEntity(id = "rules-001", rawJson = "{}")

        assertEquals("rules-001", entity.id)
        assertEquals("game-123", entity.gameId)
        // Verify JSON is created (contains expected content)
        assertTrue("Overview section should contain 'Overview'", entity.overview.contains("Overview"))
        assertTrue("Overview section should contain content", entity.overview.contains("Game overview content"))
        assertTrue("Setup section should contain 'Setup'", entity.setup.contains("Setup"))
        assertTrue("Setup section should contain items", entity.setup.contains("Place board"))
    }

    @Test
    fun `RulesEntity toDomain parses JSON sections correctly`() {
        // Create entity with JSON sections
        val rules = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )
        val entity = rules.toEntity(id = "rules-001", rawJson = "{}")

        // Convert back to domain
        val domain = entity.toDomain()

        assertEquals("game-123", domain.gameId)
        assertEquals("Overview", domain.overview.title)
        assertEquals("Game overview content", domain.overview.content)
        assertEquals("Setup", domain.setup.title)
        assertEquals(listOf("Place board", "Shuffle cards"), domain.setup.items)
        assertEquals("First Round", domain.firstRound.title)
        assertEquals("Advanced", domain.advanced.title)
        assertEquals(listOf("Rule 1", "Rule 2", "Rule 3"), domain.advanced.items)
    }

    @Test
    fun `round trip conversion preserves all data`() {
        val original = Rules(
            gameId = "game-456",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        val roundTrip = original.toEntity(id = "rules-002", rawJson = "{}").toDomain()

        assertEquals(original.gameId, roundTrip.gameId)
        assertEquals(original.overview, roundTrip.overview)
        assertEquals(original.setup, roundTrip.setup)
        assertEquals(original.firstRound, roundTrip.firstRound)
        assertEquals(original.advanced, roundTrip.advanced)
    }

    @Test
    fun `RulesEntity toDomain handles plain text fallback`() {
        // Entity with plain text instead of JSON (backwards compatibility)
        val entity = RulesEntity(
            id = "rules-legacy",
            gameId = "game-legacy",
            overview = "Plain text overview",
            setup = "Plain text setup",
            firstRound = "Plain text first round",
            advanced = "Plain text advanced",
            rawJson = "{}"
        )

        val domain = entity.toDomain()

        assertEquals("game-legacy", domain.gameId)
        assertEquals("Plain text overview", domain.overview.content)
        assertEquals("Plain text setup", domain.setup.content)
        assertEquals("Plain text first round", domain.firstRound.content)
        assertEquals("Plain text advanced", domain.advanced.content)
    }

    @Test
    fun `Rules toEntity with empty rawJson`() {
        val rules = Rules(
            gameId = "game-123",
            overview = overviewSection,
            setup = setupSection,
            firstRound = firstRoundSection,
            advanced = advancedSection
        )

        val entity = rules.toEntity(id = "rules-003")

        assertEquals("", entity.rawJson)
    }

    @Test
    fun `Rules sections with null items serialize correctly`() {
        val sectionWithoutItems = RuleSection(
            title = "Test",
            content = "Test content",
            items = null
        )
        val rules = Rules(
            gameId = "game-test",
            overview = sectionWithoutItems,
            setup = sectionWithoutItems,
            firstRound = sectionWithoutItems,
            advanced = sectionWithoutItems
        )

        val entity = rules.toEntity(id = "rules-test")
        val domain = entity.toDomain()

        assertEquals(null, domain.overview.items)
        assertEquals(null, domain.setup.items)
    }

    @Test
    fun `Rules sections with empty items list serialize correctly`() {
        val sectionWithEmptyItems = RuleSection(
            title = "Test",
            content = "Test content",
            items = emptyList()
        )
        val rules = Rules(
            gameId = "game-test",
            overview = sectionWithEmptyItems,
            setup = sectionWithEmptyItems,
            firstRound = sectionWithEmptyItems,
            advanced = sectionWithEmptyItems
        )

        val entity = rules.toEntity(id = "rules-test")
        val domain = entity.toDomain()

        assertEquals(emptyList<String>(), domain.overview.items)
    }
}
