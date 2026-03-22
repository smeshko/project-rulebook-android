package com.rulebook.core.billing.di

import com.rulebook.core.billing.BillingClientWrapper
import com.rulebook.core.billing.BillingClientWrapperImpl
import com.rulebook.core.billing.BillingRepositoryImpl
import com.rulebook.core.billing.PendingPurchaseChecker
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.history.PurchaseHistoryStoreImpl
import com.rulebook.core.billing.pending.PendingValidationStore
import com.rulebook.core.billing.pending.PendingValidationStoreImpl
import com.rulebook.core.billing.reconciliation.BalanceReconciliation
import com.rulebook.core.billing.reconciliation.BalanceReconciliationManager
import com.rulebook.core.billing.recovery.ValidationRecovery
import com.rulebook.core.billing.recovery.ValidationRecoveryManager
import com.rulebook.core.billing.refund.RefundAcknowledgmentStore
import com.rulebook.core.billing.refund.RefundAcknowledgmentStoreImpl
import com.rulebook.core.billing.refund.RefundSync
import com.rulebook.core.billing.refund.RefundSyncManager
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
 * - [ValidationRecoveryManager] is provided as a singleton for app-launch recovery (Story 10.4).
 * - [RefundAcknowledgmentStore] is provided as a singleton for refund acknowledgment (Story 10.5).
 * - [RefundSyncManager] is provided as a singleton for app-launch refund detection (Story 10.5).
 * - [BalanceReconciliationManager] is provided as a singleton for app-launch balance reconciliation (Story 10.7).
 */
val billingModule = module {
    single<BillingClientWrapper> { BillingClientWrapperImpl(androidContext()) }
    single<BillingRepository> { BillingRepositoryImpl(get()) }
    single<PurchaseVerifier> { ServerSidePurchaseVerifier(get()) }
    single<PurchaseHistoryStore> { PurchaseHistoryStoreImpl.create(androidContext()) }
    single<PendingValidationStore> { PendingValidationStoreImpl.create(androidContext()) }
    single { PendingPurchaseChecker(get(), get(), get(), get()) }
    single<ValidationRecovery> { ValidationRecoveryManager(get(), get(), get(), get(), get(), get()) }
    single<RefundAcknowledgmentStore> { RefundAcknowledgmentStoreImpl.create(androidContext()) }
    single<RefundSync> { RefundSyncManager(get(), get(), get(), get(), get(), get()) }
    single<BalanceReconciliation> { BalanceReconciliationManager(get(), get(), get()) }
}
