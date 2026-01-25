---
title: Room Entity & DAO Template
description: Template for creating Room database entities and DAOs
author: Ivo
date: 2026-01-25
---

# Room Entity & DAO Template

## When to Use

- Persisting a new data type to local database
- Need reactive queries with Flow
- Want type-safe database operations
- Require structured local storage

## Quick Reference

| Aspect | Value |
|--------|-------|
| Entity Location | `core/database/src/main/kotlin/.../entity/{Entity}Entity.kt` |
| DAO Location | `core/database/src/main/kotlin/.../{Entity}Dao.kt` |
| Pattern | Entity + DAO pair |
| Table Naming | `lowercase_plural` (e.g., `saved_games`) |
| Column Naming | `snake_case` (e.g., `created_at`) |

## Code Template - Entity

```kotlin
package com.rulebook.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for {description}.
 *
 * Stored in the `{table_name}` table.
 */
@Entity(tableName = "{table_name}")
data class {Entity}Entity(
    @PrimaryKey
    val id: String,

    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "image_url")
    val imageUrl: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
```

## Code Template - DAO

```kotlin
package com.rulebook.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rulebook.core.database.entity.{Entity}Entity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [{Entity}Entity] operations.
 *
 * All query methods return [Flow] for reactive data observation.
 * Modification methods are suspend functions for coroutine integration.
 */
@Dao
interface {Entity}Dao {

    // =========================================================================
    // INSERT / UPDATE / DELETE
    // =========================================================================

    /**
     * Inserts an entity. If an entity with the same ID already exists,
     * it will be replaced (upsert behavior).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert({entity}: {Entity}Entity)

    /**
     * Inserts multiple entities with upsert behavior.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll({entities}: List<{Entity}Entity>)

    /**
     * Updates an existing entity.
     */
    @Update
    suspend fun update({entity}: {Entity}Entity)

    /**
     * Deletes an entity.
     */
    @Delete
    suspend fun delete({entity}: {Entity}Entity)

    /**
     * Deletes an entity by ID.
     */
    @Query("DELETE FROM {table_name} WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletes all entities.
     */
    @Query("DELETE FROM {table_name}")
    suspend fun deleteAll()

    // =========================================================================
    // QUERIES - ONE-SHOT
    // =========================================================================

    /**
     * Returns an entity by ID, or null if not found.
     */
    @Query("SELECT * FROM {table_name} WHERE id = :id")
    suspend fun getById(id: String): {Entity}Entity?

    /**
     * Returns all entities (unsorted).
     */
    @Query("SELECT * FROM {table_name}")
    suspend fun getAll(): List<{Entity}Entity>

    // =========================================================================
    // QUERIES - REACTIVE (FLOW)
    // =========================================================================

    /**
     * Observes an entity by ID.
     * Emits null if not found, emits new value on changes.
     */
    @Query("SELECT * FROM {table_name} WHERE id = :id")
    fun observeById(id: String): Flow<{Entity}Entity?>

    /**
     * Observes all entities (unsorted).
     * Emits a new list whenever the underlying data changes.
     */
    @Query("SELECT * FROM {table_name}")
    fun observeAll(): Flow<List<{Entity}Entity>>

    /**
     * Observes all entities sorted by creation date (newest first).
     */
    @Query("SELECT * FROM {table_name} ORDER BY created_at DESC")
    fun observeAllSortedByCreatedAtDesc(): Flow<List<{Entity}Entity>>

    /**
     * Observes all entities sorted alphabetically by title.
     */
    @Query("SELECT * FROM {table_name} ORDER BY title ASC")
    fun observeAllSortedByTitleAsc(): Flow<List<{Entity}Entity>>

    // =========================================================================
    // AGGREGATE QUERIES
    // =========================================================================

    /**
     * Returns the count of entities.
     */
    @Query("SELECT COUNT(*) FROM {table_name}")
    suspend fun getCount(): Int

    /**
     * Observes the count of entities.
     */
    @Query("SELECT COUNT(*) FROM {table_name}")
    fun observeCount(): Flow<Int>
}
```

## Existing Patterns

Reference implementations in the codebase:
- [GameEntity.kt](../../core/database/src/main/kotlin/com/rulebook/core/database/entity/GameEntity.kt)
- [GameDao.kt](../../core/database/src/main/kotlin/com/rulebook/core/database/GameDao.kt)

## Key Patterns

### Column Naming
Use `@ColumnInfo` for multi-word columns:
```kotlin
@ColumnInfo(name = "created_at")
val createdAt: Long
```

### Timestamps
Store as `Long` (milliseconds since epoch):
```kotlin
val createdAt: Long = System.currentTimeMillis()
```

### Flow vs Suspend
- **Flow:** For reactive observation (UI updates automatically)
- **Suspend:** For one-shot operations (get once, modify)

### OnConflictStrategy
- `REPLACE`: Upsert behavior (insert or update)
- `IGNORE`: Skip if exists
- `ABORT`: Throw exception if exists

## Integrations

1. **Add to Database:** Register entity and DAO in `RulebookDatabase.kt`:
   ```kotlin
   @Database(
       entities = [
           GameEntity::class,
           {Entity}Entity::class  // Add here
       ],
       version = 2  // Increment version
   )
   abstract class RulebookDatabase : RoomDatabase() {
       abstract fun gameDao(): GameDao
       abstract fun {entity}Dao(): {Entity}Dao  // Add here
   }
   ```

2. **Create migration:** If adding to existing database:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL("""
               CREATE TABLE IF NOT EXISTS `{table_name}` (
                   `id` TEXT NOT NULL PRIMARY KEY,
                   `title` TEXT NOT NULL,
                   `created_at` INTEGER NOT NULL
               )
           """)
       }
   }
   ```

3. **Register in Koin:** In `DatabaseModule.kt`:
   ```kotlin
   single { get<RulebookDatabase>().{entity}Dao() }
   ```

## Checklist

- [ ] Created Entity with `@Entity` annotation
- [ ] Table name is `lowercase_plural`
- [ ] Column names are `snake_case`
- [ ] `@PrimaryKey` on id field
- [ ] Created DAO with `@Dao` annotation
- [ ] Insert/Update/Delete as suspend functions
- [ ] Query methods return Flow for reactive observation
- [ ] Added entity to `@Database` entities array
- [ ] Added DAO abstract function to Database
- [ ] Created migration if modifying existing database
- [ ] Registered DAO in Koin DatabaseModule

## References

- [repository-template.md](repository-template.md) - Repository layer using DAO
- [koin-module-template.md](koin-module-template.md) - DI registration
