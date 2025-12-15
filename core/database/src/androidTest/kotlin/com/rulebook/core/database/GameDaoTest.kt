package com.rulebook.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rulebook.core.database.entity.GameEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDaoTest {

    private lateinit var database: RulebookDatabase
    private lateinit var gameDao: GameDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RulebookDatabase::class.java
        ).allowMainThreadQueries().build()
        gameDao = database.gameDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insert_and_getById_returnsGame() = runTest {
        val game = createTestGame("game-1", "Catan")
        gameDao.insert(game)

        val result = gameDao.getById("game-1").first()

        assertEquals(game, result)
    }

    @Test
    fun insert_withSameId_replacesExisting() = runTest {
        val original = createTestGame("game-1", "Original Title")
        val updated = createTestGame("game-1", "Updated Title")

        gameDao.insert(original)
        gameDao.insert(updated)

        val result = gameDao.getById("game-1").first()
        assertEquals("Updated Title", result?.title)
    }

    @Test
    fun update_modifiesExistingGame() = runTest {
        val game = createTestGame("game-1", "Catan")
        gameDao.insert(game)

        val updatedGame = game.copy(title = "Catan: Seafarers")
        gameDao.update(updatedGame)

        val result = gameDao.getById("game-1").first()
        assertEquals("Catan: Seafarers", result?.title)
    }

    @Test
    fun delete_removesGame() = runTest {
        val game = createTestGame("game-1", "Catan")
        gameDao.insert(game)
        gameDao.delete(game)

        val result = gameDao.getById("game-1").first()
        assertNull(result)
    }

    @Test
    fun getAll_returnsAllGames() = runTest {
        val game1 = createTestGame("game-1", "Catan")
        val game2 = createTestGame("game-2", "Monopoly")
        gameDao.insert(game1)
        gameDao.insert(game2)

        val result = gameDao.getAll().first()

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "game-1" })
        assertTrue(result.any { it.id == "game-2" })
    }

    @Test
    fun getAllSortedByTitleAsc_returnsSortedByTitle() = runTest {
        gameDao.insert(createTestGame("game-1", "Zelda"))
        gameDao.insert(createTestGame("game-2", "Avalon"))
        gameDao.insert(createTestGame("game-3", "Monopoly"))

        val result = gameDao.getAllSortedByTitleAsc().first()

        assertEquals("Avalon", result[0].title)
        assertEquals("Monopoly", result[1].title)
        assertEquals("Zelda", result[2].title)
    }

    @Test
    fun getAllSortedByCreatedAtDesc_returnsSortedByCreatedAt() = runTest {
        gameDao.insert(createTestGame("game-1", "Old Game", createdAt = 1000L))
        gameDao.insert(createTestGame("game-2", "New Game", createdAt = 3000L))
        gameDao.insert(createTestGame("game-3", "Mid Game", createdAt = 2000L))

        val result = gameDao.getAllSortedByCreatedAtDesc().first()

        assertEquals("New Game", result[0].title)
        assertEquals("Mid Game", result[1].title)
        assertEquals("Old Game", result[2].title)
    }

    @Test
    fun getAllSortedByLastAccessedDesc_returnsSortedByLastAccessed() = runTest {
        gameDao.insert(createTestGame("game-1", "Rarely Played", lastAccessedAt = 1000L))
        gameDao.insert(createTestGame("game-2", "Just Played", lastAccessedAt = 3000L))
        gameDao.insert(createTestGame("game-3", "Sometimes", lastAccessedAt = 2000L))

        val result = gameDao.getAllSortedByLastAccessedDesc().first()

        assertEquals("Just Played", result[0].title)
        assertEquals("Sometimes", result[1].title)
        assertEquals("Rarely Played", result[2].title)
    }

    @Test
    fun getById_withNonExistentId_returnsNull() = runTest {
        val result = gameDao.getById("non-existent").first()
        assertNull(result)
    }

    private fun createTestGame(
        id: String,
        title: String,
        thumbnailUrl: String? = "https://example.com/thumb.jpg",
        createdAt: Long = System.currentTimeMillis(),
        lastAccessedAt: Long = System.currentTimeMillis()
    ) = GameEntity(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        createdAt = createdAt,
        lastAccessedAt = lastAccessedAt
    )
}
