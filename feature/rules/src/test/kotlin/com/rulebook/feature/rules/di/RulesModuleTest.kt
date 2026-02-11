package com.rulebook.feature.rules.di

import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.model.Game
import com.rulebook.core.model.Rules
import com.rulebook.feature.rules.RulesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

/**
 * Tests for RulesModule Koin configuration.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RulesModuleTest : KoinTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun `rulesModule provides RulesViewModel with gameId parameter`() {
        // Setup Koin with test dependencies
        startKoin {
            modules(
                rulesModule,
                module {
                    single<GameRepository> { FakeGameRepository() }
                }
            )
        }

        // Inject ViewModel with gameId parameter
        val viewModel: RulesViewModel by inject { parametersOf("test-game-id") }

        // Verify ViewModel was created
        assertNotNull(viewModel)
    }
}

/**
 * Fake GameRepository for testing DI setup.
 */
private class FakeGameRepository : GameRepository {
    override suspend fun getGames(): Result<List<Game>> = Result.Error("Not implemented")
    override fun getGamesSorted(sortOrder: com.rulebook.core.model.SortOrder): kotlinx.coroutines.flow.Flow<List<Game>> {
        throw NotImplementedError()
    }
    override suspend fun getGameById(id: String): Result<Game> = Result.Error("Not implemented")
    override suspend fun saveGame(game: Game): Result<Unit> = Result.Error("Not implemented")
    override suspend fun deleteGame(id: String): Result<Unit> = Result.Error("Not implemented")
    override suspend fun getRulesForGame(gameId: String): Result<Rules> = Result.Error("Not implemented")
    override suspend fun saveGameWithRules(game: Game, rules: Rules, rawJson: String): Result<String> = Result.Error("Not implemented")
    override suspend fun updateLastAccessed(gameId: String): Result<Unit> = Result.Success(Unit)
}
