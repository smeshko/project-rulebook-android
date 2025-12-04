package com.rulebook.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rulebook.core.database.entity.GameEntity

@Database(
    entities = [GameEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RulebookDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
