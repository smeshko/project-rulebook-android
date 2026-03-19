package com.rulebook.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rulebook.core.database.entity.GameEntity
import com.rulebook.core.database.entity.RulesEntity

/**
 * Main Room database for the Rulebook app.
 *
 * Contains tables for saved games and their associated rules.
 * JSON data is stored as raw String (no TypeConverters needed).
 * Timestamps are stored as Long (milliseconds since epoch).
 */
@Database(
    entities = [GameEntity::class, RulesEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RulebookDatabase : RoomDatabase(), ClearableDatabase {
    abstract fun gameDao(): GameDao
    abstract fun rulesDao(): RulesDao
}
