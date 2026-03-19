package com.rulebook.core.database

/**
 * Abstraction for wiping all Room database tables.
 *
 * Implemented by [RulebookDatabase] to allow the Settings feature to clear all
 * stored data without depending on the concrete RoomDatabase implementation.
 *
 * Note: [clearAllTables] is a blocking call and must be invoked from a
 * background thread (e.g., inside `withContext(Dispatchers.IO)`).
 */
interface ClearableDatabase {
    /**
     * Deletes all rows from all tables in the database.
     *
     * This is a blocking operation — it must not be called on the main thread.
     */
    fun clearAllTables()
}
