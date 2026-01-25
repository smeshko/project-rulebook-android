---
title: Repository Template
description: Template for creating repository interface and implementation
author: Ivo
date: 2026-01-25
---

# Repository Template

## When to Use

- Creating data access abstraction for a domain entity
- Need to coordinate multiple data sources (database, network, preferences)
- Want consistent error handling with Result type
- Require testable data layer with interface abstraction

## Quick Reference

| Aspect | Value |
|--------|-------|
| Interface Location | `core/data/src/main/kotlin/.../repository/{Entity}Repository.kt` |
| Implementation Location | `core/data/src/main/kotlin/.../repository/{Entity}RepositoryImpl.kt` |
| Pattern | Interface + Implementation |
| Naming | `{Entity}Repository.kt`, `{Entity}RepositoryImpl.kt` |

## Code Template - Interface

```kotlin
package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.{Entity}
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for {entity} data operations.
 *
 * Provides access to {description of data}.
 * Implementations handle data source coordination and caching.
 */
interface {Entity}Repository {

    /**
     * Retrieves all {entities}.
     *
     * @return Result containing the list of {entities} or an error.
     */
    suspend fun get{Entities}(): Result<List<{Entity}>>

    /**
     * Retrieves a specific {entity} by its ID.
     *
     * @param id The unique identifier of the {entity}.
     * @return Result containing the {entity} or an error if not found.
     */
    suspend fun get{Entity}ById(id: String): Result<{Entity}>

    /**
     * Saves a new {entity}.
     *
     * @param {entity} The {entity} to save.
     * @return Result indicating success or failure.
     */
    suspend fun save{Entity}({entity}: {Entity}): Result<Unit>

    /**
     * Updates an existing {entity}.
     *
     * @param {entity} The {entity} with updated data.
     * @return Result indicating success or failure.
     */
    suspend fun update{Entity}({entity}: {Entity}): Result<Unit>

    /**
     * Deletes a {entity}.
     *
     * @param id The unique identifier of the {entity} to delete.
     * @return Result indicating success or failure.
     */
    suspend fun delete{Entity}(id: String): Result<Unit>

    /**
     * Observes all {entities} reactively.
     *
     * @return Flow emitting the list of {entities} on changes.
     */
    fun observe{Entities}(): Flow<List<{Entity}>>
}
```

## Code Template - Implementation

```kotlin
package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.database.{Entity}Dao
import com.rulebook.core.database.entity.{Entity}Entity
import com.rulebook.core.model.{Entity}
import com.rulebook.core.network.{Entity}ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of [{Entity}Repository].
 *
 * Coordinates between local database and remote API.
 * Local database is the source of truth for offline-first behavior.
 *
 * @param {entity}Dao DAO for local database operations.
 * @param {entity}ApiService API service for remote operations (optional).
 */
class {Entity}RepositoryImpl(
    private val {entity}Dao: {Entity}Dao,
    // private val {entity}ApiService: {Entity}ApiService  // Add when needed
) : {Entity}Repository {

    override suspend fun get{Entities}(): Result<List<{Entity}>> {
        return try {
            val entities = {entity}Dao.getAll()
            Result.Success(entities.map { it.toDomainModel() })
        } catch (e: Exception) {
            Result.Error("Failed to load {entities}", e)
        }
    }

    override suspend fun get{Entity}ById(id: String): Result<{Entity}> {
        return try {
            val entity = {entity}Dao.getById(id)
            if (entity != null) {
                Result.Success(entity.toDomainModel())
            } else {
                Result.Error("{Entity} not found")
            }
        } catch (e: Exception) {
            Result.Error("Failed to load {entity}", e)
        }
    }

    override suspend fun save{Entity}({entity}: {Entity}): Result<Unit> {
        return try {
            {entity}Dao.insert({entity}.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to save {entity}", e)
        }
    }

    override suspend fun update{Entity}({entity}: {Entity}): Result<Unit> {
        return try {
            {entity}Dao.update({entity}.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update {entity}", e)
        }
    }

    override suspend fun delete{Entity}(id: String): Result<Unit> {
        return try {
            val entity = {entity}Dao.getById(id)
            if (entity != null) {
                {entity}Dao.delete(entity)
                Result.Success(Unit)
            } else {
                Result.Error("{Entity} not found")
            }
        } catch (e: Exception) {
            Result.Error("Failed to delete {entity}", e)
        }
    }

    override fun observe{Entities}(): Flow<List<{Entity}>> {
        return {entity}Dao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}

// =============================================================================
// MAPPERS
// =============================================================================

/**
 * Converts database entity to domain model.
 */
private fun {Entity}Entity.toDomainModel(): {Entity} {
    return {Entity}(
        id = id,
        // Map other fields
    )
}

/**
 * Converts domain model to database entity.
 */
private fun {Entity}.toEntity(): {Entity}Entity {
    return {Entity}Entity(
        id = id,
        // Map other fields
    )
}
```

## Existing Patterns

Reference implementations in the codebase:
- [GameRepository.kt](../../core/data/src/main/kotlin/com/rulebook/core/data/repository/GameRepository.kt)
- [GameRepositoryImpl.kt](../../core/data/src/main/kotlin/com/rulebook/core/data/repository/GameRepositoryImpl.kt)

## Key Patterns

### Result Type
Always return `Result<T>` for consistent error handling:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
```

### Mapper Functions
Keep mappers as private extension functions at file bottom:
```kotlin
private fun EntityDb.toDomain(): EntityDomain { ... }
private fun EntityDomain.toDb(): EntityDb { ... }
```

### Flow for Observation
Use Flow for reactive queries, suspend for one-shot operations:
```kotlin
fun observeAll(): Flow<List<Entity>>  // Reactive
suspend fun getById(id: String): Entity?  // One-shot
```

## Integrations

1. **Create DAO:** See [room-entity-dao-template.md](room-entity-dao-template.md)
2. **Create domain model:** In `core/model`
3. **Register in Koin:** In `core/data/di/DataModule.kt`:
   ```kotlin
   single<{Entity}Repository> { {Entity}RepositoryImpl(get()) }
   ```

## Checklist

- [ ] Created interface with all CRUD operations
- [ ] Created implementation with dependency injection
- [ ] All methods return `Result<T>`
- [ ] Mapper functions for entity conversion
- [ ] KDoc comments on interface methods
- [ ] Registered in Koin DataModule

## References

- [room-entity-dao-template.md](room-entity-dao-template.md) - Database layer
- [koin-module-template.md](koin-module-template.md) - DI registration
