package com.rulebook.core.billing

import android.app.Activity
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stub implementation of BillingRepository.
 *
 * This is a temporary implementation for Story 8.3 that returns empty data.
 * The actual Google Play Billing integration will be implemented in Story 8.4.
 */
class BillingRepositoryImpl : BillingRepository {

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    override val products: Flow<List<ProductInfo>> = _products.asStateFlow()

    override suspend fun queryProducts(): Result<List<ProductInfo>> {
        // Stub: returns empty list until Story 8.4 implements actual billing
        return Result.success(emptyList())
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> {
        // Stub: will be implemented in Story 8.4
        return Result.failure(NotImplementedError("Purchase flow not yet implemented"))
    }

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        // Stub: will be implemented in Story 8.4
        return Result.failure(NotImplementedError("Purchase consumption not yet implemented"))
    }

    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> {
        // Stub: returns empty list until Story 8.4
        return Result.success(emptyList())
    }
}
