package com.rulebook.core.database.mapper

import com.rulebook.core.database.entity.GameEntity
import com.rulebook.core.model.Game

/**
 * Extension function to convert a GameEntity to a Game domain model.
 */
fun GameEntity.toDomain(): Game = Game(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)

/**
 * Extension function to convert a Game domain model to a GameEntity.
 */
fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)

/**
 * Extension function to convert a list of GameEntities to a list of Game domain models.
 */
fun List<GameEntity>.toDomainList(): List<Game> = map { it.toDomain() }

/**
 * Extension function to convert a list of Game domain models to a list of GameEntities.
 */
fun List<Game>.toEntityList(): List<GameEntity> = map { it.toEntity() }
