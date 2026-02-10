package com.rulebook.feature.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.core.model.Game
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import com.rulebook.feature.rules.components.CollapsibleRuleSection
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Rules screen displaying game rules in a clear, organized layout.
 *
 * Shows:
 * - Header with game title and back button
 * - Game thumbnail (if available)
 * - Metadata badges (player count, time, complexity - if available)
 * - Four collapsible sections: Overview, Setup, First Round, Advanced
 *
 * @param gameId The ID of the game whose rules to display.
 * @param onNavigateBack Callback invoked when the back button is clicked.
 * @param viewModel The ViewModel managing rules state.
 * @param modifier Modifier to be applied to the screen.
 */
@Composable
fun RulesScreen(
    gameId: String,
    onNavigateBack: () -> Unit,
    viewModel: RulesViewModel = koinViewModel { parametersOf(gameId) },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RulesScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::retry,
        onSetupItemToggle = viewModel::toggleSetupItem,
        modifier = modifier
    )
}

@Composable
internal fun RulesScreenContent(
    uiState: RulesUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onSetupItemToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        RulebookHeaderBar(
            title = uiState.game?.title ?: "Rules",
            onBackClick = onNavigateBack
        )

        when {
            uiState.isLoading -> RulesLoadingState()
            uiState.error != null -> RulesErrorState(
                message = uiState.error,
                onRetry = onRetry
            )
            uiState.isSuccess -> RulesSuccessState(
                game = uiState.game!!,
                rules = uiState.rules!!,
                setupCheckedItems = uiState.setupCheckedItems,
                onSetupItemToggle = onSetupItemToggle
            )
        }
    }
}

/**
 * Loading state displayed while fetching rules data.
 */
@Composable
private fun RulesLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error state displayed when loading rules fails.
 * Shows an error message with a retry button.
 */
@Composable
private fun RulesErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        RulebookCard(
            modifier = Modifier.padding(RulebookTheme.spacing.md)
        ) {
            Column(
                modifier = Modifier.padding(RulebookTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Something went wrong",
                    style = RulebookTheme.typography.displayTitle2,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))

                Text(
                    text = message,
                    style = RulebookTheme.typography.body,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(RulebookTheme.spacing.lg))

                RulebookButton(
                    text = "Try Again",
                    onClick = onRetry,
                    variant = ButtonVariant.Primary
                )
            }
        }
    }
}

/**
 * Success state displaying the rules content.
 * Shows game thumbnail, metadata (if available), and four collapsible rule sections.
 */
@Composable
private fun RulesSuccessState(
    game: Game,
    rules: Rules,
    setupCheckedItems: Set<Int>,
    onSetupItemToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track which sections are expanded (default: first section expanded)
    val expandedSections = remember {
        mutableStateMapOf<String, Boolean>(
            "overview" to true,
            "setup" to false,
            "firstRound" to false,
            "advanced" to false
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = RulebookTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(RulebookTheme.spacing.md)
    ) {
        // Game thumbnail (if available)
        item {
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
            if (game.thumbnailUrl != null) {
                AsyncImage(
                    model = game.thumbnailUrl,
                    contentDescription = "${game.title} thumbnail",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Metadata badges (currently hidden since Game model doesn't have these fields)
        // TODO: Add metadata badges when Game model includes player count, time, complexity

        // Section spacing
        item {
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.lg))
        }

        // Overview section
        item {
            CollapsibleRuleSection(
                title = rules.overview.title.ifBlank { "Overview" },
                content = rules.overview.content,
                items = rules.overview.items,
                accentColor = RulebookTheme.colors.orange,
                isExpanded = expandedSections["overview"] ?: false,
                onToggle = {
                    expandedSections["overview"] = !(expandedSections["overview"] ?: false)
                },
                winCondition = rules.overview.winCondition
            )
        }

        // Setup section
        item {
            CollapsibleRuleSection(
                title = rules.setup.title.ifBlank { "Setup" },
                content = rules.setup.content,
                items = rules.setup.items,
                accentColor = RulebookTheme.colors.blue,
                isExpanded = expandedSections["setup"] ?: false,
                onToggle = {
                    expandedSections["setup"] = !(expandedSections["setup"] ?: false)
                },
                checkedItems = setupCheckedItems,
                onItemToggle = onSetupItemToggle
            )
        }

        // First Round section
        item {
            CollapsibleRuleSection(
                title = rules.firstRound.title.ifBlank { "First Round" },
                content = rules.firstRound.content,
                items = rules.firstRound.items,
                accentColor = RulebookTheme.colors.yellow,
                isExpanded = expandedSections["firstRound"] ?: false,
                onToggle = {
                    expandedSections["firstRound"] = !(expandedSections["firstRound"] ?: false)
                }
            )
        }

        // Advanced section
        item {
            CollapsibleRuleSection(
                title = rules.advanced.title.ifBlank { "Advanced" },
                content = rules.advanced.content,
                items = rules.advanced.items,
                accentColor = RulebookTheme.colors.purple,
                isExpanded = expandedSections["advanced"] ?: false,
                onToggle = {
                    expandedSections["advanced"] = !(expandedSections["advanced"] ?: false)
                }
            )
        }

        // Bottom padding
        item {
            Spacer(modifier = Modifier.height(RulebookTheme.spacing.md))
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Loading State - Light")
@Composable
private fun RulesScreenLoadingLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulesScreenContent(
            uiState = RulesUiState(isLoading = true),
            onNavigateBack = {},
            onRetry = {},
            onSetupItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State - Dark")
@Composable
private fun RulesScreenLoadingDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulesScreenContent(
            uiState = RulesUiState(isLoading = true),
            onNavigateBack = {},
            onRetry = {},
            onSetupItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Light")
@Composable
private fun RulesScreenErrorLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulesScreenContent(
            uiState = RulesUiState(
                isLoading = false,
                error = "Failed to load rules. Please check your connection."
            ),
            onNavigateBack = {},
            onRetry = {},
            onSetupItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Dark")
@Composable
private fun RulesScreenErrorDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulesScreenContent(
            uiState = RulesUiState(
                isLoading = false,
                error = "Failed to load rules. Please check your connection."
            ),
            onNavigateBack = {},
            onRetry = {},
            onSetupItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State - Light")
@Composable
private fun RulesScreenSuccessLightPreview() {
    val testGame = Game(
        id = "game-1",
        title = "Catan",
        thumbnailUrl = null,
        createdAt = 1000L,
        lastAccessedAt = 2000L
    )

    val testRules = Rules(
        gameId = "game-1",
        overview = RuleSection(
            title = "Overview",
            content = "Catan is a multiplayer board game about settling an island.",
            items = null,
            winCondition = "Be the first player to collect 10 victory points."
        ),
        setup = RuleSection(
            title = "Setup",
            content = "Follow these steps to set up the game:",
            items = listOf(
                "Shuffle the terrain hexes and arrange them randomly",
                "Place number tokens on each hex",
                "Deal two settlements and two roads to each player"
            )
        ),
        firstRound = RuleSection(
            title = "First Round",
            content = "The first round begins with these special rules.",
            items = null
        ),
        advanced = RuleSection(
            title = "Advanced",
            content = "Advanced rules for experienced players.",
            items = null
        )
    )

    RulebookTheme(darkTheme = false) {
        RulesScreenContent(
            uiState = RulesUiState(
                game = testGame,
                rules = testRules,
                isLoading = false,
                error = null,
                setupCheckedItems = setOf(0, 2)
            ),
            onNavigateBack = {},
            onRetry = {},
            onSetupItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Success State - Dark")
@Composable
private fun RulesScreenSuccessDarkPreview() {
    val testGame = Game(
        id = "game-1",
        title = "Catan",
        thumbnailUrl = null,
        createdAt = 1000L,
        lastAccessedAt = 2000L
    )

    val testRules = Rules(
        gameId = "game-1",
        overview = RuleSection(
            title = "Overview",
            content = "Catan is a multiplayer board game about settling an island.",
            items = null,
            winCondition = "Be the first player to collect 10 victory points."
        ),
        setup = RuleSection(
            title = "Setup",
            content = "Follow these steps to set up the game:",
            items = listOf(
                "Shuffle the terrain hexes and arrange them randomly",
                "Place number tokens on each hex",
                "Deal two settlements and two roads to each player"
            )
        ),
        firstRound = RuleSection(
            title = "First Round",
            content = "The first round begins with these special rules.",
            items = null
        ),
        advanced = RuleSection(
            title = "Advanced",
            content = "Advanced rules for experienced players.",
            items = null
        )
    )

    RulebookTheme(darkTheme = true) {
        RulesScreenContent(
            uiState = RulesUiState(
                game = testGame,
                rules = testRules,
                isLoading = false,
                error = null,
                setupCheckedItems = setOf(1)
            ),
            onNavigateBack = {},
            onRetry = {},
            onSetupItemToggle = {}
        )
    }
}
