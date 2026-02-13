package com.rulebook.core.billing.di

import com.rulebook.core.billing.BillingRepositoryImpl
import com.rulebook.core.billing.repository.BillingRepository
import org.koin.dsl.module

/**
 * Koin DI module for billing components.
 */
val billingModule = module {
    single<BillingRepository> { BillingRepositoryImpl() }
}
