package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RuleSectionTest {

    @Test
    fun `create RuleSection with all properties`() {
        val section = RuleSection(
            title = "Overview",
            content = "This is the game overview.",
            items = listOf("Item 1", "Item 2", "Item 3")
        )

        assertEquals("Overview", section.title)
        assertEquals("This is the game overview.", section.content)
        assertEquals(listOf("Item 1", "Item 2", "Item 3"), section.items)
    }

    @Test
    fun `create RuleSection with null items by default`() {
        val section = RuleSection(
            title = "Setup",
            content = "Set up the board."
        )

        assertEquals("Setup", section.title)
        assertEquals("Set up the board.", section.content)
        assertNull(section.items)
    }

    @Test
    fun `create RuleSection with explicit null items`() {
        val section = RuleSection(
            title = "Advanced",
            content = "Advanced rules.",
            items = null
        )

        assertNull(section.items)
    }

    @Test
    fun `create RuleSection with empty items list`() {
        val section = RuleSection(
            title = "First Round",
            content = "First round instructions.",
            items = emptyList()
        )

        assertEquals(emptyList<String>(), section.items)
    }

    @Test
    fun `RuleSection data class equals works correctly`() {
        val section1 = RuleSection(
            title = "Overview",
            content = "Content",
            items = listOf("Item 1")
        )
        val section2 = RuleSection(
            title = "Overview",
            content = "Content",
            items = listOf("Item 1")
        )

        assertEquals(section1, section2)
        assertEquals(section1.hashCode(), section2.hashCode())
    }

    @Test
    fun `RuleSection data class copy works correctly`() {
        val original = RuleSection(
            title = "Overview",
            content = "Original content",
            items = listOf("Item 1")
        )
        val updated = original.copy(content = "Updated content")

        assertEquals(original.title, updated.title)
        assertEquals("Updated content", updated.content)
        assertEquals(original.items, updated.items)
        assertNotEquals(original, updated)
    }

    @Test
    fun `create RuleSection with winCondition field`() {
        val section = RuleSection(
            title = "Overview",
            content = "This is the game overview.",
            items = listOf("Item 1"),
            winCondition = "Be the first player to collect 10 victory points"
        )

        assertEquals("Overview", section.title)
        assertEquals("This is the game overview.", section.content)
        assertEquals(listOf("Item 1"), section.items)
        assertEquals("Be the first player to collect 10 victory points", section.winCondition)
    }

    @Test
    fun `create RuleSection with null winCondition by default`() {
        val section = RuleSection(
            title = "Setup",
            content = "Set up the board."
        )

        assertNull(section.winCondition)
    }

    @Test
    fun `RuleSection with winCondition equals works correctly`() {
        val section1 = RuleSection(
            title = "Overview",
            content = "Content",
            items = listOf("Item 1"),
            winCondition = "Win condition"
        )
        val section2 = RuleSection(
            title = "Overview",
            content = "Content",
            items = listOf("Item 1"),
            winCondition = "Win condition"
        )

        assertEquals(section1, section2)
        assertEquals(section1.hashCode(), section2.hashCode())
    }
}
