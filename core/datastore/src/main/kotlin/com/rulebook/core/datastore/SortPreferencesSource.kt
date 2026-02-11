package com.rulebook.core.datastore

import com.rulebook.core.model.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for sort order preferences data source.
 *
 * Allows LibraryViewModel to be tested without DataStore dependency.
 * [RulebookPreferences] implements this interface in production.
 */
interface SortPreferencesSource {

    /**
     * Flow of current library sort order.
     * Default value is [SortOrder.RECENT].
     */
    val sortOrder: Flow<SortOrder>

    /**
     * Sets the library sort order.
     *
     * @param order The sort order to persist.
     */
    suspend fun setSortOrder(order: SortOrder)
}
