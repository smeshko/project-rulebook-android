package com.rulebook.core.billing

import android.util.Log
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.datastore.PendingPurchasePreferencesSource
import com.rulebook.core.model.PendingPurchaseResolution
import kotlinx.coroutines.flow.first

private const val TAG = "PendingPurchaseChecker"

/**
 * Encapsulates the app-level check for resolved pending purchases.
 *
 * Intended to be called on app resume (via ProcessLifecycleOwner) to silently
 * deliver credits if a pending Ask-to-Buy purchase was approved while the app
 * was in the background.
 *
 * Resolution flow:
 * 1. Read pending token from DataStore — if none, return early
 * 2. Query billing service for resolution status
 * 3. If PURCHASED: consume → deliver credits → clear token → return [PendingCheckResult.Resolved]
 * 4. If STILL_PENDING: no-op → return [PendingCheckResult.StillPending]
 * 5. If NOT_FOUND: clear token → return [PendingCheckResult.Cancelled]
 *
 * @param pendingPurchasePrefs DataStore source for pending purchase token persistence.
 * @param billingRepository Repository for billing operations.
 * @param purchaseVerifier Verifier that consumes the purchase and resolves credits.
 * @param creditRepository Repository for delivering credits to the user.
 */
class PendingPurchaseChecker(
    private val pendingPurchasePrefs: PendingPurchasePreferencesSource,
    private val billingRepository: BillingRepository,
    private val purchaseVerifier: PurchaseVerifier,
    private val creditRepository: CreditRepository
) {

    /**
     * Checks for and resolves a pending purchase if one exists.
     *
     * @return [PendingCheckResult] indicating the outcome of the check.
     */
    suspend fun checkAndResolve(): PendingCheckResult {
        val pendingToken = pendingPurchasePrefs.pendingPurchaseToken.first()
            ?: return PendingCheckResult.NoPendingPurchase
        val pendingProductId = pendingPurchasePrefs.pendingPurchaseProductId.first()
            ?: return PendingCheckResult.NoPendingPurchase

        val resolutionResult = billingRepository.checkPendingPurchases(pendingToken)
        resolutionResult.onFailure { error ->
            Log.w(TAG, "Failed to check pending purchase resolution on resume", error)
            return PendingCheckResult.CheckFailed
        }

        return when (val resolution = resolutionResult.getOrThrow()) {
            is PendingPurchaseResolution.Purchased -> {
                val verifyResult = purchaseVerifier.verifyAndConsume(resolution.token, resolution.productId)
                verifyResult.onFailure { error ->
                    Log.e(TAG, "Failed to verify/consume resolved pending purchase on resume", error)
                    return PendingCheckResult.CheckFailed
                }

                val credits = verifyResult.getOrThrow().credits
                creditRepository.addCredits(credits)
                pendingPurchasePrefs.clearPendingPurchase()

                PendingCheckResult.Resolved(credits, pendingProductId)
            }

            is PendingPurchaseResolution.StillPending -> PendingCheckResult.StillPending

            is PendingPurchaseResolution.NotFound -> {
                pendingPurchasePrefs.clearPendingPurchase()
                PendingCheckResult.Cancelled
            }
        }
    }
}

/**
 * Result of [PendingPurchaseChecker.checkAndResolve].
 */
sealed class PendingCheckResult {
    /** No pending purchase token stored — nothing to check. */
    data object NoPendingPurchase : PendingCheckResult()

    /** Purchase was approved and credits delivered successfully. */
    data class Resolved(val creditsAdded: Int, val productId: String) : PendingCheckResult()

    /** Purchase is still waiting for approval. */
    data object StillPending : PendingCheckResult()

    /** Purchase was cancelled by the approver — token cleared. */
    data object Cancelled : PendingCheckResult()

    /** Billing service check failed — will retry on next resume. */
    data object CheckFailed : PendingCheckResult()
}
