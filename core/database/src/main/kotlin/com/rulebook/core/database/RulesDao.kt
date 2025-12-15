package com.rulebook.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rulebook.core.database.entity.RulesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [RulesEntity] operations.
 */
@Dao
interface RulesDao {
    /**
     * Inserts rules into the database. If rules with the same ID already exist,
     * they will be replaced (upsert behavior).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rules: RulesEntity)

    /**
     * Updates an existing rules entry. Use this for partial updates when you want
     * explicit update semantics rather than upsert behavior.
     */
    @Update
    suspend fun update(rules: RulesEntity)

    /**
     * Returns a Flow of rules for the specified game ID.
     * Emits null if no rules exist for the game.
     * Automatically emits new values when the underlying data changes.
     */
    @Query("SELECT * FROM rules WHERE game_id = :gameId")
    fun getByGameId(gameId: String): Flow<RulesEntity?>

    /**
     * Deletes all rules associated with the specified game ID.
     * Note: This is also handled automatically via CASCADE when the game is deleted.
     */
    @Query("DELETE FROM rules WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
