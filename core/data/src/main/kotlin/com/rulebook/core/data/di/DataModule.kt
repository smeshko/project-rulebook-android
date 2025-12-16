package com.rulebook.core.data.di

import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.data.repository.GameRepositoryImpl
import org.koin.dsl.module

val dataModule = module {
    single<GameRepository> { GameRepositoryImpl() }
}
