package com.rulebook.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rulebook.core.database.entity.GameEntity
import com.rulebook.core.database.entity.RulesEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RulesDaoTest {

    private lateinit var database: RulebookDatabase
    private lateinit var gameDao: GameDao
    private lateinit var rulesDao: RulesDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RulebookDatabase::class.java
        ).allowMainThreadQueries().build()
        gameDao = database.gameDao()
        rulesDao = database.rulesDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_and_getByGameId_returnsRules() = runTest {
        val game = createTestGame("game-1")
        gameDao.insert(game)

        val rules = createTestRules("rules-1", "game-1")
        rulesDao.insert(rules)

        val result = rulesDao.getByGameId("game-1").first()

        assertEquals(rules, result)
    }

    @Test
    fun insert_withSameId_replacesExisting() = runTest {
        val game = createTestGame("game-1")
        gameDao.insert(game)

        val original = createTestRules("rules-1", "game-1", overview = "Original")
        val updated = createTestRules("rules-1", "game-1", overview = "Updated")

        rulesDao.insert(original)
        rulesDao.insert(updated)

        val result = rulesDao.getByGameId("game-1").first()
        assertEquals("Updated", result?.overview)
    }

    @Test
    fun update_modifiesExistingRules() = runTest {
        val game = createTestGame("game-1")
        gameDao.insert(game)

        val rules = createTestRules("rules-1", "game-1", overview = "Original")
        rulesDao.insert(rules)

        val updatedRules = rules.copy(overview = "Updated Overview")
        rulesDao.update(updatedRules)

        val result = rulesDao.getByGameId("game-1").first()
        assertEquals("Updated Overview", result?.overview)
    }

    @Test
    fun deleteByGameId_removesRules() = runTest {
        val game = createTestGame("game-1")
        gameDao.insert(game)

        val rules = createTestRules("rules-1", "game-1")
        rulesDao.insert(rules)
        rulesDao.deleteByGameId("game-1")

        val result = rulesDao.getByGameId("game-1").first()
        assertNull(result)
    }

    @Test
    fun cascade_delete_removesRulesWhenGameDeleted() = runTest {
        // This is the critical test for foreign key CASCADE behavior
        val game = createTestGame("game-1")
        gameDao.insert(game)

        val rules = createTestRules("rules-1", "game-1")
        rulesDao.insert(rules)

        // Verify rules exist
        val beforeDelete = rulesDao.getByGameId("game-1").first()
        assertEquals(rules, beforeDelete)

        // Delete the game - rules should be CASCADE deleted
        gameDao.delete(game)

        // Verify rules were automatically deleted
        val afterDelete = rulesDao.getByGameId("game-1").first()
        assertNull(afterDelete)
    }

    @Test
    fun getByGameId_withNonExistentGame_returnsNull() = runTest {
        val result = rulesDao.getByGameId("non-existent").first()
        assertNull(result)
    }

    @Test
    fun multipleGames_haveIndependentRules() = runTest {
        val game1 = createTestGame("game-1")
        val game2 = createTestGame("game-2")
        gameDao.insert(game1)
        gameDao.insert(game2)

        val rules1 = createTestRules("rules-1", "game-1", overview = "Game 1 Rules")
        val rules2 = createTestRules("rules-2", "game-2", overview = "Game 2 Rules")
        rulesDao.insert(rules1)
        rulesDao.insert(rules2)

        val result1 = rulesDao.getByGameId("game-1").first()
        val result2 = rulesDao.getByGameId("game-2").first()

        assertEquals("Game 1 Rules", result1?.overview)
        assertEquals("Game 2 Rules", result2?.overview)
    }

    private fun createTestGame(id: String) = GameEntity(
        id = id,
        title = "Test Game",
        thumbnailUrl = null,
        createdAt = System.currentTimeMillis(),
        lastAccessedAt = System.currentTimeMillis()
    )

    private fun createTestRules(
        id: String,
        gameId: String,
        overview: String = "Overview text",
        setup: String = "Setup text",
        firstRound: String = "First round text",
        advanced: String = "Advanced text",
        rawJson: String = "{}"
    ) = RulesEntity(
        id = id,
        gameId = gameId,
        overview = overview,
        setup = setup,
        firstRound = firstRound,
        advanced = advanced,
        rawJson = rawJson
    )
}
