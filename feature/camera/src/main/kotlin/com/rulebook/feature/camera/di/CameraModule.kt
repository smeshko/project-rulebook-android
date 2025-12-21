package com.rulebook.feature.camera.di

import com.rulebook.feature.camera.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Camera feature.
 *
 * Provides:
 * - [CameraViewModel] for camera state management
 *
 * Dependencies:
 * - CreditRepository from :core:data for credit balance observation
 * - AnalyticsManager from :core:analytics for event tracking (Story 5.1)
 */
val cameraModule = module {
    viewModel { CameraViewModel(creditRepository = get(), analyticsManager = get()) }
}
