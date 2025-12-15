package com.rulebook.core.network.mapper

import com.rulebook.core.network.model.AnalyzeResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScanResultMapperTest {

    @Test
    fun `AnalyzeResponse toDomain maps all fields correctly`() {
        val response = AnalyzeResponse(
            gameTitle = "Catan",
            confidence = 0.95f,
            thumbnailUrl = "https://example.com/catan.jpg"
        )

        val scanResult = response.toDomain()

        assertEquals("Catan", scanResult.gameTitle)
        assertEquals(0.95f, scanResult.confidence, 0.001f)
        assertEquals("https://example.com/catan.jpg", scanResult.thumbnailUrl)
    }

    @Test
    fun `AnalyzeResponse toDomain handles null thumbnailUrl`() {
        val response = AnalyzeResponse(
            gameTitle = "Ticket to Ride",
            confidence = 0.87f,
            thumbnailUrl = null
        )

        val scanResult = response.toDomain()

        assertEquals("Ticket to Ride", scanResult.gameTitle)
        assertEquals(0.87f, scanResult.confidence, 0.001f)
        assertNull(scanResult.thumbnailUrl)
    }

    @Test
    fun `AnalyzeResponse toDomain handles zero confidence`() {
        val response = AnalyzeResponse(
            gameTitle = "Unknown",
            confidence = 0.0f,
            thumbnailUrl = null
        )

        val scanResult = response.toDomain()

        assertEquals(0.0f, scanResult.confidence, 0.001f)
    }

    @Test
    fun `AnalyzeResponse toDomain handles full confidence`() {
        val response = AnalyzeResponse(
            gameTitle = "Known Game",
            confidence = 1.0f,
            thumbnailUrl = "url"
        )

        val scanResult = response.toDomain()

        assertEquals(1.0f, scanResult.confidence, 0.001f)
    }

    @Test
    fun `list toDomainList converts all responses`() {
        val responses = listOf(
            AnalyzeResponse("Game 1", 0.9f, "url1"),
            AnalyzeResponse("Game 2", 0.8f, null),
            AnalyzeResponse("Game 3", 0.7f, "url3")
        )

        val scanResults = responses.toDomainList()

        assertEquals(3, scanResults.size)
        assertEquals("Game 1", scanResults[0].gameTitle)
        assertEquals(0.9f, scanResults[0].confidence, 0.001f)
        assertEquals("url1", scanResults[0].thumbnailUrl)
        assertEquals("Game 2", scanResults[1].gameTitle)
        assertNull(scanResults[1].thumbnailUrl)
        assertEquals("Game 3", scanResults[2].gameTitle)
    }

    @Test
    fun `empty list toDomainList returns empty list`() {
        val responses = emptyList<AnalyzeResponse>()

        val scanResults = responses.toDomainList()

        assertEquals(0, scanResults.size)
    }
}
