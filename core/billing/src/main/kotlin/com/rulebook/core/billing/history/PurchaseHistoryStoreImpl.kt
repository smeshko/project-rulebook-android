package com.rulebook.core.billing.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.purchaseHistoryDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "purchase_history"
)

/**
 * DataStore-backed implementation of [PurchaseHistoryStore].
 *
 * Persists purchase records as a newline-delimited string with entries in the format
 * `"token::productId"`. Records are stored newest-first and capped at [PurchaseHistoryStore.MAX_ENTRIES].
 *
 * The [dataStore] parameter accepts either the production DataStore (from [create]) or a
 * test double for unit testing.
 *
 * @param dataStore The DataStore instance for persisting purchase records.
 */
class PurchaseHistoryStoreImpl(private val dataStore: DataStore<Preferences>) : PurchaseHistoryStore {

    companion object {
        val ENTRIES_KEY = stringPreferencesKey("purchase_history_entries")
        const val DELIMITER = "\n"
        const val ENTRY_SEPARATOR = "::"

        /** Factory for production use — creates a context-scoped DataStore. */
        fun create(context: Context): PurchaseHistoryStoreImpl =
            PurchaseHistoryStoreImpl(context.purchaseHistoryDataStore)
    }

    override suspend fun savePurchase(purchaseToken: String, productId: String) {
        dataStore.edit { preferences ->
            val existing = preferences[ENTRIES_KEY] ?: ""
            val existingEntries = if (existing.isEmpty()) emptyList() else existing.split(DELIMITER)
            val newEntry = "$purchaseToken$ENTRY_SEPARATOR$productId"
            val updated = (listOf(newEntry) + existingEntries)
                .take(PurchaseHistoryStore.MAX_ENTRIES)
            preferences[ENTRIES_KEY] = updated.joinToString(DELIMITER)
        }
    }

    override suspend fun getRecentTokens(): List<String> {
        val preferences = dataStore.data.first()
        val stored = preferences[ENTRIES_KEY] ?: return emptyList()
        if (stored.isEmpty()) return emptyList()
        return stored.split(DELIMITER).mapNotNull { entry ->
            entry.split(ENTRY_SEPARATOR).firstOrNull()?.takeIf { it.isNotEmpty() }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ENTRIES_KEY)
        }
    }
}
