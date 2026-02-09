package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.common.safeCall
import com.rulebook.core.database.GameDao
import com.rulebook.core.database.RulesDao
import com.rulebook.core.database.entity.GameEntity
import com.rulebook.core.database.entity.RulesEntity
import com.rulebook.core.model.Game
import com.rulebook.core.model.Rules
import kotlinx.coroutines.flow.first
import java.util.UUID

/**
 * Implementation of [GameRepository] using Room DAOs.
 *
 * Maps between domain models (Game) and database entities (GameEntity).
 * Uses safeCall to wrap all database operations in Result.
 */
class GameRepositoryImpl(
    private val gameDao: GameDao,
    private val rulesDao: RulesDao,
    private val transactionRunner: suspend (suspend () -> Unit) -> Unit
) : GameRepository {

    override suspend fun getGames(): Result<List<Game>> = safeCall {
        gameDao.getAll().first().map { it.toDomain() }
    }

    override suspend fun getGameById(id: String): Result<Game> = safeCall {
        val entity = gameDao.getById(id).first()
            ?: throw IllegalArgumentException("Game with id $id not found")
        entity.toDomain()
    }

    override suspend fun saveGame(game: Game): Result<Unit> = safeCall {
        gameDao.insert(game.toEntity())
    }

    override suspend fun deleteGame(id: String): Result<Unit> = safeCall {
        val entity = gameDao.getById(id).first()
        if (entity != null) {
            gameDao.delete(entity)
        }
    }

    override suspend fun saveGameWithRules(
        game: Game,
        rules: Rules,
        rawJson: String
    ): Result<String> = safeCall {
        // Generate a single UUID for both game and rules
        val generatedId = UUID.randomUUID().toString()

        // Create entities with the generated ID
        val gameEntity = game.copy(id = generatedId).toEntity()
        val rulesEntity = rules.toEntity(generatedId, rawJson)

        // Insert both entities atomically — rollback both if either fails
        transactionRunner {
            gameDao.insert(gameEntity)
            rulesDao.insert(rulesEntity)
        }

        // Return the generated game ID
        generatedId
    }
}

// ==================== Mapper Extension Functions ====================

/**
 * Converts a Game domain model to a GameEntity for database storage.
 */
private fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)

/**
 * Converts a GameEntity from the database to a Game domain model.
 */
private fun GameEntity.toDomain(): Game = Game(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)

/**
 * Converts a Rules domain model to a RulesEntity for database storage.
 *
 * @param gameId The ID of the associated game (foreign key).
 * @param rawJson The raw JSON representation of the rules.
 */
private fun Rules.toEntity(gameId: String, rawJson: String): RulesEntity = RulesEntity(
    id = UUID.randomUUID().toString(),
    gameId = gameId,
    overview = overview.content,
    setup = setup.content,
    firstRound = firstRound.content,
    advanced = advanced.content,
    rawJson = rawJson
)
