package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.Game
import com.rulebook.core.model.Rules

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

    /**
     * Retrieves the rules for a specific game by its ID.
     *
     * @param gameId The unique identifier of the game.
     * @return Result containing the rules or an error if not found.
     */
    suspend fun getRulesForGame(gameId: String): Result<Rules>

    /**
     * Saves a game and its associated rules in a single atomic transaction.
     *
     * Generates a new UUID for both the game and rules, ensuring referential integrity.
     * If either insert fails, the entire transaction is rolled back.
     *
     * @param game The game to save (id will be overwritten with generated UUID).
     * @param rules The rules to save (gameId will be overwritten with generated UUID).
     * @param rawJson The raw JSON representation of the rules for future re-parsing.
     * @return Result containing the generated game ID on success, or an error.
     */
    suspend fun saveGameWithRules(game: Game, rules: Rules, rawJson: String): Result<String>
}
