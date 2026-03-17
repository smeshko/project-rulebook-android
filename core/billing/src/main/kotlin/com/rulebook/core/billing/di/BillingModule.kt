package com.rulebook.core.billing.di

import com.rulebook.core.billing.BillingClientWrapper
import com.rulebook.core.billing.BillingClientWrapperImpl
import com.rulebook.core.billing.BillingRepositoryImpl
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.verification.ClientSidePurchaseVerifier
import com.rulebook.core.billing.verification.PurchaseVerifier
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin DI module for billing components.
 *
 * - [BillingClientWrapper] is provided as a singleton tied to Application context.
 * - [BillingRepository] is provided as a singleton delegating to the wrapper.
 * - [PurchaseVerifier] is provided as a singleton using [ClientSidePurchaseVerifier].
 *   Epic 10 will swap this for a server-side implementation.
 */
val billingModule = module {
    single<BillingClientWrapper> { BillingClientWrapperImpl(androidContext()) }
    single<BillingRepository> { BillingRepositoryImpl(get()) }
    single<PurchaseVerifier> { ClientSidePurchaseVerifier(get()) }
}
