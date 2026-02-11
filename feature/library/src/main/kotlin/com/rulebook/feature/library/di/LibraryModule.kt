package com.rulebook.feature.library.di

import com.rulebook.feature.library.LibraryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val libraryModule = module {
    viewModel { LibraryViewModel(get(), get()) }
}
