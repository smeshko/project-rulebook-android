package com.rulebook.core.billing.pending

/**
 * Storage for purchase validations that could not be completed due to transient network errors.
 *
 * Implementations must persist entries across app launches (e.g. using EncryptedSharedPreferences).
 * Used by the retry mechanism in Story 10.3 to queue failed validations for recovery in Story 10.4.
 *
 * All methods are safe to call from any coroutine context.
 */
interface PendingValidationStore {

    /**
     * Saves a pending validation entry.
     *
     * If an entry with the same [PendingValidation.purchaseToken] already exists, it is replaced.
     * Expired entries (older than [PendingValidation.EXPIRY_DURATION_MS]) are removed on save.
     */
    suspend fun save(pendingValidation: PendingValidation)

    /**
     * Returns all non-expired pending validation entries.
     *
     * Expired entries are automatically removed from storage when this method is called.
     */
    suspend fun getAll(): List<PendingValidation>

    /**
     * Removes the pending validation entry with the given [purchaseToken], if present.
     */
    suspend fun remove(purchaseToken: String)

    /**
     * Removes all pending validation entries.
     */
    suspend fun clear()
}
