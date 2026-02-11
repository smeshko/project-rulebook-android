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
        repository = GameRepositoryImpl(
            gameDao = fakeGameDao,
            rulesDao = fakeRulesDao,
            transactionRunner = { block -> block() }
        )
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

    // ==================== getRulesForGame Tests ====================

    @Test
    fun `getRulesForGame returns rules when found`() = runTest {
        val rulesEntity = RulesEntity(
            id = "rules-1",
            gameId = "game-1",
            overview = """{"title":"Overview","content":"Test overview","items":null}""",
            setup = """{"title":"Setup","content":"Test setup","items":null}""",
            firstRound = """{"title":"First Round","content":"Test first round","items":null}""",
            advanced = """{"title":"Advanced","content":"Test advanced","items":null}""",
            rawJson = """{"overview":"test"}"""
        )
        fakeRulesDao.insertSync(rulesEntity)

        val result = repository.getRulesForGame("game-1")

        assertTrue(result is Result.Success)
        val rules = (result as Result.Success).data
        assertEquals("game-1", rules.gameId)
        assertEquals("Test overview", rules.overview.content)
        assertEquals("Test setup", rules.setup.content)
    }

    @Test
    fun `getRulesForGame returns error when rules not found`() = runTest {
        val result = repository.getRulesForGame("nonexistent")

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).message.contains("not found"))
    }

    // ==================== saveGameWithRules Tests ====================

    @Test
    fun `saveGameWithRules creates both game and rules entities`() = runTest {
        val game = com.rulebook.core.model.Game(
            id = "", // will be generated
            title = "Catan",
            thumbnailUrl = "http://example.com/catan.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val rules = com.rulebook.core.model.Rules(
            gameId = "", // will be set to generated game ID
            overview = com.rulebook.core.model.RuleSection(
                title = "Overview",
                content = "Test overview",
                items = null
            ),
            setup = com.rulebook.core.model.RuleSection(
                title = "Setup",
                content = "Test setup",
                items = null
            ),
            firstRound = com.rulebook.core.model.RuleSection(
                title = "First Round",
                content = "Test first round",
                items = null
            ),
            advanced = com.rulebook.core.model.RuleSection(
                title = "Advanced",
                content = "Test advanced",
                items = null
            )
        )
        val rawJson = """{"overview":"test"}"""

        val result = repository.saveGameWithRules(game, rules, rawJson)

        assertTrue(result is Result.Success)
        val gameId = (result as Result.Success).data

        // Verify game was created
        assertEquals(1, fakeGameDao.getGamesCount())
        val savedGame = fakeGameDao.getAll().first()[0]
        assertEquals(gameId, savedGame.id)
        assertEquals("Catan", savedGame.title)

        // Verify rules were created
        assertEquals(1, fakeRulesDao.getRulesCount())
        val savedRules = fakeRulesDao.getByGameId(gameId).first()!!
        assertEquals(gameId, savedRules.gameId)
        // Rules are stored as JSON with title, content, and items preserved
        assertTrue(savedRules.overview.contains("\"title\""))
        assertTrue(savedRules.overview.contains("\"content\""))
        assertTrue(savedRules.overview.contains("Test overview"))
        assertEquals(rawJson, savedRules.rawJson)
    }

    @Test
    fun `saveGameWithRules returns generated UUID as game ID`() = runTest {
        val game = com.rulebook.core.model.Game(
            id = "",
            title = "Catan",
            thumbnailUrl = null,
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val rules = com.rulebook.core.model.Rules(
            gameId = "",
            overview = com.rulebook.core.model.RuleSection("O", "o", null),
            setup = com.rulebook.core.model.RuleSection("S", "s", null),
            firstRound = com.rulebook.core.model.RuleSection("F", "f", null),
            advanced = com.rulebook.core.model.RuleSection("A", "a", null)
        )

        val result = repository.saveGameWithRules(game, rules, "{}")

        assertTrue(result is Result.Success)
        val gameId = (result as Result.Success).data

        // UUID should be non-empty and follow UUID format (basic check)
        assertTrue(gameId.isNotEmpty())
        assertTrue(gameId.contains("-"))
    }

    @Test
    fun `saveGameWithRules uses same ID for game and rules foreign key`() = runTest {
        val game = com.rulebook.core.model.Game(
            id = "",
            title = "Test Game",
            thumbnailUrl = null,
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val rules = com.rulebook.core.model.Rules(
            gameId = "",
            overview = com.rulebook.core.model.RuleSection("O", "o", null),
            setup = com.rulebook.core.model.RuleSection("S", "s", null),
            firstRound = com.rulebook.core.model.RuleSection("F", "f", null),
            advanced = com.rulebook.core.model.RuleSection("A", "a", null)
        )

        val result = repository.saveGameWithRules(game, rules, "{}")

        assertTrue(result is Result.Success)
        val gameId = (result as Result.Success).data

        val savedGame = fakeGameDao.getById(gameId).first()!!
        val savedRules = fakeRulesDao.getByGameId(gameId).first()!!

        assertEquals(savedGame.id, savedRules.gameId)
    }

    @Test
    fun `saveGameWithRules returns error when rules insert fails`() = runTest {
        fakeRulesDao.shouldThrowOnInsert = true
        val game = com.rulebook.core.model.Game(
            id = "",
            title = "Test Game",
            thumbnailUrl = null,
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        val rules = com.rulebook.core.model.Rules(
            gameId = "",
            overview = com.rulebook.core.model.RuleSection("O", "o", null),
            setup = com.rulebook.core.model.RuleSection("S", "s", null),
            firstRound = com.rulebook.core.model.RuleSection("F", "f", null),
            advanced = com.rulebook.core.model.RuleSection("A", "a", null)
        )

        val result = repository.saveGameWithRules(game, rules, "{}")

        assertTrue(result is Result.Error)
    }

    // ==================== Offline Scenario Tests ====================

    @Test
    fun `getGameById returns data from DAO without any network interaction`() = runTest {
        val entity = GameEntity(
            id = "offline-game-1",
            title = "Offline Game",
            thumbnailUrl = "http://example.com/offline.jpg",
            createdAt = 1000L,
            lastAccessedAt = 2000L
        )
        fakeGameDao.insertSync(entity)

        // Verify read from DAO succeeds (simulating offline scenario)
        val result = repository.getGameById("offline-game-1")

        assertTrue(result is Result.Success)
        val game = (result as Result.Success).data
        assertEquals("offline-game-1", game.id)
        assertEquals("Offline Game", game.title)
        assertEquals("http://example.com/offline.jpg", game.thumbnailUrl)
    }

    @Test
    fun `getRulesForGame returns data from DAO without any network interaction`() = runTest {
        val rulesEntity = RulesEntity(
            id = "offline-rules-1",
            gameId = "offline-game-1",
            overview = """{"title":"Offline Overview","content":"Offline content","items":null}""",
            setup = """{"title":"Offline Setup","content":"Offline setup content","items":null}""",
            firstRound = """{"title":"Offline First Round","content":"Offline first round content","items":null}""",
            advanced = """{"title":"Offline Advanced","content":"Offline advanced content","items":null}""",
            rawJson = """{"overview":"offline test"}"""
        )
        fakeRulesDao.insertSync(rulesEntity)

        // Verify read from DAO succeeds (simulating offline scenario)
        val result = repository.getRulesForGame("offline-game-1")

        assertTrue(result is Result.Success)
        val rules = (result as Result.Success).data
        assertEquals("offline-game-1", rules.gameId)
        assertEquals("Offline content", rules.overview.content)
        assertEquals("Offline setup content", rules.setup.content)
        assertEquals("Offline first round content", rules.firstRound.content)
        assertEquals("Offline advanced content", rules.advanced.content)
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

    override suspend fun updateLastAccessedAt(id: String, timestamp: Long) {
        val index = games.indexOfFirst { it.id == id }
        if (index != -1) {
            games[index] = games[index].copy(lastAccessedAt = timestamp)
            gamesFlow.value = games.toList()
        }
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
    var shouldThrowOnInsert = false

    override suspend fun insert(rules: RulesEntity) {
        if (shouldThrowOnInsert) throw RuntimeException("Simulated insert failure")
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
    fun insertSync(rules: RulesEntity) {
        this.rules.removeIf { it.id == rules.id }
        this.rules.add(rules)
    }

    fun getRulesCount() = rules.size
}
