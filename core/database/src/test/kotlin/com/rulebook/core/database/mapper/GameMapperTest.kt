package com.rulebook.core.database.mapper

import com.rulebook.core.database.entity.GameEntity
import com.rulebook.core.model.Game
import org.junit.Assert.assertEquals
import org.junit.Test

class GameMapperTest {

    @Test
    fun `GameEntity toDomain maps all fields correctly`() {
        val entity = GameEntity(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals("game-123", domain.id)
        assertEquals("Catan", domain.title)
        assertEquals("https://example.com/catan.jpg", domain.thumbnailUrl)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.lastAccessedAt)
    }

    @Test
    fun `GameEntity toDomain handles null thumbnailUrl`() {
        val entity = GameEntity(
            id = "game-456",
            title = "Ticket to Ride",
            thumbnailUrl = null,
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals(null, domain.thumbnailUrl)
    }

    @Test
    fun `Game toEntity maps all fields correctly`() {
        val domain = Game(
            id = "game-123",
            title = "Catan",
            thumbnailUrl = "https://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        val entity = domain.toEntity()

        assertEquals("game-123", entity.id)
        assertEquals("Catan", entity.title)
        assertEquals("https://example.com/catan.jpg", entity.thumbnailUrl)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.lastAccessedAt)
    }

    @Test
    fun `Game toEntity handles null thumbnailUrl`() {
        val domain = Game(
            id = "game-456",
            title = "Ticket to Ride",
            thumbnailUrl = null,
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        val entity = domain.toEntity()

        assertEquals(null, entity.thumbnailUrl)
    }

    @Test
    fun `round trip conversion preserves all data`() {
        val original = Game(
            id = "game-789",
            title = "Pandemic",
            thumbnailUrl = "https://example.com/pandemic.jpg",
            createdAt = 3000L,
            lastAccessedAt = 4000L
        )

        val roundTrip = original.toEntity().toDomain()

        assertEquals(original, roundTrip)
    }

    @Test
    fun `list toDomainList converts all entities`() {
        val entities = listOf(
            GameEntity("1", "Game 1", null, 1000L, 2000L),
            GameEntity("2", "Game 2", "url", 3000L, 4000L)
        )

        val domains = entities.toDomainList()

        assertEquals(2, domains.size)
        assertEquals("Game 1", domains[0].title)
        assertEquals("Game 2", domains[1].title)
    }

    @Test
    fun `list toEntityList converts all games`() {
        val games = listOf(
            Game("1", "Game 1", null, 1000L, 2000L),
            Game("2", "Game 2", "url", 3000L, 4000L)
        )

        val entities = games.toEntityList()

        assertEquals(2, entities.size)
        assertEquals("Game 1", entities[0].title)
        assertEquals("Game 2", entities[1].title)
    }
}
