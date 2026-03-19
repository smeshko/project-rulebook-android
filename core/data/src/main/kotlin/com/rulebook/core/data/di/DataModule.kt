package com.rulebook.core.data.di

import androidx.room.withTransaction
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.CreditRepositoryImpl
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.data.repository.GameRepositoryImpl
import com.rulebook.core.data.repository.OnboardingRepository
import com.rulebook.core.data.repository.OnboardingRepositoryImpl
import com.rulebook.core.data.repository.ScanRepository
import com.rulebook.core.data.repository.ScanRepositoryImpl
import com.rulebook.core.database.RulebookDatabase
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.OnboardingPreferencesSource
import com.rulebook.core.datastore.PendingPurchasePreferencesSource
import com.rulebook.core.datastore.RulebookPreferences
import com.rulebook.core.datastore.SortPreferencesSource
import com.rulebook.core.datastore.ThemePreferencesSource
import org.koin.dsl.module

val dataModule = module {
    single<GameRepository> {
        val database = get<RulebookDatabase>()
        GameRepositoryImpl(
            gameDao = get(),
            rulesDao = get(),
            transactionRunner = { block -> database.withTransaction { block() } }
        )
    }
    single<OnboardingPreferencesSource> { get<RulebookPreferences>() }
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
    single<CreditPreferencesSource> { get<RulebookPreferences>() }
    single<CreditRepository> { CreditRepositoryImpl(get()) }
    single<SortPreferencesSource> { get<RulebookPreferences>() }
    single<ThemePreferencesSource> { get<RulebookPreferences>() }
    single<HapticsPreferencesSource> { get<RulebookPreferences>() }
    single<PendingPurchasePreferencesSource> { get<RulebookPreferences>() }
    single<ScanRepository> { ScanRepositoryImpl(api = get(), context = get()) }
}
