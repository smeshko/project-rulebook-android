package com.rulebook.core.billing.history

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PurchaseHistoryStoreImplTest {

    private lateinit var fakeDataStore: FakePreferencesDataStore
    private lateinit var store: PurchaseHistoryStoreImpl

    @Before
    fun setup() {
        fakeDataStore = FakePreferencesDataStore()
        store = PurchaseHistoryStoreImpl(fakeDataStore)
    }

    @Test
    fun `savePurchase stores token and productId`() = runTest {
        store.savePurchase("token-1", "credits_3")

        val tokens = store.getRecentTokens()
        assertTrue(tokens.contains("token-1"))
    }

    @Test
    fun `getRecentTokens returns tokens in newest-first order`() = runTest {
        store.savePurchase("token-1", "credits_1")
        store.savePurchase("token-2", "credits_3")
        store.savePurchase("token-3", "credits_10")

        val tokens = store.getRecentTokens()
        assertEquals(listOf("token-3", "token-2", "token-1"), tokens)
    }

    @Test
    fun `savePurchase caps entries at MAX_ENTRIES`() = runTest {
        val maxEntries = PurchaseHistoryStore.MAX_ENTRIES
        repeat(maxEntries + 5) { i ->
            store.savePurchase("token-$i", "credits_1")
        }

        val tokens = store.getRecentTokens()
        assertEquals(maxEntries, tokens.size)
    }

    @Test
    fun `savePurchase keeps newest entries when capping`() = runTest {
        val maxEntries = PurchaseHistoryStore.MAX_ENTRIES
        repeat(maxEntries + 5) { i ->
            store.savePurchase("token-$i", "credits_1")
        }

        val tokens = store.getRecentTokens()
        // Newest entries (highest index) should be kept
        val highestIndex = maxEntries + 4
        assertTrue(tokens.contains("token-$highestIndex"))
    }

    @Test
    fun `clear removes all entries`() = runTest {
        store.savePurchase("token-1", "credits_3")
        store.savePurchase("token-2", "credits_1")

        store.clear()

        val tokens = store.getRecentTokens()
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `getRecentTokens returns empty list when no entries`() = runTest {
        val tokens = store.getRecentTokens()
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `multiple saves and retrieval works correctly`() = runTest {
        store.savePurchase("abc-token", "credits_1")
        store.savePurchase("def-token", "credits_3")

        val tokens = store.getRecentTokens()
        assertEquals(2, tokens.size)
        assertTrue(tokens.contains("abc-token"))
        assertTrue(tokens.contains("def-token"))
    }
}

// ======================================================================
// Fake DataStore for unit testing
// ======================================================================

class FakePreferencesDataStore : DataStore<Preferences> {
    private val _prefs = MutableStateFlow<Preferences>(mutablePreferencesOf())

    override val data: Flow<Preferences> = _prefs

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val current = _prefs.value
        val mutable = current.toMutablePreferences()
        val updated = transform(mutable)
        _prefs.value = updated
        return updated
    }
}
