package com.rulebook.feature.library.di

import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.SortPreferencesSource
import com.rulebook.feature.library.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryModule = module {
    viewModel {
        LibraryViewModel(
            get(),
            get<SortPreferencesSource>(),
            get<CreditPreferencesSource>()
        )
    }
}
