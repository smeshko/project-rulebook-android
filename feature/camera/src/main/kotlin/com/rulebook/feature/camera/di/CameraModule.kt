package com.rulebook.feature.camera.di

import com.rulebook.feature.camera.CameraViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Camera feature.
 *
 * Provides:
 * - [CameraViewModel] for camera state management
 */
val cameraModule = module {
    viewModel { CameraViewModel() }
}
