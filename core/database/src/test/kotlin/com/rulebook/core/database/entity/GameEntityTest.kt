package com.rulebook.core.database.entity

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameEntityTest {

    @Test
    fun `GameEntity should correctly store all properties`() {
        val entity = GameEntity(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1699500000000L,
            lastAccessedAt = 1699600000000L
        )

        assertEquals("game-123", entity.id)
        assertEquals("Catan", entity.title)
        assertEquals("https://example.com/catan.jpg", entity.thumbnailUrl)
        assertEquals(1699500000000L, entity.createdAt)
        assertEquals(1699600000000L, entity.lastAccessedAt)
    }

    @Test
    fun `GameEntity should allow null thumbnailUrl`() {
        val entity = GameEntity(
            id = "game-456",
            title = "Monopoly",
            thumbnailUrl = null,
            createdAt = 1699500000000L,
            lastAccessedAt = 1699600000000L
        )

        assertNull(entity.thumbnailUrl)
    }

    @Test
    fun `GameEntity copy should create new instance with updated values`() {
        val original = GameEntity(
            id = "game-789",
            title = "Chess",
            thumbnailUrl = "https://example.com/chess.jpg",
            createdAt = 1699500000000L,
            lastAccessedAt = 1699600000000L
        )

        val updated = original.copy(lastAccessedAt = 1699700000000L)

        assertEquals(original.id, updated.id)
        assertEquals(original.title, updated.title)
        assertEquals(1699700000000L, updated.lastAccessedAt)
    }

    @Test
    fun `GameEntity equality should work correctly`() {
        val entity1 = GameEntity(
            id = "game-100",
            title = "Risk",
            thumbnailUrl = null,
            createdAt = 1699500000000L,
            lastAccessedAt = 1699600000000L
        )

        val entity2 = GameEntity(
            id = "game-100",
            title = "Risk",
            thumbnailUrl = null,
            createdAt = 1699500000000L,
            lastAccessedAt = 1699600000000L
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }
}
