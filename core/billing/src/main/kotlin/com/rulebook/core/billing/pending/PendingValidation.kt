package com.rulebook.core.billing.pending

import kotlinx.serialization.Serializable

/**
 * Represents a purchase validation that could not be completed due to a transient network error.
 *
 * Stored in [EncryptedSharedPreferences] when server validation fails after all retry attempts.
 * Persists across app launches for recovery in Story 10.4.
 *
 * @param purchaseToken The Google Play purchase token to validate.
 * @param productId The product ID (SKU) of the purchase.
 * @param timestamp The epoch milliseconds when this entry was created.
 * @param retryCount The number of retry attempts already made.
 */
@Serializable
data class PendingValidation(
    val purchaseToken: String,
    val productId: String,
    val timestamp: Long,
    val retryCount: Int
) {
    companion object {
        /** 72 hours in milliseconds — matches Google's 3-day purchase acknowledgment window. */
        const val EXPIRY_DURATION_MS = 72L * 60L * 60L * 1000L
    }

    /** Returns true if this entry is older than [EXPIRY_DURATION_MS]. */
    fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > EXPIRY_DURATION_MS
}
