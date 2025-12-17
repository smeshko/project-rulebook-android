package com.rulebook.core.data.di

import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.CreditRepositoryImpl
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.data.repository.GameRepositoryImpl
import com.rulebook.core.data.repository.OnboardingRepository
import com.rulebook.core.data.repository.OnboardingRepositoryImpl
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.OnboardingPreferencesSource
import com.rulebook.core.datastore.RulebookPreferences
import org.koin.dsl.module

val dataModule = module {
    single<GameRepository> { GameRepositoryImpl() }
    single<OnboardingPreferencesSource> { get<RulebookPreferences>() }
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
    single<CreditPreferencesSource> { get<RulebookPreferences>() }
    single<CreditRepository> { CreditRepositoryImpl(get()) }
}
