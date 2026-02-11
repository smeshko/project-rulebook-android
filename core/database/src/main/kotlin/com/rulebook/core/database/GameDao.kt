package com.rulebook.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rulebook.core.database.entity.GameEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [GameEntity] operations.
 *
 * All query methods return [Flow] for reactive data observation.
 * Modification methods are suspend functions for coroutine integration.
 */
@Dao
interface GameDao {
    /**
     * Inserts a game into the database. If a game with the same ID already exists,
     * it will be replaced (upsert behavior).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity)

    /**
     * Updates an existing game entry. Use this for partial updates when you want
     * explicit update semantics rather than upsert behavior.
     */
    @Update
    suspend fun update(game: GameEntity)

    /**
     * Deletes a game from the database.
     * Associated rules will be automatically deleted via CASCADE.
     */
    @Delete
    suspend fun delete(game: GameEntity)

    /**
     * Returns a Flow of all saved games (unsorted).
     * Emits a new list whenever the underlying data changes.
     */
    @Query("SELECT * FROM saved_games")
    fun getAll(): Flow<List<GameEntity>>

    /**
     * Returns a Flow of the game with the specified ID, or null if not found.
     * Emits a new value whenever the game data changes.
     */
    @Query("SELECT * FROM saved_games WHERE id = :id")
    fun getById(id: String): Flow<GameEntity?>

    /**
     * Returns all saved games sorted by creation date (newest first).
     */
    @Query("SELECT * FROM saved_games ORDER BY created_at DESC")
    fun getAllSortedByCreatedAtDesc(): Flow<List<GameEntity>>

    /**
     * Returns all saved games sorted by last access time (most recent first).
     * Useful for "Recently Played" lists.
     */
    @Query("SELECT * FROM saved_games ORDER BY last_accessed_at DESC")
    fun getAllSortedByLastAccessedDesc(): Flow<List<GameEntity>>

    /**
     * Returns all saved games sorted alphabetically by title.
     */
    @Query("SELECT * FROM saved_games ORDER BY title ASC")
    fun getAllSortedByTitleAsc(): Flow<List<GameEntity>>

    /**
     * Updates the last accessed timestamp for a specific game.
     * This is a targeted update operation that only modifies the lastAccessedAt field.
     */
    @Query("UPDATE saved_games SET last_accessed_at = :timestamp WHERE id = :id")
    suspend fun updateLastAccessedAt(id: String, timestamp: Long)
}
