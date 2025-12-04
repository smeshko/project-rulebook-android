package com.rulebook.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rulebook.core.database.entity.GameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM saved_games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM saved_games WHERE id = :id")
    suspend fun getGameById(id: String): GameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Query("DELETE FROM saved_games WHERE id = :id")
    suspend fun deleteGame(id: String)

    @Query("DELETE FROM saved_games")
    suspend fun deleteAllGames()
}
