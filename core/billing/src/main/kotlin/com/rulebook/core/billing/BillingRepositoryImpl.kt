package com.rulebook.core.billing

import android.app.Activity
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Real implementation of [BillingRepository] backed by [BillingClientWrapper].
 *
 * Delegates all billing operations to the wrapper and caches the product list
 * in a [MutableStateFlow] for reactive UI observation.
 *
 * Ensures a BillingClient connection is established before each operation.
 * Retries once on SERVICE_DISCONNECTED (connection drops mid-operation).
 */
class BillingRepositoryImpl(
    private val wrapper: BillingClientWrapper
) : BillingRepository {

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    override val products: Flow<List<ProductInfo>> = _products.asStateFlow()

    override suspend fun queryProducts(): Result<List<ProductInfo>> {
        val connectResult = wrapper.ensureConnected()
        if (connectResult.isFailure) {
            return Result.failure(connectResult.exceptionOrNull()!!)
        }

        val result = wrapper.queryProducts()
        if (result.isSuccess) {
            _products.value = result.getOrDefault(emptyList())
        }
        return result
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> {
        val connectResult = wrapper.ensureConnected()
        if (connectResult.isFailure) {
            return Result.failure(connectResult.exceptionOrNull()!!)
        }
        return wrapper.launchBillingFlow(activity, productId)
    }

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        val connectResult = wrapper.ensureConnected()
        if (connectResult.isFailure) {
            return Result.failure(connectResult.exceptionOrNull()!!)
        }

        val result = wrapper.consumePurchase(purchaseToken)
        // Retry once if disconnected mid-operation
        if (result.isFailure && !wrapper.connectionState.value) {
            val reconnectResult = wrapper.ensureConnected()
            if (reconnectResult.isFailure) {
                return Result.failure(reconnectResult.exceptionOrNull()!!)
            }
            return wrapper.consumePurchase(purchaseToken)
        }
        return result
    }

    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> {
        val connectResult = wrapper.ensureConnected()
        if (connectResult.isFailure) {
            return Result.failure(connectResult.exceptionOrNull()!!)
        }

        val result = wrapper.queryPurchases()
        // Retry once if disconnected mid-operation
        if (result.isFailure && !wrapper.connectionState.value) {
            val reconnectResult = wrapper.ensureConnected()
            if (reconnectResult.isFailure) {
                return Result.failure(reconnectResult.exceptionOrNull()!!)
            }
            return wrapper.queryPurchases()
        }
        return result
    }
}
