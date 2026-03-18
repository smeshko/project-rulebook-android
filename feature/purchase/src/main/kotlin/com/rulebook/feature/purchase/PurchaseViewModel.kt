package com.rulebook.feature.purchase

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.BillingResponseCode
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.datastore.PendingPurchasePreferencesSource
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.PurchaseState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PurchaseViewModel"

/**
 * ViewModel for the Purchase (Paywall) screen.
 *
 * Manages the paywall UI state and handles product loading and purchase operations.
 * Follows the MVI pattern with [PurchaseUiState] as the single source of truth.
 *
 * The ViewModel handles:
 * - Loading available in-app products from the billing service
 * - Observing user's credit balance
 * - Initiating and tracking purchase flows
 * - Handling purchase success/failure/cancellation
 * - Restore purchases
 * - Dismiss action
 *
 * @param creditRepository Repository for observing and modifying credit balance.
 * @param billingRepository Repository for billing operations and purchase updates.
 * @param analyticsManager Manager for tracking analytics events.
 * @param purchaseVerifier Verifier that consumes the purchase and resolves credits.
 * @param pendingPurchasePrefs DataStore preferences for pending purchase token storage.
 */
class PurchaseViewModel(
    private val creditRepository: CreditRepository,
    private val billingRepository: BillingRepository,
    private val analyticsManager: AnalyticsManager,
    private val purchaseVerifier: PurchaseVerifier,
    private val pendingPurchasePrefs: PendingPurchasePreferencesSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())

    /**
     * The current UI state of the purchase screen.
     */
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    /**
     * Channel for one-time events.
     *
     * Using Channel with BUFFERED capacity ensures events aren't lost if emitted
     * before the collector is ready. Events are consumed exactly once.
     */
    private val _events = Channel<PurchaseEvent>(Channel.BUFFERED)

    /**
     * Flow of one-time events for the UI to collect.
     */
    val events: Flow<PurchaseEvent> = _events.receiveAsFlow()

    init {
        // Observe credit balance changes and update UI state
        creditRepository.creditBalance
            .catch { emit(0) }
            .onEach { balance ->
                _uiState.update { it.copy(currentBalance = balance) }
            }
            .launchIn(viewModelScope)

        // Observe billing products and update UI state
        billingRepository.products
            .catch {
                Log.e(TAG, "Error observing products", it)
                emit(emptyList())
            }
            .onEach { products ->
                _uiState.update { it.copy(products = products) }
            }
            .launchIn(viewModelScope)

        // Observe purchase update callbacks from Google Play
        billingRepository.purchaseUpdates
            .onEach { update -> handlePurchaseUpdate(update) }
            .launchIn(viewModelScope)

        // Query products from Play Store
        queryProducts()

        // Check if a pending purchase was resolved since last launch
        viewModelScope.launch {
            checkPendingPurchaseResolution()
        }
    }

    /**
     * Queries available products from the Google Play billing service.
     */
    private fun queryProducts() {
        viewModelScope.launch {
            val result = billingRepository.queryProducts()
            result.onFailure { error ->
                Log.e(TAG, "Failed to query products", error)
                _uiState.update { it.copy(error = "Failed to load products", isLoading = false) }
            }
            result.onSuccess {
                // Products are already emitted via billingRepository.products flow
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Handles user selecting a product to purchase.
     *
     * Sets the purchase state to [PurchaseState.Processing], launches the Google Play
     * billing flow, and waits for the result via [handlePurchaseUpdate].
     *
     * @param activity The current Activity, required by BillingClient.launchBillingFlow.
     *                 If null (e.g. in tests where Activity cannot be created), the billing
     *                 flow is not launched but state and analytics are still updated.
     * @param productId The product identifier (SKU) to purchase.
     */
    fun onProductSelected(activity: Activity?, productId: String) {
        analyticsManager.trackEvent("purchase_started", mapOf("product_id" to productId))
        _uiState.update { it.copy(purchaseState = PurchaseState.Processing(productId)) }

        if (activity == null) {
            Log.w(TAG, "onProductSelected called with null activity — billing flow not launched")
            return
        }

        viewModelScope.launch {
            val result = billingRepository.launchPurchaseFlow(activity, productId)
            result.onFailure { error ->
                Log.e(TAG, "Failed to launch purchase flow for $productId", error)
                analyticsManager.trackEvent(
                    "purchase_failed",
                    mapOf("product_id" to productId, "error_code" to "LAUNCH_FAILED")
                )
                _uiState.update {
                    it.copy(purchaseState = PurchaseState.Error(error.message ?: "Failed to start purchase"))
                }
            }
        }
    }

    /**
     * Handles purchase result callbacks from Google Play.
     *
     * Called when a PurchaseUpdate arrives on the purchaseUpdates SharedFlow.
     * Only processes updates when a purchase is currently in [PurchaseState.Processing].
     */
    private fun handlePurchaseUpdate(update: PurchaseUpdate) {
        val currentState = _uiState.value.purchaseState
        if (currentState !is PurchaseState.Processing) return

        val productId = currentState.sku

        when (update.responseCode) {
            BillingResponseCode.OK -> {
                // Check for pending purchases (Ask-to-Buy / family approval)
                val pendingToken = update.pendingPurchaseTokens.firstOrNull()
                if (pendingToken != null) {
                    val pendingProductId = update.pendingProductIds.firstOrNull() ?: productId
                    analyticsManager.trackEvent(
                        "purchase_pending",
                        mapOf("product_id" to pendingProductId)
                    )
                    viewModelScope.launch {
                        pendingPurchasePrefs.setPendingPurchase(pendingToken, pendingProductId)
                    }
                    _uiState.update { it.copy(purchaseState = PurchaseState.Pending) }
                    return
                }

                val purchaseToken = update.purchaseTokens.firstOrNull()
                if (purchaseToken == null) {
                    Log.e(TAG, "Purchase OK but no token received for $productId")
                    analyticsManager.trackEvent(
                        "purchase_failed",
                        mapOf("product_id" to productId, "error_code" to "TOKEN_MISSING")
                    )
                    _uiState.update {
                        it.copy(purchaseState = PurchaseState.Error("Purchase token missing"))
                    }
                    return
                }
                viewModelScope.launch {
                    val verifyResult = purchaseVerifier.verifyAndConsume(purchaseToken, productId)
                    verifyResult.onFailure { error ->
                        Log.e(TAG, "Failed to verify/consume purchase for $productId", error)
                        analyticsManager.trackEvent(
                            "purchase_failed",
                            mapOf("product_id" to productId, "error_code" to "CONSUME_FAILED")
                        )
                        _uiState.update {
                            it.copy(purchaseState = PurchaseState.Error("Purchase verification failed"))
                        }
                        return@launch
                    }

                    val credits = verifyResult.getOrThrow().credits

                    // Deliver credits only after successful consumption
                    creditRepository.addCredits(credits)

                    val newBalance = creditRepository.creditBalance.first()

                    analyticsManager.trackEvent(
                        "purchase_completed",
                        mapOf(
                            "product_id" to productId,
                            "credits_added" to credits.toString(),
                            "new_balance" to newBalance.toString()
                        )
                    )

                    _uiState.update { it.copy(purchaseState = PurchaseState.Success(credits)) }

                    // Auto-dismiss after success animation plays (1.5s)
                    delay(1500)
                    _events.send(PurchaseEvent.PurchaseSuccess(credits))
                }
            }

            BillingResponseCode.USER_CANCELED -> {
                analyticsManager.trackEvent(
                    "purchase_failed",
                    mapOf("product_id" to productId, "error_code" to "USER_CANCELED")
                )
                // Silent reset — no error shown to user
                _uiState.update { it.copy(purchaseState = null) }
            }

            else -> {
                analyticsManager.trackEvent(
                    "purchase_failed",
                    mapOf("product_id" to productId, "error_code" to update.responseCode.toString())
                )
                _uiState.update {
                    it.copy(
                        purchaseState = PurchaseState.Error(
                            "Purchase failed (code: ${update.responseCode})"
                        )
                    )
                }
            }
        }
    }

    /**
     * Handles user tapping the "Restore Purchases" button.
     *
     * Queries unconsumed purchases, verifies and consumes each one, delivers credits,
     * and emits a one-time event with the result.
     */
    /**
     * Initiates a restore purchases operation to recover previously purchased items.
     *
     * This method queries the Google Play Billing service for unconsumed in-app purchases,
     * verifies each purchase, and delivers credits for any successfully verified purchases.
     *
     * Flow:
     * 1. Sets [isRestoring] = true, prevents duplicate calls
     * 2. Tracks "paywall_restore_purchases_tapped" analytics event
     * 3. Queries [BillingRepository.queryUnconsumedPurchases]
     * 4. For each found purchase:
     *    - Calls [PurchaseVerifier.verifyAndConsume] to verify and consume the purchase
     *    - On success: calls [CreditRepository.addCredits] to deliver credits
     *    - On failure: logs warning but continues with next purchase (partial success allowed)
     * 5. Emits result events:
     *    - [PurchaseEvent.RestoreSuccess] if any credits were restored
     *    - [PurchaseEvent.RestoreNoPurchases] if no unconsumed purchases found
     *    - [PurchaseEvent.RestoreError] if query or all verifications failed
     * 6. Tracks "purchase_restored" analytics with result and credits_count
     * 7. Sets [isRestoring] = false when complete
     *
     * Note: This method handles mixed success/failure scenarios where some purchases
     * verify successfully and others fail - partial restoration is allowed.
     */
    fun onRestorePurchases() {
        if (_uiState.value.isRestoring) return

        analyticsManager.trackEvent("paywall_restore_purchases_tapped")
        _uiState.update { it.copy(isRestoring = true) }

        viewModelScope.launch {
            try {
                val queryResult = billingRepository.queryUnconsumedPurchases()

                queryResult.onFailure { error ->
                    Log.e(TAG, "Failed to query unconsumed purchases", error)
                    analyticsManager.trackEvent(
                        "purchase_restored",
                        mapOf("result" to "error")
                    )
                    _uiState.update { it.copy(isRestoring = false) }
                    _events.send(PurchaseEvent.RestoreError(error.message ?: "Failed to restore purchases"))
                    return@launch
                }

                val purchases = queryResult.getOrThrow()

                if (purchases.isEmpty()) {
                    analyticsManager.trackEvent(
                        "purchase_restored",
                        mapOf("result" to "none")
                    )
                    _uiState.update { it.copy(isRestoring = false) }
                    _events.send(PurchaseEvent.RestoreNoPurchases)
                    return@launch
                }

                var totalCreditsRestored = 0
                for (purchase in purchases) {
                    val verifyResult = purchaseVerifier.verifyAndConsume(purchase.purchaseToken, purchase.productId)
                    verifyResult.onSuccess { verificationResult ->
                        creditRepository.addCredits(verificationResult.credits)
                        totalCreditsRestored += verificationResult.credits
                    }
                    verifyResult.onFailure { error ->
                        Log.w(TAG, "Failed to verify/consume restored purchase ${purchase.purchaseToken}", error)
                    }
                }

                _uiState.update { it.copy(isRestoring = false) }

                if (totalCreditsRestored > 0) {
                    analyticsManager.trackEvent(
                        "purchase_restored",
                        mapOf("result" to "success", "credits_count" to totalCreditsRestored.toString())
                    )
                    _events.send(PurchaseEvent.RestoreSuccess(totalCreditsRestored))
                } else {
                    analyticsManager.trackEvent(
                        "purchase_restored",
                        mapOf("result" to "error")
                    )
                    _events.send(PurchaseEvent.RestoreError("Failed to restore purchases"))
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during restore", e)
                _uiState.update { it.copy(isRestoring = false) }
                _events.send(PurchaseEvent.RestoreError(e.message ?: "Failed to restore purchases"))
            }
        }
    }

    /**
     * Handles user dismissing the paywall.
     */
    fun onDismiss() {
        viewModelScope.launch {
            try {
                analyticsManager.trackEvent("paywall_dismissed")
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to track paywall dismissed analytics", e)
            }
            _events.send(PurchaseEvent.Dismiss)
        }
    }

    /**
     * Resets the purchase error state, allowing the user to retry.
     */
    fun onPurchaseErrorDismissed() {
        _uiState.update { it.copy(purchaseState = null) }
    }

    /**
     * Dismisses the pending purchase dialog and resets purchase state.
     *
     * The pending token remains in DataStore so the app can check for resolution
     * on the next resume or ViewModel init.
     */
    fun onPendingDismissed() {
        _uiState.update { it.copy(purchaseState = null) }
    }

    /**
     * Checks if a previously pending purchase has been resolved.
     *
     * Called on ViewModel init. If a pending token exists in DataStore, queries
     * the billing service for resolution:
     * - [PendingPurchaseResolution.Purchased]: consume, deliver credits, clear token, emit event
     * - [PendingPurchaseResolution.StillPending]: no-op, token remains for next check
     * - [PendingPurchaseResolution.NotFound]: clear token (purchase was cancelled)
     */
    suspend fun checkPendingPurchaseResolution() {
        val pendingToken = pendingPurchasePrefs.pendingPurchaseToken.first() ?: return
        val pendingProductId = pendingPurchasePrefs.pendingPurchaseProductId.first()
        if (pendingProductId == null) {
            // Mismatched state: token exists but product ID doesn't — clear stale data
            Log.w(TAG, "Pending token exists without product ID, clearing stale pending purchase")
            pendingPurchasePrefs.clearPendingPurchase()
            return
        }

        val resolutionResult = billingRepository.checkPendingPurchases(pendingToken)
        resolutionResult.onFailure { error ->
            Log.w(TAG, "Failed to check pending purchase resolution", error)
            return
        }

        when (val resolution = resolutionResult.getOrThrow()) {
            is PendingPurchaseResolution.Purchased -> {
                val verifyResult = purchaseVerifier.verifyAndConsume(resolution.token, resolution.productId)
                verifyResult.onFailure { error ->
                    Log.e(TAG, "Failed to verify/consume resolved pending purchase", error)
                    return
                }

                val credits = verifyResult.getOrThrow().credits
                val creditsAdded = creditRepository.addCredits(credits)
                if (!creditsAdded) {
                    Log.e(TAG, "Failed to add credits after consuming resolved pending purchase")
                    return
                }
                pendingPurchasePrefs.clearPendingPurchase()

                analyticsManager.trackEvent(
                    "purchase_pending_resolved",
                    mapOf("product_id" to pendingProductId)
                )

                _events.send(PurchaseEvent.PendingPurchaseResolved(credits, pendingProductId))
            }

            is PendingPurchaseResolution.StillPending -> {
                // No-op: purchase still awaiting approval, token remains for next check
            }

            is PendingPurchaseResolution.NotFound -> {
                // Purchase was cancelled by approver — clear stored token
                pendingPurchasePrefs.clearPendingPurchase()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "PurchaseViewModel cleared")
    }
}
