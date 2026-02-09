package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.database.GameDao
import com.rulebook.core.database.RulesDao
import com.rulebook.core.database.entity.GameEntity
import com.rulebook.core.database.entity.RulesEntity
import com.rulebook.core.model.Game
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for GameRepositoryImpl.
 *
 * Tests repository behavior using fake implementations of GameDao and RulesDao.
 */
class GameRepositoryImplTest {

    private lateinit var fakeGameDao: FakeGameDao
    private lateinit var fakeRulesDao: FakeRulesDao
    private lateinit var repository: GameRepository

    @Before
    fun setup() {
        fakeGameDao = FakeGameDao()
        fakeRulesDao = FakeRulesDao()
        repository = GameRepositoryImpl(gameDao = fakeGameDao, rulesDao = fakeRulesDao)
    }

    // ==================== getGames Tests ====================

    @Test
    fun `getGames returns empty list when no games saved`() = runTest {
        val result = repository.getGames()

        assertTrue(result is Result.Success)
        assertEquals(emptyList<Game>(), (result as Result.Success).data)
    }

    @Test
    fun `getGames returns all saved games`() = runTest {
        val entity1 = GameEntity(
            id = "game-1",
            title = "Catan",
            thumbnailUrl = "http://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val entity2 = GameEntity(
            id = "game-2",
            title = "Ticket to Ride",
            thumbnailUrl = null,
            createdAt = 3000L,
            lastAccessedAt = 4000L
        )
        fakeGameDao.insertSync(entity1)
        fakeGameDao.insertSync(entity2)

        val result = repository.getGames()

        assertTrue(result is Result.Success)
        val games = (result as Result.Success).data
        assertEquals(2, games.size)
        assertEquals("Catan", games[0].title)
        assertEquals("Ticket to Ride", games[1].title)
    }

    // ==================== getGameById Tests ====================

    @Test
    fun `getGameById returns game when found`() = runTest {
        val entity = GameEntity(
            id = "game-1",
            title = "Catan",
            thumbnailUrl = "http://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        fakeGameDao.insertSync(entity)

        val result = repository.getGameById("game-1")

        assertTrue(result is Result.Success)
        val game = (result as Result.Success).data
        assertEquals("game-1", game.id)
        assertEquals("Catan", game.title)
    }

    @Test
    fun `getGameById returns error when game not found`() = runTest {
        val result = repository.getGameById("nonexistent")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("not found"))
    }

    // ==================== saveGame Tests ====================

    @Test
    fun `saveGame inserts game entity`() = runTest {
        val game = Game(
            id = "game-1",
            title = "Catan",
            thumbnailUrl = "http://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )

        val result = repository.saveGame(game)

        assertTrue(result is Result.Success)
        assertEquals(1, fakeGameDao.getGamesCount())

        val saved = fakeGameDao.getAll().first()[0]
        assertEquals("game-1", saved.id)
        assertEquals("Catan", saved.title)
    }

    // ==================== deleteGame Tests ====================

    @Test
    fun `deleteGame removes game entity`() = runTest {
        val entity = GameEntity(
            id = "game-1",
            title = "Catan",
            thumbnailUrl = "http://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        fakeGameDao.insertSync(entity)
        assertEquals(1, fakeGameDao.getGamesCount())

        val result = repository.deleteGame("game-1")

        assertTrue(result is Result.Success)
        assertEquals(0, fakeGameDao.getGamesCount())
    }

    @Test
    fun `deleteGame returns success even if game doesn't exist`() = runTest {
        val result = repository.deleteGame("nonexistent")
        assertTrue(result is Result.Success)
    }
}

/**
 * Fake implementation of GameDao for testing.
 */
class FakeGameDao : GameDao {
    private val games = mutableListOf<GameEntity>()
    private val gamesFlow = MutableStateFlow<List<GameEntity>>(emptyList())

    override suspend fun insert(game: GameEntity) {
        games.removeIf { it.id == game.id }
        games.add(game)
        gamesFlow.value = games.toList()
    }

    override suspend fun update(game: GameEntity) {
        val index = games.indexOfFirst { it.id == game.id }
        if (index != -1) {
            games[index] = game
            gamesFlow.value = games.toList()
        }
    }

    override suspend fun delete(game: GameEntity) {
        games.removeIf { it.id == game.id }
        gamesFlow.value = games.toList()
    }

    override fun getAll(): Flow<List<GameEntity>> = gamesFlow

    override fun getById(id: String): Flow<GameEntity?> {
        val game = games.find { it.id == id }
        return MutableStateFlow(game)
    }

    override fun getAllSortedByCreatedAtDesc(): Flow<List<GameEntity>> {
        return MutableStateFlow(games.sortedByDescending { it.createdAt })
    }

    override fun getAllSortedByLastAccessedDesc(): Flow<List<GameEntity>> {
        return MutableStateFlow(games.sortedByDescending { it.lastAccessedAt })
    }

    override fun getAllSortedByTitleAsc(): Flow<List<GameEntity>> {
        return MutableStateFlow(games.sortedBy { it.title })
    }

    // Test helper methods
    fun insertSync(game: GameEntity) {
        games.removeIf { it.id == game.id }
        games.add(game)
        gamesFlow.value = games.toList()
    }

    fun getGamesCount() = games.size
}

/**
 * Fake implementation of RulesDao for testing.
 */
class FakeRulesDao : RulesDao {
    private val rules = mutableListOf<RulesEntity>()

    override suspend fun insert(rules: RulesEntity) {
        this.rules.removeIf { it.id == rules.id }
        this.rules.add(rules)
    }

    override suspend fun update(rules: RulesEntity) {
        val index = this.rules.indexOfFirst { it.id == rules.id }
        if (index != -1) {
            this.rules[index] = rules
        }
    }

    override fun getByGameId(gameId: String): Flow<RulesEntity?> {
        val entity = rules.find { it.gameId == gameId }
        return MutableStateFlow(entity)
    }

    override suspend fun deleteByGameId(gameId: String) {
        rules.removeIf { it.gameId == gameId }
    }

    // Test helper methods
    fun getRulesCount() = rules.size
}
