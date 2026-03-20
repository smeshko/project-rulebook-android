package com.rulebook.core.billing.di

import com.rulebook.core.billing.BillingClientWrapper
import com.rulebook.core.billing.BillingClientWrapperImpl
import com.rulebook.core.billing.BillingRepositoryImpl
import com.rulebook.core.billing.PendingPurchaseChecker
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.history.PurchaseHistoryStoreImpl
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.billing.verification.ServerSidePurchaseVerifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin DI module for billing components.
 *
 * - [BillingClientWrapper] is provided as a singleton tied to Application context.
 * - [BillingRepository] is provided as a singleton delegating to the wrapper.
 * - [PurchaseVerifier] is provided as a singleton using [ServerSidePurchaseVerifier] (Epic 10).
 * - [PurchaseHistoryStore] is provided as a singleton for purchase history persistence.
 * - [PendingPurchaseChecker] is provided as a singleton for app-resume pending checks.
 */
val billingModule = module {
    single<BillingClientWrapper> { BillingClientWrapperImpl(androidContext()) }
    single<BillingRepository> { BillingRepositoryImpl(get()) }
    single<PurchaseVerifier> { ServerSidePurchaseVerifier(get()) }
    single<PurchaseHistoryStore> { PurchaseHistoryStoreImpl.create(androidContext()) }
    single { PendingPurchaseChecker(get(), get(), get(), get()) }
}
