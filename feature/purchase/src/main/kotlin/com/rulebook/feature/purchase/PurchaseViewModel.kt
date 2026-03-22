package com.rulebook.feature.purchase

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.BillingResponseCode
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.pending.PendingValidation
import com.rulebook.core.billing.pending.PendingValidationStore
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.verification.PurchaseValidationException
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.billing.verification.VerificationStatus
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

/** Maximum number of retry attempts after the initial validation failure. */
private const val MAX_VALIDATION_RETRY_ATTEMPTS = 3

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
 * - Server-side receipt validation (Story 10.2)
 * - Restore purchases
 * - Dismiss action
 *
 * @param creditRepository Repository for observing and modifying credit balance.
 * @param billingRepository Repository for billing operations and purchase updates.
 * @param analyticsManager Manager for tracking analytics events.
 * @param purchaseVerifier Verifier that validates the purchase server-side and resolves credits.
 * @param pendingPurchasePrefs DataStore preferences for pending purchase token storage.
 * @param purchaseHistoryStore Store for persisting validated purchase tokens (Story 10.5 prerequisite).
 * @param pendingValidationStore Store for persisting failed validations pending retry (Story 10.3).
 * @param source The navigation source that triggered the paywall (e.g., "scan_gate", "settings").
 */
class PurchaseViewModel(
    private val creditRepository: CreditRepository,
    private val billingRepository: BillingRepository,
    private val analyticsManager: AnalyticsManager,
    private val purchaseVerifier: PurchaseVerifier,
    private val pendingPurchasePrefs: PendingPurchasePreferencesSource,
    private val purchaseHistoryStore: PurchaseHistoryStore,
    private val pendingValidationStore: PendingValidationStore,
    private val source: String = "unknown"
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

        // Track paywall_displayed after loading the initial credit balance
        viewModelScope.launch {
            val currentBalance = creditRepository.creditBalance.catch { emit(0) }.first()
            analyticsManager.trackPaywallDisplayed(source, currentBalance)
        }

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
        val credits = _uiState.value.products.find { it.productId == productId }?.credits ?: 0
        analyticsManager.trackPurchaseStarted(sku = productId, credits = credits)
        _uiState.update { it.copy(purchaseState = PurchaseState.Processing(productId)) }

        if (activity == null) {
            Log.w(TAG, "onProductSelected called with null activity — billing flow not launched")
            return
        }

        viewModelScope.launch {
            val result = billingRepository.launchPurchaseFlow(activity, productId)
            result.onFailure { error ->
                Log.e(TAG, "Failed to launch purchase flow for $productId", error)
                analyticsManager.trackPurchaseFailed(
                    sku = productId,
                    errorCode = "LAUNCH_FAILED",
                    errorMessage = error.message ?: "Failed to start purchase"
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
     *
     * After receiving a valid purchase token, transitions to [PurchaseState.Validating]
     * and calls the server-side verifier. Credits are only delivered after the server
     * confirms the purchase is valid.
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
                    analyticsManager.trackPurchaseFailed(
                        sku = productId,
                        errorCode = "TOKEN_MISSING",
                        errorMessage = "Purchase token missing"
                    )
                    _uiState.update {
                        it.copy(purchaseState = PurchaseState.Error("Purchase token missing"))
                    }
                    return
                }

                // Transition to Validating state — server validation in progress
                _uiState.update { it.copy(purchaseState = PurchaseState.Validating(productId)) }

                viewModelScope.launch {
                    // Attempt validation with exponential backoff (2s, 4s, 8s) for transient errors.
                    // PurchaseValidationException (server-side INVALID) is not retried.
                    var lastTransientError: Exception? = null

                    for (attempt in 0..MAX_VALIDATION_RETRY_ATTEMPTS) {
                        if (attempt > 0) {
                            // Delays: retry 1 = 2s, retry 2 = 4s, retry 3 = 8s
                            delay(2000L * (1L shl (attempt - 1)))
                        }

                        val verifyResult = purchaseVerifier.verifyAndConsume(purchaseToken, productId)

                        if (verifyResult.isSuccess) {
                            val verification = verifyResult.getOrThrow()
                            val credits = verification.credits
                            val analyticsStatus = when (verification.status) {
                                VerificationStatus.VALID -> "valid"
                                VerificationStatus.ALREADY_PROCESSED -> "already_processed"
                            }

                            // For ALREADY_PROCESSED, skip credit delivery if already delivered locally
                            val alreadyDelivered = verification.status == VerificationStatus.ALREADY_PROCESSED &&
                                purchaseHistoryStore.getRecentTokens().contains(purchaseToken)

                            if (!alreadyDelivered) {
                                // Deliver credits only after successful server validation
                                val creditsSaved = creditRepository.addCredits(credits)
                                if (!creditsSaved) {
                                    analyticsManager.trackPurchaseFailed(
                                        sku = productId,
                                        errorCode = "CREDIT_SAVE_FAILED",
                                        errorMessage = "Credits could not be saved after successful purchase"
                                    )
                                    _uiState.update {
                                        it.copy(purchaseState = PurchaseState.Error("Failed to save credits. Please restore purchases."))
                                    }
                                    return@launch
                                }

                                // Save to history store AFTER credits delivered (Story 10.5)
                                purchaseHistoryStore.savePurchase(purchaseToken, productId)
                            }

                            val newBalance = creditRepository.creditBalance.first()

                            analyticsManager.trackPurchaseValidated(sku = productId, status = analyticsStatus)
                            analyticsManager.trackPurchaseCompleted(
                                sku = productId,
                                creditsAdded = credits,
                                newBalance = newBalance
                            )

                            _uiState.update { it.copy(purchaseState = PurchaseState.Success(credits)) }

                            // Auto-dismiss after success animation plays (1.5s)
                            delay(1500)
                            _events.send(PurchaseEvent.PurchaseSuccess(credits))
                            return@launch
                        }

                        val error = verifyResult.exceptionOrNull()!!
                        if (error is PurchaseValidationException) {
                            // Server explicitly rejected this purchase — do NOT retry
                            Log.e(TAG, "Purchase explicitly invalid for $productId", error)
                            analyticsManager.trackPurchaseValidated(sku = productId, status = "invalid")
                            analyticsManager.trackPurchaseFailed(
                                sku = productId,
                                errorCode = "VALIDATION_INVALID",
                                errorMessage = error.message ?: "Purchase verification failed"
                            )
                            _uiState.update {
                                it.copy(purchaseState = PurchaseState.Error("Purchase verification failed"))
                            }
                            return@launch
                        }

                        // Transient network/timeout error — retry with backoff
                        lastTransientError = error
                        Log.w(TAG, "Transient validation error on attempt $attempt for $productId, will retry", error)
                    }

                    // All retry attempts exhausted — save to pending queue for Story 10.4 recovery
                    Log.e(TAG, "All validation retries exhausted for $productId, saving to pending queue", lastTransientError)
                    analyticsManager.trackPurchaseValidated(sku = productId, status = "error")
                    analyticsManager.trackPurchaseFailed(
                        sku = productId,
                        errorCode = "CONSUME_FAILED",
                        errorMessage = lastTransientError?.message ?: "Purchase verification failed"
                    )
                    pendingValidationStore.save(
                        PendingValidation(
                            purchaseToken = purchaseToken,
                            productId = productId,
                            timestamp = System.currentTimeMillis(),
                            retryCount = MAX_VALIDATION_RETRY_ATTEMPTS
                        )
                    )
                    _events.send(PurchaseEvent.ValidationPending)
                    _uiState.update { it.copy(purchaseState = null) }
                }
            }

            BillingResponseCode.USER_CANCELED -> {
                analyticsManager.trackPurchaseFailed(
                    sku = productId,
                    errorCode = "USER_CANCELED",
                    errorMessage = "Purchase cancelled"
                )
                // Silent reset — no error shown to user
                _uiState.update { it.copy(purchaseState = null) }
            }

            else -> {
                analyticsManager.trackPurchaseFailed(
                    sku = productId,
                    errorCode = update.responseCode.toString(),
                    errorMessage = "Purchase failed (code: ${update.responseCode})"
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
     *    - Calls [PurchaseVerifier.verifyAndConsume] to verify the purchase server-side
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
                    analyticsManager.trackPurchaseRestored(result = "error", creditsRestored = 0)
                    _uiState.update { it.copy(isRestoring = false) }
                    _events.send(PurchaseEvent.RestoreError(error.message ?: "Failed to restore purchases"))
                    return@launch
                }

                val purchases = queryResult.getOrThrow()

                if (purchases.isEmpty()) {
                    analyticsManager.trackPurchaseRestored(result = "none", creditsRestored = 0)
                    _uiState.update { it.copy(isRestoring = false) }
                    _events.send(PurchaseEvent.RestoreNoPurchases)
                    return@launch
                }

                var totalCreditsRestored = 0
                for (purchase in purchases) {
                    val verifyResult = purchaseVerifier.verifyAndConsume(purchase.purchaseToken, purchase.productId)
                    verifyResult.onSuccess { verification ->
                        val status = when (verification.status) {
                            VerificationStatus.VALID -> "valid"
                            VerificationStatus.ALREADY_PROCESSED -> "already_processed"
                        }
                        analyticsManager.trackPurchaseValidated(sku = purchase.productId, status = status)

                        val alreadyDelivered = verification.status == VerificationStatus.ALREADY_PROCESSED &&
                            purchaseHistoryStore.getRecentTokens().contains(purchase.purchaseToken)

                        if (!alreadyDelivered) {
                            creditRepository.addCredits(verification.credits)
                            totalCreditsRestored += verification.credits
                            purchaseHistoryStore.savePurchase(purchase.purchaseToken, purchase.productId)
                        }
                    }
                    verifyResult.onFailure { error ->
                        Log.w(TAG, "Failed to verify/consume restored purchase ${purchase.purchaseToken}", error)
                        val status = if (error is PurchaseValidationException) "invalid" else "error"
                        analyticsManager.trackPurchaseValidated(sku = purchase.productId, status = status)
                    }
                }

                _uiState.update { it.copy(isRestoring = false) }

                if (totalCreditsRestored > 0) {
                    analyticsManager.trackPurchaseRestored(result = "success", creditsRestored = totalCreditsRestored)
                    _events.send(PurchaseEvent.RestoreSuccess(totalCreditsRestored))
                } else {
                    analyticsManager.trackPurchaseRestored(result = "error", creditsRestored = 0)
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
     * - [PendingPurchaseResolution.Purchased]: validate server-side, deliver credits, clear token, emit event
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
                    val status = if (error is PurchaseValidationException) "invalid" else "error"
                    analyticsManager.trackPurchaseValidated(sku = resolution.productId, status = status)
                    return
                }

                val verification = verifyResult.getOrThrow()
                val credits = verification.credits
                val analyticsStatus = when (verification.status) {
                    VerificationStatus.VALID -> "valid"
                    VerificationStatus.ALREADY_PROCESSED -> "already_processed"
                }

                // For ALREADY_PROCESSED, skip credit delivery if already delivered locally
                val alreadyDelivered = verification.status == VerificationStatus.ALREADY_PROCESSED &&
                    purchaseHistoryStore.getRecentTokens().contains(resolution.token)

                if (!alreadyDelivered) {
                    val creditsAdded = creditRepository.addCredits(credits)
                    if (!creditsAdded) {
                        Log.e(TAG, "Failed to add credits after consuming resolved pending purchase")
                        return
                    }

                    // Save to history store AFTER credits delivered (Story 10.5)
                    purchaseHistoryStore.savePurchase(resolution.token, resolution.productId)
                }

                pendingPurchasePrefs.clearPendingPurchase()

                analyticsManager.trackPurchaseValidated(sku = resolution.productId, status = analyticsStatus)
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
