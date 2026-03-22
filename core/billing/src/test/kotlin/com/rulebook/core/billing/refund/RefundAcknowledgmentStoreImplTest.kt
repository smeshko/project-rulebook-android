package com.rulebook.core.billing.refund

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RefundAcknowledgmentStoreImplTest {

    private lateinit var fakeDataStore: FakeRefundPreferencesDataStore
    private lateinit var store: RefundAcknowledgmentStoreImpl

    @Before
    fun setup() {
        fakeDataStore = FakeRefundPreferencesDataStore()
        store = RefundAcknowledgmentStoreImpl(fakeDataStore)
    }

    @Test
    fun `isAcknowledged returns false for unknown token`() = runTest {
        assertFalse(store.isAcknowledged("unknown-token"))
    }

    @Test
    fun `acknowledge makes token acknowledged`() = runTest {
        store.acknowledge("token-1")
        assertTrue(store.isAcknowledged("token-1"))
    }

    @Test
    fun `acknowledge stores multiple tokens independently`() = runTest {
        store.acknowledge("token-1")
        store.acknowledge("token-2")

        assertTrue(store.isAcknowledged("token-1"))
        assertTrue(store.isAcknowledged("token-2"))
    }

    @Test
    fun `acknowledging the same token twice is idempotent`() = runTest {
        store.acknowledge("token-1")
        store.acknowledge("token-1")

        assertTrue(store.isAcknowledged("token-1"))
    }

    @Test
    fun `isAcknowledged returns false for non-acknowledged token when other tokens exist`() = runTest {
        store.acknowledge("token-1")

        assertFalse(store.isAcknowledged("token-2"))
    }

    @Test
    fun `clear removes all acknowledged tokens`() = runTest {
        store.acknowledge("token-1")
        store.acknowledge("token-2")

        store.clear()

        assertFalse(store.isAcknowledged("token-1"))
        assertFalse(store.isAcknowledged("token-2"))
    }

    @Test
    fun `isAcknowledged returns false after clear`() = runTest {
        store.acknowledge("token-1")
        store.clear()

        assertFalse(store.isAcknowledged("token-1"))
    }
}

// ======================================================================
// Fake DataStore for unit testing
// ======================================================================

class FakeRefundPreferencesDataStore : DataStore<Preferences> {
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
