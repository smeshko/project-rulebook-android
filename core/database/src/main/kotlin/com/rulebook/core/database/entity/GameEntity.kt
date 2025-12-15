package com.rulebook.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    @ColumnInfo(name = "thumbnail_url")
    val thumbnailUrl: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long
)
