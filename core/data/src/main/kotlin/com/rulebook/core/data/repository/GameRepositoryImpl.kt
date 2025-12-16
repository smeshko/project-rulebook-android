package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.Game

/**
 * Implementation of [GameRepository].
 *
 * Currently returns empty data as the full implementation will be done in Epic 3.
 * This stub implementation allows the Library screen to function with its empty state.
 */
class GameRepositoryImpl : GameRepository {

    override suspend fun getGames(): Result<List<Game>> {
        // TODO: Implement with actual data source in Epic 3
        return Result.Success(emptyList())
    }

    override suspend fun getGameById(id: String): Result<Game> {
        // TODO: Implement with actual data source in Epic 3
        return Result.Error("Game not found")
    }

    override suspend fun saveGame(game: Game): Result<Unit> {
        // TODO: Implement with actual data source in Epic 3
        return Result.Success(Unit)
    }

    override suspend fun deleteGame(id: String): Result<Unit> {
        // TODO: Implement with actual data source in Epic 3
        return Result.Success(Unit)
    }
}
