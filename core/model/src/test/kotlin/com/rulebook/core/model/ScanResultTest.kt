package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanResultTest {

    @Test
    fun `create ScanResult with all properties`() {
        val result = ScanResult(
            gameTitle = "Catan",
            confidence = 0.95f,
            thumbnailUrl = "https://example.com/catan.jpg"
        )

        assertEquals("Catan", result.gameTitle)
        assertEquals(0.95f, result.confidence, 0.001f)
        assertEquals("https://example.com/catan.jpg", result.thumbnailUrl)
    }

    @Test
    fun `create ScanResult with null thumbnailUrl`() {
        val result = ScanResult(
            gameTitle = "Ticket to Ride",
            confidence = 0.87f,
            thumbnailUrl = null
        )

        assertEquals("Ticket to Ride", result.gameTitle)
        assertEquals(0.87f, result.confidence, 0.001f)
        assertNull(result.thumbnailUrl)
    }

    @Test
    fun `ScanResult confidence can be zero`() {
        val result = ScanResult(
            gameTitle = "Unknown Game",
            confidence = 0.0f,
            thumbnailUrl = null
        )

        assertEquals(0.0f, result.confidence, 0.001f)
    }

    @Test
    fun `ScanResult confidence can be one`() {
        val result = ScanResult(
            gameTitle = "Known Game",
            confidence = 1.0f,
            thumbnailUrl = null
        )

        assertEquals(1.0f, result.confidence, 0.001f)
    }

    @Test
    fun `ScanResult data class equals works correctly`() {
        val result1 = ScanResult(
            gameTitle = "Catan",
            confidence = 0.95f,
            thumbnailUrl = "https://example.com/catan.jpg"
        )
        val result2 = ScanResult(
            gameTitle = "Catan",
            confidence = 0.95f,
            thumbnailUrl = "https://example.com/catan.jpg"
        )

        assertEquals(result1, result2)
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun `ScanResult data class copy works correctly`() {
        val original = ScanResult(
            gameTitle = "Catan",
            confidence = 0.95f,
            thumbnailUrl = "https://example.com/catan.jpg"
        )
        val updated = original.copy(confidence = 0.99f)

        assertEquals(original.gameTitle, updated.gameTitle)
        assertEquals(0.99f, updated.confidence, 0.001f)
        assertEquals(original.thumbnailUrl, updated.thumbnailUrl)
        assertNotEquals(original, updated)
    }

    @Test
    fun `ScanResult with different confidence are not equal`() {
        val result1 = ScanResult(
            gameTitle = "Catan",
            confidence = 0.95f,
            thumbnailUrl = null
        )
        val result2 = ScanResult(
            gameTitle = "Catan",
            confidence = 0.90f,
            thumbnailUrl = null
        )

        assertNotEquals(result1, result2)
    }

    @Test
    fun `ScanResult confidence comparison`() {
        val highConfidence = ScanResult(
            gameTitle = "Game",
            confidence = 0.9f,
            thumbnailUrl = null
        )
        val lowConfidence = ScanResult(
            gameTitle = "Game",
            confidence = 0.3f,
            thumbnailUrl = null
        )

        assertTrue(highConfidence.confidence > lowConfidence.confidence)
    }
}
