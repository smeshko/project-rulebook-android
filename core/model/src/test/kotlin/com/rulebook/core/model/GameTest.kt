package com.rulebook.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameTest {

    @Test
    fun `create Game with all properties`() {
        val game = Game(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        assertEquals("game-123", game.id)
        assertEquals("Catan", game.title)
        assertEquals("https://example.com/catan.jpg", game.thumbnailUrl)
        assertEquals(1000L, game.createdAt)
        assertEquals(2000L, game.lastAccessedAt)
    }

    @Test
    fun `create Game with null thumbnailUrl`() {
        val game = Game(
            id = "game-456",
            title = "Ticket to Ride",
            thumbnailUrl = null,
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        assertNull(game.thumbnailUrl)
    }

    @Test
    fun `Game data class equals works correctly`() {
        val game1 = Game(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val game2 = Game(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        assertEquals(game1, game2)
        assertEquals(game1.hashCode(), game2.hashCode())
    }

    @Test
    fun `Game data class copy works correctly`() {
        val original = Game(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val updated = original.copy(lastAccessedAt = 3000L)

        assertEquals(original.id, updated.id)
        assertEquals(original.title, updated.title)
        assertEquals(original.thumbnailUrl, updated.thumbnailUrl)
        assertEquals(original.createdAt, updated.createdAt)
        assertEquals(3000L, updated.lastAccessedAt)
        assertNotEquals(original, updated)
    }
}
