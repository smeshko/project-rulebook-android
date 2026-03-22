package com.rulebook.core.billing.pending

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val PREFS_FILE_NAME = "pending_validations"
private const val PENDING_KEY = "pending_validations"

/**
 * [EncryptedSharedPreferences]-backed implementation of [PendingValidationStore].
 *
 * Persists pending validations as a JSON array under a single encrypted key.
 * All read/write operations are performed on [Dispatchers.IO] for thread safety.
 *
 * The [sharedPreferences] parameter accepts either the production EncryptedSharedPreferences
 * (from [create]) or a plain [SharedPreferences] test double for unit testing.
 *
 * @param sharedPreferences The SharedPreferences instance for persisting pending validations.
 */
class PendingValidationStoreImpl(
    private val sharedPreferences: SharedPreferences
) : PendingValidationStore {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        /** Factory for production use — creates an EncryptedSharedPreferences instance. */
        fun create(context: Context): PendingValidationStoreImpl {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            return PendingValidationStoreImpl(encryptedPrefs)
        }
    }

    override suspend fun save(pendingValidation: PendingValidation) = withContext(Dispatchers.IO) {
        val existing = readAll()
        // Replace any existing entry with the same token and remove expired entries
        val updated = existing
            .filter { it.purchaseToken != pendingValidation.purchaseToken && !it.isExpired() }
            .plus(pendingValidation)
        write(updated)
    }

    override suspend fun getAll(): List<PendingValidation> = withContext(Dispatchers.IO) {
        val all = readAll()
        val active = all.filter { !it.isExpired() }
        // Write back to remove stale expired entries
        if (active.size < all.size) {
            write(active)
        }
        active
    }

    override suspend fun remove(purchaseToken: String) = withContext(Dispatchers.IO) {
        val existing = readAll()
        val updated = existing.filter { it.purchaseToken != purchaseToken }
        write(updated)
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(PENDING_KEY).apply()
    }

    /** Reads all entries from SharedPreferences, returning an empty list on parse failure. */
    private fun readAll(): List<PendingValidation> {
        val stored = sharedPreferences.getString(PENDING_KEY, null) ?: return emptyList()
        return try {
            json.decodeFromString(stored)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Serializes and writes the list to SharedPreferences. */
    private fun write(entries: List<PendingValidation>) {
        sharedPreferences.edit().putString(PENDING_KEY, json.encodeToString(entries)).apply()
    }
}
