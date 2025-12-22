package com.rulebook.feature.scan.di

import androidx.lifecycle.SavedStateHandle
import com.rulebook.feature.scan.ScanFlowViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module for the scan feature.
 *
 * Provides:
 * - ScanFlowViewModel with CreditRepository and AnalyticsManager dependencies
 */
val scanFlowModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        ScanFlowViewModel(
            creditRepository = get(),
            analyticsManager = get(),
            savedStateHandle = savedStateHandle
        )
    }
}
