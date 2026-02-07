package com.rulebook.feature.generation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class GenerationUiStateTest {

    @Test
    fun `default state has PROCESSING_IMAGE phase`() {
        val state = GenerationUiState()
        assertEquals(ScanPhase.PROCESSING_IMAGE, state.currentPhase)
    }

    @Test
    fun `default state has zero progress`() {
        val state = GenerationUiState()
        assertEquals(0f, state.overallProgress)
    }

    @Test
    fun `default state is not cancelling`() {
        val state = GenerationUiState()
        assertFalse(state.isCancelling)
    }

    @Test
    fun `default state has no error`() {
        val state = GenerationUiState()
        assertNull(state.error)
    }

    @Test
    fun `default state has empty imageUri`() {
        val state = GenerationUiState()
        assertEquals("", state.imageUri)
    }

    @Test
    fun `copy with updated phase preserves other fields`() {
        val state = GenerationUiState(
            imageUri = "file:///test/image.jpg",
            overallProgress = 0.15f
        )
        val updated = state.copy(currentPhase = ScanPhase.ANALYZING_IMAGE)

        assertEquals(ScanPhase.ANALYZING_IMAGE, updated.currentPhase)
        assertEquals("file:///test/image.jpg", updated.imageUri)
        assertEquals(0.15f, updated.overallProgress)
    }

    @Test
    fun `copy with error preserves current phase`() {
        val state = GenerationUiState(currentPhase = ScanPhase.GENERATING_RULES)
        val updated = state.copy(error = "Network error")

        assertEquals("Network error", updated.error)
        assertEquals(ScanPhase.GENERATING_RULES, updated.currentPhase)
    }
}
