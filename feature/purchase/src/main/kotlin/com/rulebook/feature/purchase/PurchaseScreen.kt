package com.rulebook.feature.purchase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.core.model.ProductInfo
import com.rulebook.feature.purchase.components.ProductCardsRow
import org.koin.androidx.compose.koinViewModel

/**
 * PurchaseScreen - The paywall screen showing credit pack options.
 *
 * Collects [PurchaseUiState] from [PurchaseViewModel] and renders [ProductCardsRow].
 * Handles the [PurchaseEvent.Dismiss] event by calling [onDismiss].
 *
 * @param onDismiss Callback invoked when the paywall should be closed (back navigation).
 * @param viewModel The ViewModel managing purchase state and events.
 * @param modifier Modifier to be applied to the screen container.
 */
@Composable
fun PurchaseScreen(
    onDismiss: () -> Unit = {},
    viewModel: PurchaseViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect one-time dismiss events and forward to navigation callback
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PurchaseEvent.Dismiss -> onDismiss()
                is PurchaseEvent.PurchaseSuccess -> onDismiss()
            }
        }
    }

    PurchaseScreenContent(
        uiState = uiState,
        onProductSelected = viewModel::onProductSelected,
        onDismiss = viewModel::onDismiss,
        onRestorePurchases = viewModel::onRestorePurchases,
        modifier = modifier
    )
}

/**
 * Stateless content composable for the Purchase screen.
 *
 * Displays a title, description, [ProductCardsRow] (or skeletons while loading),
 * and action buttons at the bottom.
 *
 * @param uiState The current UI state.
 * @param onProductSelected Callback with productId when a card is tapped.
 * @param onDismiss Callback when the user taps "Not now".
 * @param onRestorePurchases Callback when the user taps "Restore Purchases".
 * @param modifier Modifier to be applied to the container.
 */
@Composable
internal fun PurchaseScreenContent(
    uiState: PurchaseUiState,
    onProductSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onRestorePurchases: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = RulebookTheme.spacing

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.md, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            Text(
                text = "Get Credits",
                style = RulebookTheme.typography.displayLargeTitle,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Scan rulebooks and unlock your games.",
                style = RulebookTheme.typography.callout,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(spacing.sm))

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    style = RulebookTheme.typography.callout,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                ProductCardsRow(
                    products = uiState.products,
                    isLoading = uiState.isLoading,
                    onProductSelected = onProductSelected
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRestorePurchases) {
                    Text(
                        text = "Restore Purchases",
                        style = RulebookTheme.typography.brutalistButtonText
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Not now",
                        style = RulebookTheme.typography.brutalistButtonText
                    )
                }
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Purchase Screen - Loaded Light")
@Composable
private fun PurchaseScreenLoadedLightPreview() {
    RulebookTheme(darkTheme = false) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(
                products = listOf(
                    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
                    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
                    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
                ),
                isLoading = false
            ),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}

@Preview(showBackground = true, name = "Purchase Screen - Loading Light")
@Composable
private fun PurchaseScreenLoadingLightPreview() {
    RulebookTheme(darkTheme = false) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(isLoading = true),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}

@Preview(showBackground = true, name = "Purchase Screen - Loaded Dark")
@Composable
private fun PurchaseScreenLoadedDarkPreview() {
    RulebookTheme(darkTheme = true) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(
                products = listOf(
                    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
                    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
                    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
                ),
                isLoading = false
            ),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}
