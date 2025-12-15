package com.rulebook.core.datastore.di

import com.rulebook.core.datastore.RulebookPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataStoreModule = module {
    single { RulebookPreferences(androidContext()) }
}
