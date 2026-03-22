package com.rulebook.core.billing.refund

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.refundAcknowledgmentDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "refund_acknowledgments"
)

/**
 * DataStore-backed implementation of [RefundAcknowledgmentStore].
 *
 * Stores acknowledged refund tokens as a Set<String> using [stringSetPreferencesKey].
 * Set semantics naturally prevent duplicates.
 *
 * The [dataStore] parameter accepts either the production DataStore (from [create]) or a
 * test double for unit testing.
 *
 * @param dataStore The DataStore instance for persisting acknowledged tokens.
 */
class RefundAcknowledgmentStoreImpl(private val dataStore: DataStore<Preferences>) : RefundAcknowledgmentStore {

    companion object {
        val ACKNOWLEDGED_TOKENS_KEY = stringSetPreferencesKey("acknowledged_refund_tokens")

        /** Factory for production use — creates a context-scoped DataStore. */
        fun create(context: Context): RefundAcknowledgmentStoreImpl =
            RefundAcknowledgmentStoreImpl(context.refundAcknowledgmentDataStore)
    }

    override suspend fun isAcknowledged(purchaseToken: String): Boolean {
        val preferences = dataStore.data.first()
        val acknowledged = preferences[ACKNOWLEDGED_TOKENS_KEY] ?: emptySet()
        return purchaseToken in acknowledged
    }

    override suspend fun acknowledge(purchaseToken: String) {
        dataStore.edit { preferences ->
            val current = preferences[ACKNOWLEDGED_TOKENS_KEY] ?: emptySet()
            preferences[ACKNOWLEDGED_TOKENS_KEY] = current + purchaseToken
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ACKNOWLEDGED_TOKENS_KEY)
        }
    }
}
