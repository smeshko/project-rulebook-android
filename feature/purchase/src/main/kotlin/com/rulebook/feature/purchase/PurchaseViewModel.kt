package com.rulebook.feature.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.data.repository.CreditRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
 * - Handling purchase intent (stub for Story 8.3, real impl in Story 8.4)
 * - Restore purchases
 * - Dismiss action
 *
 * @param creditRepository Repository for observing credit balance.
 * @param billingRepository Repository for billing operations.
 * @param analyticsManager Manager for tracking analytics events.
 */
class PurchaseViewModel(
    private val creditRepository: CreditRepository,
    private val billingRepository: BillingRepository,
    private val analyticsManager: AnalyticsManager
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
                _uiState.update { it.copy(products = products, isLoading = products.isEmpty()) }
            }
            .launchIn(viewModelScope)

        // Query products from Play Store
        queryProducts()
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
     * This is a stub for Story 8.3. The actual purchase flow will be
     * implemented in Story 8.4.
     *
     * @param productId The product identifier (SKU) to purchase.
     */
    fun onProductSelected(productId: String) {
        Log.d(TAG, "Product selected: $productId (stub - not implemented)")
        analyticsManager.trackEvent("paywall_product_tapped", mapOf("product_id" to productId))
        // Story 8.4 will implement the actual purchase flow
    }

    /**
     * Handles user tapping the "Restore Purchases" button.
     *
     * This is a stub for Story 8.3. The actual restore logic will be
     * implemented in Story 8.4.
     */
    fun onRestorePurchases() {
        Log.d(TAG, "Restore purchases tapped (stub - not implemented)")
        analyticsManager.trackEvent("paywall_restore_purchases_tapped")
        // Story 8.4 will implement the actual restore logic
    }

    /**
     * Handles user dismissing the paywall.
     */
    fun onDismiss() {
        viewModelScope.launch {
            analyticsManager.trackEvent("paywall_dismissed")
            _events.send(PurchaseEvent.Dismiss)
        }
    }
}
