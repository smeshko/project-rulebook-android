package com.rulebook.core.database.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class RulesEntityTest {

    @Test
    fun `RulesEntity should correctly store all properties`() {
        val entity = RulesEntity(
            id = "rules-123",
            gameId = "game-123",
            overview = "Catan is a board game about trading and building.",
            setup = "Place the board and distribute resources.",
            firstRound = "Roll dice and collect resources.",
            advanced = "Trade with other players and build settlements.",
            rawJson = """{"gameId":"game-123","rules":{}}"""
        )

        assertEquals("rules-123", entity.id)
        assertEquals("game-123", entity.gameId)
        assertEquals("Catan is a board game about trading and building.", entity.overview)
        assertEquals("Place the board and distribute resources.", entity.setup)
        assertEquals("Roll dice and collect resources.", entity.firstRound)
        assertEquals("Trade with other players and build settlements.", entity.advanced)
        assertEquals("""{"gameId":"game-123","rules":{}}""", entity.rawJson)
    }

    @Test
    fun `RulesEntity should correctly reference gameId`() {
        val gameEntity = GameEntity(
            id = "game-456",
            title = "Monopoly",
            thumbnailUrl = null,
            createdAt = 1699500000000L,
            lastAccessedAt = 1699600000000L
        )

        val rulesEntity = RulesEntity(
            id = "rules-456",
            gameId = gameEntity.id,
            overview = "Buy properties and collect rent.",
            setup = "Each player starts with $1500.",
            firstRound = "Roll dice and move your token.",
            advanced = "Build houses and hotels.",
            rawJson = "{}"
        )

        assertEquals(gameEntity.id, rulesEntity.gameId)
    }

    @Test
    fun `RulesEntity copy should create new instance with updated values`() {
        val original = RulesEntity(
            id = "rules-789",
            gameId = "game-789",
            overview = "Original overview",
            setup = "Original setup",
            firstRound = "Original first round",
            advanced = "Original advanced",
            rawJson = "{}"
        )

        val updated = original.copy(overview = "Updated overview")

        assertEquals(original.id, updated.id)
        assertEquals(original.gameId, updated.gameId)
        assertEquals("Updated overview", updated.overview)
        assertEquals(original.setup, updated.setup)
    }

    @Test
    fun `RulesEntity equality should work correctly`() {
        val entity1 = RulesEntity(
            id = "rules-100",
            gameId = "game-100",
            overview = "Overview",
            setup = "Setup",
            firstRound = "First Round",
            advanced = "Advanced",
            rawJson = "{}"
        )

        val entity2 = RulesEntity(
            id = "rules-100",
            gameId = "game-100",
            overview = "Overview",
            setup = "Setup",
            firstRound = "First Round",
            advanced = "Advanced",
            rawJson = "{}"
        )

        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
    }
}
