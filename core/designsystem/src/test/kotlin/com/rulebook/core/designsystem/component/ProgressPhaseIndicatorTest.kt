package com.rulebook.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressPhaseIndicatorTest {

    @Test
    fun `PhaseStatus enum has 3 values`() {
        assertEquals(3, PhaseStatus.entries.size)
    }

    @Test
    fun `PhaseStatus has PENDING value`() {
        assertNotNull(PhaseStatus.PENDING)
        assertEquals("PENDING", PhaseStatus.PENDING.name)
    }

    @Test
    fun `PhaseStatus has ACTIVE value`() {
        assertNotNull(PhaseStatus.ACTIVE)
        assertEquals("ACTIVE", PhaseStatus.ACTIVE.name)
    }

    @Test
    fun `PhaseStatus has COMPLETED value`() {
        assertNotNull(PhaseStatus.COMPLETED)
        assertEquals("COMPLETED", PhaseStatus.COMPLETED.name)
    }

    @Test
    fun `PhaseItem data class holds correct values`() {
        val item = PhaseItem(
            name = "Processing Image",
            status = PhaseStatus.ACTIVE,
            phaseNumber = 1
        )
        assertEquals("Processing Image", item.name)
        assertEquals(PhaseStatus.ACTIVE, item.status)
        assertEquals(1, item.phaseNumber)
    }

    @Test
    fun `PhaseItem equality works correctly`() {
        val item1 = PhaseItem("Test", PhaseStatus.PENDING, 1)
        val item2 = PhaseItem("Test", PhaseStatus.PENDING, 1)
        assertEquals(item1, item2)
    }

    @Test
    fun `PhaseItem copy updates status`() {
        val item = PhaseItem("Test", PhaseStatus.PENDING, 1)
        val updated = item.copy(status = PhaseStatus.COMPLETED)
        assertEquals(PhaseStatus.COMPLETED, updated.status)
        assertEquals("Test", updated.name)
    }

    @Test
    fun `PhaseStatus enum exists as enum type`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.PhaseStatus")
        assertNotNull(clazz)
        assertTrue(clazz.isEnum)
    }

    @Test
    fun `PhaseItem data class exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.PhaseItem")
        assertNotNull(clazz)
    }
}
