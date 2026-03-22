package com.rulebook.core.billing.pending

import android.content.SharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PendingValidationStoreImplTest {

    private lateinit var fakePrefs: FakeSharedPreferences
    private lateinit var store: PendingValidationStoreImpl

    @Before
    fun setup() {
        fakePrefs = FakeSharedPreferences()
        store = PendingValidationStoreImpl(fakePrefs)
    }

    @Test
    fun `save stores a pending validation and getAll retrieves it`() = runTest {
        val pending = PendingValidation(
            purchaseToken = "token-1",
            productId = "credits_3",
            timestamp = System.currentTimeMillis(),
            retryCount = 3
        )

        store.save(pending)

        val result = store.getAll()
        assertEquals(1, result.size)
        assertEquals("token-1", result.first().purchaseToken)
        assertEquals("credits_3", result.first().productId)
        assertEquals(3, result.first().retryCount)
    }

    @Test
    fun `remove deletes a specific entry by purchaseToken`() = runTest {
        val pending1 = PendingValidation("token-a", "credits_1", System.currentTimeMillis(), 3)
        val pending2 = PendingValidation("token-b", "credits_3", System.currentTimeMillis(), 3)

        store.save(pending1)
        store.save(pending2)

        store.remove("token-a")

        val result = store.getAll()
        assertEquals(1, result.size)
        assertEquals("token-b", result.first().purchaseToken)
    }

    @Test
    fun `clear removes all entries`() = runTest {
        store.save(PendingValidation("token-1", "credits_1", System.currentTimeMillis(), 3))
        store.save(PendingValidation("token-2", "credits_3", System.currentTimeMillis(), 3))

        store.clear()

        assertTrue(store.getAll().isEmpty())
    }

    @Test
    fun `getAll returns empty list when no entries exist`() = runTest {
        assertTrue(store.getAll().isEmpty())
    }

    @Test
    fun `multiple entries can be stored and retrieved`() = runTest {
        store.save(PendingValidation("token-1", "credits_1", System.currentTimeMillis(), 1))
        store.save(PendingValidation("token-2", "credits_3", System.currentTimeMillis(), 2))
        store.save(PendingValidation("token-3", "credits_10", System.currentTimeMillis(), 3))

        val result = store.getAll()
        assertEquals(3, result.size)
        assertTrue(result.any { it.purchaseToken == "token-1" })
        assertTrue(result.any { it.purchaseToken == "token-2" })
        assertTrue(result.any { it.purchaseToken == "token-3" })
    }

    @Test
    fun `expired entries are filtered out on getAll`() = runTest {
        val now = System.currentTimeMillis()
        val expiredTimestamp = now - PendingValidation.EXPIRY_DURATION_MS - 1000L
        val validTimestamp = now

        store.save(PendingValidation("expired-token", "credits_1", expiredTimestamp, 3))
        store.save(PendingValidation("valid-token", "credits_3", validTimestamp, 3))

        val result = store.getAll()
        assertEquals(1, result.size)
        assertEquals("valid-token", result.first().purchaseToken)
    }

    @Test
    fun `save replaces existing entry with same purchaseToken`() = runTest {
        val original = PendingValidation("token-x", "credits_1", System.currentTimeMillis(), 1)
        val updated = PendingValidation("token-x", "credits_3", System.currentTimeMillis(), 3)

        store.save(original)
        store.save(updated)

        val result = store.getAll()
        assertEquals(1, result.size)
        assertEquals("credits_3", result.first().productId)
        assertEquals(3, result.first().retryCount)
    }

    @Test
    fun `save filters out expired entries from existing data`() = runTest {
        val now = System.currentTimeMillis()
        val expiredTimestamp = now - PendingValidation.EXPIRY_DURATION_MS - 1000L

        // Directly write expired + valid entries to fake prefs
        store.save(PendingValidation("expired-token", "credits_1", expiredTimestamp, 3))
        store.save(PendingValidation("new-token", "credits_3", now, 3))

        val result = store.getAll()
        assertFalse(result.any { it.purchaseToken == "expired-token" })
        assertTrue(result.any { it.purchaseToken == "new-token" })
    }

    @Test
    fun `remove on nonexistent token leaves other entries intact`() = runTest {
        store.save(PendingValidation("token-keep", "credits_1", System.currentTimeMillis(), 3))

        store.remove("nonexistent-token")

        val result = store.getAll()
        assertEquals(1, result.size)
        assertEquals("token-keep", result.first().purchaseToken)
    }
}

// ======================================================================
// Fake SharedPreferences for unit testing (avoids Android context dependency)
// ======================================================================

class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    override fun getString(key: String, defValue: String?): String? =
        (data[key] as? String) ?: defValue

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun contains(key: String): Boolean = data.containsKey(key)

    override fun getAll(): Map<String, *> = data.toMap()

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = null

    override fun getInt(key: String, defValue: Int): Int = defValue

    override fun getLong(key: String, defValue: Long): Long = defValue

    override fun getFloat(key: String, defValue: Float): Float = defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean = defValue

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) = Unit

    inner class FakeEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private val toRemove = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            pending[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            toRemove.add(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearAll = true
            return this
        }

        override fun apply() {
            if (clearAll) data.clear()
            toRemove.forEach { data.remove(it) }
            pending.forEach { (k, v) -> if (v != null) data[k] = v else data.remove(k) }
        }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String, value: Int): SharedPreferences.Editor = this
        override fun putLong(key: String, value: Long): SharedPreferences.Editor = this
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = this
    }
}
