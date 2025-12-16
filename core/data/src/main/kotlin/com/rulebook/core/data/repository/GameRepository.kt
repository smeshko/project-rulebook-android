package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.Game

/**
 * Repository interface for game data operations.
 *
 * Provides access to saved games in the user's library.
 * Implementations handle data source coordination and caching.
 */
interface GameRepository {

    /**
     * Retrieves all saved games from the user's library.
     *
     * @return Result containing the list of games or an error.
     */
    suspend fun getGames(): Result<List<Game>>

    /**
     * Retrieves a specific game by its ID.
     *
     * @param id The unique identifier of the game.
     * @return Result containing the game or an error if not found.
     */
    suspend fun getGameById(id: String): Result<Game>

    /**
     * Saves a new game to the library.
     *
     * @param game The game to save.
     * @return Result indicating success or failure.
     */
    suspend fun saveGame(game: Game): Result<Unit>

    /**
     * Deletes a game from the library.
     *
     * @param id The unique identifier of the game to delete.
     * @return Result indicating success or failure.
     */
    suspend fun deleteGame(id: String): Result<Unit>
}
