package com.rulebook.feature.generation

import com.rulebook.core.designsystem.component.PhaseStatus
import org.junit.Test
import kotlin.test.assertEquals

class GenerationScreenTest {

    @Test
    fun `buildPhaseItems returns 5 items`() {
        val items = buildPhaseItems(ScanPhase.PROCESSING_IMAGE)
        assertEquals(5, items.size)
    }

    @Test
    fun `buildPhaseItems marks first phase as ACTIVE when current is PROCESSING_IMAGE`() {
        val items = buildPhaseItems(ScanPhase.PROCESSING_IMAGE)
        assertEquals(PhaseStatus.ACTIVE, items[0].status)
        assertEquals(PhaseStatus.PENDING, items[1].status)
        assertEquals(PhaseStatus.PENDING, items[2].status)
        assertEquals(PhaseStatus.PENDING, items[3].status)
        assertEquals(PhaseStatus.PENDING, items[4].status)
    }

    @Test
    fun `buildPhaseItems marks earlier phases as COMPLETED`() {
        val items = buildPhaseItems(ScanPhase.IDENTIFYING_GAME)
        assertEquals(PhaseStatus.COMPLETED, items[0].status) // Processing Image
        assertEquals(PhaseStatus.COMPLETED, items[1].status) // Analyzing Image
        assertEquals(PhaseStatus.ACTIVE, items[2].status)    // Identifying Game
        assertEquals(PhaseStatus.PENDING, items[3].status)   // Generating Rules
        assertEquals(PhaseStatus.PENDING, items[4].status)   // Saving Rules
    }

    @Test
    fun `buildPhaseItems marks all earlier phases as COMPLETED for last phase`() {
        val items = buildPhaseItems(ScanPhase.SAVING_RULES)
        assertEquals(PhaseStatus.COMPLETED, items[0].status)
        assertEquals(PhaseStatus.COMPLETED, items[1].status)
        assertEquals(PhaseStatus.COMPLETED, items[2].status)
        assertEquals(PhaseStatus.COMPLETED, items[3].status)
        assertEquals(PhaseStatus.ACTIVE, items[4].status)
    }

    @Test
    fun `buildPhaseItems assigns correct phase numbers`() {
        val items = buildPhaseItems(ScanPhase.PROCESSING_IMAGE)
        assertEquals(1, items[0].phaseNumber)
        assertEquals(2, items[1].phaseNumber)
        assertEquals(3, items[2].phaseNumber)
        assertEquals(4, items[3].phaseNumber)
        assertEquals(5, items[4].phaseNumber)
    }

    @Test
    fun `buildPhaseItems assigns correct display names`() {
        val items = buildPhaseItems(ScanPhase.PROCESSING_IMAGE)
        assertEquals("Processing Image", items[0].name)
        assertEquals("Analyzing Image", items[1].name)
        assertEquals("Identifying Game", items[2].name)
        assertEquals("Generating Rules", items[3].name)
        assertEquals("Saving Rules", items[4].name)
    }

    @Test
    fun `buildPhaseItems for GENERATING_RULES has 3 completed phases`() {
        val items = buildPhaseItems(ScanPhase.GENERATING_RULES)
        val completedCount = items.count { it.status == PhaseStatus.COMPLETED }
        assertEquals(3, completedCount)
    }

    @Test
    fun `buildPhaseItems always has exactly one ACTIVE phase`() {
        ScanPhase.entries.forEach { currentPhase ->
            val items = buildPhaseItems(currentPhase)
            val activeCount = items.count { it.status == PhaseStatus.ACTIVE }
            assertEquals(1, activeCount, "Expected exactly 1 ACTIVE phase for ${currentPhase.name}")
        }
    }
}
