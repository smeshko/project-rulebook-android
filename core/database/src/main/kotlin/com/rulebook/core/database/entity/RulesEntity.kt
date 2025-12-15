package com.rulebook.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rules",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["game_id"])]
)
data class RulesEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "game_id")
    val gameId: String,
    val overview: String,
    val setup: String,
    @ColumnInfo(name = "first_round")
    val firstRound: String,
    val advanced: String,
    @ColumnInfo(name = "raw_json")
    val rawJson: String
)
