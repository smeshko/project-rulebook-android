package com.rulebook.core.network.model

import com.rulebook.core.network.RulebookApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for network request/response models and their serialization.
 */
class NetworkModelsTest {

    private val json = RulebookApiClient.json

    @Test
    fun `AnalyzeRequest serializes with snake_case field names`() {
        val request = AnalyzeRequest(
            imageData = "base64data",
            imageFormat = "png",
        )

        val serialized = json.encodeToString(AnalyzeRequest.serializer(), request)

        assertEquals("""{"image_data":"base64data","image_format":"png"}""", serialized)
    }

    @Test
    fun `AnalyzeRequest uses default imageFormat when not specified`() {
        val request = AnalyzeRequest(imageData = "base64data")

        assertEquals("jpeg", request.imageFormat)
    }

    @Test
    fun `AnalyzeResponse deserializes from snake_case JSON`() {
        val jsonString = """{"game_title":"Catan","confidence":0.95,"thumbnail_url":"https://example.com/catan.jpg"}"""

        val response = json.decodeFromString(AnalyzeResponse.serializer(), jsonString)

        assertEquals("Catan", response.gameTitle)
        assertEquals(0.95f, response.confidence, 0.001f)
        assertEquals("https://example.com/catan.jpg", response.thumbnailUrl)
    }

    @Test
    fun `AnalyzeResponse handles null thumbnailUrl`() {
        val jsonString = """{"game_title":"Catan","confidence":0.95}"""

        val response = json.decodeFromString(AnalyzeResponse.serializer(), jsonString)

        assertNull(response.thumbnailUrl)
    }

    @Test
    fun `GenerateRequest serializes with snake_case field names`() {
        val request = GenerateRequest(
            gameTitle = "Catan",
            gameId = "game-123",
        )

        val serialized = json.encodeToString(GenerateRequest.serializer(), request)

        assertEquals("""{"game_title":"Catan","game_id":"game-123","thumbnail_url":null}""", serialized)
    }

    @Test
    fun `GenerateRequest handles null gameId and thumbnailUrl`() {
        val request = GenerateRequest(gameTitle = "Catan")

        val serialized = json.encodeToString(GenerateRequest.serializer(), request)

        assertEquals("""{"game_title":"Catan","game_id":null,"thumbnail_url":null}""", serialized)
    }

    @Test
    fun `GenerateRequest serializes with thumbnailUrl when provided`() {
        val request = GenerateRequest(
            gameTitle = "Catan",
            gameId = "game-123",
            thumbnailUrl = "https://example.com/catan.jpg"
        )

        val serialized = json.encodeToString(GenerateRequest.serializer(), request)

        assertEquals("""{"game_title":"Catan","game_id":"game-123","thumbnail_url":"https://example.com/catan.jpg"}""", serialized)
    }

    @Test
    fun `GenerateResponse deserializes with rules sections`() {
        val jsonString = """
            {
                "game_title": "Catan",
                "rules_summary": "Trade and build to victory",
                "rules_sections": [
                    {"title": "Setup", "content": "Place hexes..."},
                    {"title": "Turns", "content": "Roll dice..."}
                ]
            }
        """.trimIndent()

        val response = json.decodeFromString(GenerateResponse.serializer(), jsonString)

        assertEquals("Catan", response.gameTitle)
        assertEquals("Trade and build to victory", response.rulesSummary)
        assertEquals(2, response.rulesSections.size)
        assertEquals("Setup", response.rulesSections[0].title)
        assertEquals("Place hexes...", response.rulesSections[0].content)
    }

    @Test
    fun `GenerateResponse handles empty rules sections`() {
        val jsonString = """{"game_title":"Catan","rules_summary":"Summary"}"""

        val response = json.decodeFromString(GenerateResponse.serializer(), jsonString)

        assertEquals(emptyList<RulesSection>(), response.rulesSections)
    }
}
