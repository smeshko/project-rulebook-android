package com.rulebook.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val imageUrl: String?,
    val rulesJson: String?,
    val confidence: Float,
    val createdAt: Long,
)
