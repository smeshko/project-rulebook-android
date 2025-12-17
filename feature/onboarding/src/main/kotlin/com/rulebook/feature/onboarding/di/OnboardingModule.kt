package com.rulebook.feature.onboarding.di

import com.rulebook.feature.onboarding.OnboardingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val onboardingModule = module {
    viewModel { OnboardingViewModel(onboardingRepository = get()) }
}
