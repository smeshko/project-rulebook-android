package com.rulebook.feature.generation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScanPhaseTest {

    @Test
    fun `ScanPhase has exactly 5 phases`() {
        assertEquals(5, ScanPhase.entries.size)
    }

    @Test
    fun `TOTAL_PHASES companion returns 5`() {
        assertEquals(5, ScanPhase.TOTAL_PHASES)
    }

    @Test
    fun `phases are in correct order`() {
        val phases = ScanPhase.entries
        assertEquals(ScanPhase.PROCESSING_IMAGE, phases[0])
        assertEquals(ScanPhase.ANALYZING_IMAGE, phases[1])
        assertEquals(ScanPhase.IDENTIFYING_GAME, phases[2])
        assertEquals(ScanPhase.GENERATING_RULES, phases[3])
        assertEquals(ScanPhase.SAVING_RULES, phases[4])
    }

    @Test
    fun `phaseNumber returns 1-based index`() {
        assertEquals(1, ScanPhase.PROCESSING_IMAGE.phaseNumber)
        assertEquals(2, ScanPhase.ANALYZING_IMAGE.phaseNumber)
        assertEquals(3, ScanPhase.IDENTIFYING_GAME.phaseNumber)
        assertEquals(4, ScanPhase.GENERATING_RULES.phaseNumber)
        assertEquals(5, ScanPhase.SAVING_RULES.phaseNumber)
    }

    @Test
    fun `PROCESSING_IMAGE has correct progress range 0-15 percent`() {
        assertEquals(0f, ScanPhase.PROCESSING_IMAGE.startProgress)
        assertEquals(0.15f, ScanPhase.PROCESSING_IMAGE.endProgress)
    }

    @Test
    fun `ANALYZING_IMAGE has correct progress range 15-40 percent`() {
        assertEquals(0.15f, ScanPhase.ANALYZING_IMAGE.startProgress)
        assertEquals(0.40f, ScanPhase.ANALYZING_IMAGE.endProgress)
    }

    @Test
    fun `IDENTIFYING_GAME has correct progress range 40-60 percent`() {
        assertEquals(0.40f, ScanPhase.IDENTIFYING_GAME.startProgress)
        assertEquals(0.60f, ScanPhase.IDENTIFYING_GAME.endProgress)
    }

    @Test
    fun `GENERATING_RULES has correct progress range 60-90 percent`() {
        assertEquals(0.60f, ScanPhase.GENERATING_RULES.startProgress)
        assertEquals(0.90f, ScanPhase.GENERATING_RULES.endProgress)
    }

    @Test
    fun `SAVING_RULES has correct progress range 90-100 percent`() {
        assertEquals(0.90f, ScanPhase.SAVING_RULES.startProgress)
        assertEquals(1.0f, ScanPhase.SAVING_RULES.endProgress)
    }

    @Test
    fun `phases have contiguous progress ranges`() {
        val phases = ScanPhase.entries
        for (i in 0 until phases.size - 1) {
            assertEquals(
                phases[i].endProgress,
                phases[i + 1].startProgress,
                "Phase ${phases[i].name} endProgress should equal ${phases[i + 1].name} startProgress"
            )
        }
    }

    @Test
    fun `first phase starts at 0 and last phase ends at 1`() {
        assertEquals(0f, ScanPhase.entries.first().startProgress)
        assertEquals(1.0f, ScanPhase.entries.last().endProgress)
    }

    @Test
    fun `all phases have non-empty display names`() {
        ScanPhase.entries.forEach { phase ->
            assertTrue(phase.displayName.isNotBlank(), "${phase.name} should have a non-empty displayName")
        }
    }

    @Test
    fun `all phases have non-empty messages`() {
        ScanPhase.entries.forEach { phase ->
            assertTrue(phase.message.isNotBlank(), "${phase.name} should have a non-empty message")
        }
    }

    @Test
    fun `PROCESSING_IMAGE display name is Processing Image`() {
        assertEquals("Processing Image", ScanPhase.PROCESSING_IMAGE.displayName)
    }

    @Test
    fun `ANALYZING_IMAGE display name is Analyzing Image`() {
        assertEquals("Analyzing Image", ScanPhase.ANALYZING_IMAGE.displayName)
    }

    @Test
    fun `IDENTIFYING_GAME display name is Identifying Game`() {
        assertEquals("Identifying Game", ScanPhase.IDENTIFYING_GAME.displayName)
    }

    @Test
    fun `GENERATING_RULES display name is Generating Rules`() {
        assertEquals("Generating Rules", ScanPhase.GENERATING_RULES.displayName)
    }

    @Test
    fun `SAVING_RULES display name is Saving Rules`() {
        assertEquals("Saving Rules", ScanPhase.SAVING_RULES.displayName)
    }
}
