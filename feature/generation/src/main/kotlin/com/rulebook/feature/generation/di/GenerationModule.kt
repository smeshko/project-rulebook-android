package com.rulebook.feature.generation.di

import com.rulebook.feature.generation.GenerationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Generation feature.
 *
 * Provides:
 * - [GenerationViewModel] for scan progress state management
 *
 * Dependencies:
 * - SavedStateHandle (auto-injected by Koin for navigation arguments)
 * - AnalyticsManager (from :core:analytics) for tracking events
 */
val generationModule = module {
    viewModel { GenerationViewModel(savedStateHandle = get(), analyticsManager = get()) }
}
