package com.rulebook.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchasesUpdatedListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service for handling Google Play Billing.
 */
class BillingService(context: Context) {

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        // TODO: Handle purchase updates
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _connectionState.value = true
                    // TODO: Query products
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = false
            }
        })
    }

    fun disconnect() {
        billingClient.endConnection()
    }

    suspend fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        // TODO: Implement billing flow
    }
}
