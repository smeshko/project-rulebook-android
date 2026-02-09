package com.rulebook.feature.rules.di

import com.rulebook.feature.rules.RulesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for the Rules feature.
 *
 * Provides:
 * - [RulesViewModel] for rules screen state management (with gameId parameter)
 *
 * Dependencies:
 * - GameRepository (from :core:data) for loading game and rules data
 */
val rulesModule = module {
    viewModel { (gameId: String) ->
        RulesViewModel(
            gameId = gameId,
            gameRepository = get()
        )
    }
}
