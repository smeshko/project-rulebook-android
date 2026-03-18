package com.rulebook.feature.purchase.di

import com.rulebook.feature.purchase.PurchaseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val purchaseModule = module {
    viewModel { params -> PurchaseViewModel(get(), get(), get(), get(), get(), params.getOrNull<String>() ?: "unknown") }
}
