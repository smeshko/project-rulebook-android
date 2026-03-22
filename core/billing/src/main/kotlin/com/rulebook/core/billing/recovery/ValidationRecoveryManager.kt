package com.rulebook.core.billing.recovery

import android.util.Log
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.pending.PendingValidationStore
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.verification.PurchaseValidationException
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.billing.verification.VerificationStatus
import com.rulebook.core.data.repository.CreditRepository

private const val TAG = "ValidationRecoveryManager"

/**
 * Contract for running app-launch purchase recovery.
 *
 * Allows [ValidationRecoveryManager] to be swapped with a test double in unit tests.
 */
interface ValidationRecovery {
    /**
     * Runs the full recovery flow and returns a summary of the outcome.
     */
    suspend fun recover(): RecoveryResult
}

/**
 * Orchestrates app-launch recovery of purchases that failed server-side validation.
 *
 * Recovery runs on app launch to handle two failure scenarios:
 * 1. **Pending store entries**: Purchases that failed validation due to transient network errors
 *    and were queued in [PendingValidationStore] (Story 10.3).
 * 2. **Orphaned purchases**: Purchases present in Google Play but absent from the pending
 *    store — caused by process death, crashes, or reinstallation (queryUnconsumedPurchases).
 *
 * Recovery is non-blocking: call [recover] in a separate [kotlinx.coroutines.Dispatchers.IO]
 * coroutine so app startup is not delayed.
 *
 * Silent operation — no UI unless credits are actually recovered (surface via [RecoveryResult]).
 *
 * @param pendingValidationStore Persistent queue of failed validation attempts.
 * @param billingRepository Repository for querying Google Play purchase state.
 * @param purchaseVerifier Verifier that sends the receipt to the backend.
 * @param creditRepository Repository for delivering credits to the user.
 * @param purchaseHistoryStore Local history of completed purchases for deduplication.
 * @param analyticsManager Analytics for tracking recovery outcomes.
 */
class ValidationRecoveryManager(
    private val pendingValidationStore: PendingValidationStore,
    private val billingRepository: BillingRepository,
    private val purchaseVerifier: PurchaseVerifier,
    private val creditRepository: CreditRepository,
    private val purchaseHistoryStore: PurchaseHistoryStore,
    private val analyticsManager: AnalyticsManager
) : ValidationRecovery {

    /**
     * Runs the full recovery flow and returns a summary of the outcome.
     *
     * Steps:
     * 1. Process all non-expired pending validation entries.
     * 2. Query Google Play for unconsumed purchases not in the pending store or history.
     * 3. Fire analytics for the recovery attempt.
     *
     * @return [RecoveryResult] with counts of recovered credits and processing outcomes.
     */
    override suspend fun recover(): RecoveryResult {
        val recentTokens = purchaseHistoryStore.getRecentTokens().toSet()

        // Step 1: Process pending store entries (getAll auto-prunes expired entries)
        val pendingEntries = pendingValidationStore.getAll()
        val pendingCount = pendingEntries.size
        val pendingTokens = pendingEntries.map { it.purchaseToken }.toSet()

        var creditsRecovered = 0
        var recoveredCount = 0
        var failedCount = 0

        for (entry in pendingEntries) {
            val verifyResult = purchaseVerifier.verifyAndConsume(entry.purchaseToken, entry.productId)

            verifyResult.fold(
                onSuccess = { verificationResult ->
                    // Guard against double-delivery: if ALREADY_PROCESSED and in history, credits
                    // were previously delivered — just clean up the pending entry.
                    val alreadyDelivered = verificationResult.status == VerificationStatus.ALREADY_PROCESSED
                        && entry.purchaseToken in recentTokens

                    if (alreadyDelivered) {
                        Log.d(TAG, "Token ${entry.purchaseToken} already processed and in history — removing from pending")
                        pendingValidationStore.remove(entry.purchaseToken)
                    } else {
                        val creditsAdded = creditRepository.addCredits(verificationResult.credits)
                        if (creditsAdded) {
                            purchaseHistoryStore.savePurchase(entry.purchaseToken, entry.productId)
                            pendingValidationStore.remove(entry.purchaseToken)
                            creditsRecovered += verificationResult.credits
                            recoveredCount++
                        } else {
                            Log.e(TAG, "Failed to add credits for token ${entry.purchaseToken}")
                            failedCount++
                        }
                    }
                },
                onFailure = { error ->
                    when (error) {
                        is PurchaseValidationException -> {
                            // Server returned INVALID — purchase is unrecoverable, remove from store
                            Log.w(TAG, "Purchase ${entry.purchaseToken} is INVALID — removing from pending store")
                            pendingValidationStore.remove(entry.purchaseToken)
                        }
                        else -> {
                            // Transient failure — keep in store, increment retry count for next launch
                            Log.w(TAG, "Transient failure validating ${entry.purchaseToken}, will retry on next launch", error)
                            pendingValidationStore.save(entry.copy(retryCount = entry.retryCount + 1))
                            failedCount++
                        }
                    }
                }
            )
        }

        // Step 2: Handle orphaned purchases (process death, crash, reinstall)
        // queryUnconsumedPurchases finds PURCHASED state purchases not yet consumed
        billingRepository.queryUnconsumedPurchases()
            .onSuccess { unconsumedPurchases ->
                for (purchase in unconsumedPurchases) {
                    // Skip tokens already in the pending store (being processed above)
                    // or already in the purchase history (credits already delivered)
                    if (purchase.purchaseToken in pendingTokens || purchase.purchaseToken in recentTokens) {
                        continue
                    }

                    val verifyResult = purchaseVerifier.verifyAndConsume(purchase.purchaseToken, purchase.productId)
                    verifyResult.fold(
                        onSuccess = { verificationResult ->
                            val creditsAdded = creditRepository.addCredits(verificationResult.credits)
                            if (creditsAdded) {
                                purchaseHistoryStore.savePurchase(purchase.purchaseToken, purchase.productId)
                                creditsRecovered += verificationResult.credits
                                recoveredCount++
                            } else {
                                Log.e(TAG, "Failed to add credits for orphaned purchase ${purchase.purchaseToken}")
                            }
                        },
                        onFailure = { error ->
                            Log.w(TAG, "Failed to validate orphaned purchase ${purchase.purchaseToken}", error)
                        }
                    )
                }
            }
            .onFailure { error ->
                Log.w(TAG, "Failed to query unconsumed purchases during recovery — orphan recovery skipped", error)
            }

        // Step 3: Fire analytics
        val result = RecoveryResult(
            creditsRecovered = creditsRecovered,
            pendingCount = pendingCount,
            recoveredCount = recoveredCount,
            expiredCount = 0, // getAll() auto-prunes expired; exact count not available
            failedCount = failedCount
        )

        analyticsManager.trackPurchaseRecoveryAttempted(
            pendingCount = result.pendingCount,
            recoveredCount = result.recoveredCount,
            expiredCount = result.expiredCount
        )

        return result
    }
}

/**
 * Summary of a [ValidationRecoveryManager.recover] run.
 *
 * @property creditsRecovered Total credits delivered to the user during this recovery.
 * @property pendingCount Number of non-expired entries found in the pending store at launch.
 * @property recoveredCount Number of purchases successfully validated and credited.
 * @property expiredCount Number of entries removed as expired (auto-pruned by store).
 * @property failedCount Number of entries that failed with a transient error.
 */
data class RecoveryResult(
    val creditsRecovered: Int,
    val pendingCount: Int,
    val recoveredCount: Int,
    val expiredCount: Int,
    val failedCount: Int
)
