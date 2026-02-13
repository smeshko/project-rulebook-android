package com.rulebook.feature.purchase

import com.rulebook.core.model.ProductInfo
import com.rulebook.core.model.PurchaseState

/**
 * UI state for the Purchase (Paywall) screen.
 *
 * @property products List of available in-app products (credit packs).
 * @property currentBalance User's current credit balance.
 * @property isLoading Whether products are being loaded from the billing service.
 * @property purchaseState Current state of any active purchase operation.
 * @property error Error message to display to the user, if any.
 */
data class PurchaseUiState(
    val products: List<ProductInfo> = emptyList(),
    val currentBalance: Int = 0,
    val isLoading: Boolean = true,
    val purchaseState: PurchaseState? = null,
    val error: String? = null
)
