package com.rulebook.feature.purchase

import android.app.Activity
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.core.model.ProductInfo
import com.rulebook.core.model.PurchaseState
import com.rulebook.feature.purchase.components.AnimatedDots
import com.rulebook.feature.purchase.components.ProductCardsRow
import org.koin.androidx.compose.koinViewModel

/**
 * PurchaseScreen - The paywall screen showing credit pack options.
 *
 * Collects [PurchaseUiState] from [PurchaseViewModel] and renders [ProductCardsRow].
 * Handles the [PurchaseEvent.Dismiss] and [PurchaseEvent.PurchaseSuccess] events by calling [onDismiss].
 * Triggers haptic feedback on purchase success and error states.
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
    val view = LocalView.current
    val context = LocalContext.current
    val activity = context as? Activity
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-time events and forward to navigation callback or snackbar
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PurchaseEvent.Dismiss -> onDismiss()
                is PurchaseEvent.PurchaseSuccess -> onDismiss()
                is PurchaseEvent.RestoreSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = "${event.creditsRestored} credits restored!",
                        duration = SnackbarDuration.Short
                    )
                }
                is PurchaseEvent.RestoreNoPurchases -> {
                    snackbarHostState.showSnackbar(
                        message = "No purchases to restore",
                        duration = SnackbarDuration.Short
                    )
                }
                is PurchaseEvent.RestoreError -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = "Retry",
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onRestorePurchases()
                    }
                }
            }
        }
    }

    // Haptic feedback on purchase state changes
    LaunchedEffect(uiState.purchaseState) {
        when (uiState.purchaseState) {
            is PurchaseState.Success -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                }
            }
            is PurchaseState.Error -> {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            else -> Unit
        }
    }

    PurchaseScreenContent(
        uiState = uiState,
        onProductSelected = { productId ->
            if (activity != null) {
                viewModel.onProductSelected(activity, productId)
            }
        },
        onDismiss = viewModel::onDismiss,
        onRestorePurchases = viewModel::onRestorePurchases,
        onPurchaseErrorDismissed = viewModel::onPurchaseErrorDismissed,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Stateless content composable for the Purchase screen.
 *
 * Displays a title, description, [ProductCardsRow] (or skeletons while loading),
 * action buttons at the bottom, a success animation overlay, and an error dialog.
 *
 * @param uiState The current UI state.
 * @param onProductSelected Callback with productId when a card is tapped.
 * @param onDismiss Callback when the user taps "Not now".
 * @param onRestorePurchases Callback when the user taps "Restore Purchases".
 * @param onPurchaseErrorDismissed Callback when the error dialog is dismissed.
 * @param modifier Modifier to be applied to the container.
 */
@Composable
internal fun PurchaseScreenContent(
    uiState: PurchaseUiState,
    onProductSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onRestorePurchases: () -> Unit,
    onPurchaseErrorDismissed: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val spacing = RulebookTheme.spacing
    val isPurchaseActive = uiState.purchaseState is PurchaseState.Processing

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
                    purchaseState = if (uiState.isRestoring) PurchaseState.Processing("") else uiState.purchaseState,
                    onProductSelected = onProductSelected
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRestorePurchases,
                    enabled = !isPurchaseActive && !uiState.isRestoring
                ) {
                    if (uiState.isRestoring) {
                        AnimatedDots()
                    } else {
                        Text(
                            text = "Restore Purchases",
                            style = RulebookTheme.typography.brutalistButtonText
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    enabled = !isPurchaseActive
                ) {
                    Text(
                        text = "Not now",
                        style = RulebookTheme.typography.brutalistButtonText
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Success animation overlay — green checkmark shown during Success state
        AnimatedVisibility(
            visible = uiState.purchaseState is PurchaseState.Success,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Purchase successful",
                tint = RulebookTheme.colors.green,
                modifier = Modifier.size(72.dp)
            )
        }
    }

    // Error dialog — shown when purchase fails with a non-cancel error
    val errorState = uiState.purchaseState as? PurchaseState.Error
    if (errorState != null) {
        AlertDialog(
            onDismissRequest = onPurchaseErrorDismissed,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    RulebookButton(
                        text = "Dismiss",
                        onClick = onPurchaseErrorDismissed,
                        variant = ButtonVariant.Secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            },
            title = {
                Text(
                    text = "Purchase Failed",
                    style = RulebookTheme.typography.brutalistTitle
                )
            },
            text = {
                Text(
                    text = errorState.message,
                    style = RulebookTheme.typography.body
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RectangleShape,
            modifier = Modifier
                .brutalistShadow(offset = RulebookTheme.spacing.shadowOffsetMedium)
                .brutalistBorder()
        )
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

@Preview(showBackground = true, name = "Purchase Screen - Processing Light")
@Composable
private fun PurchaseScreenProcessingLightPreview() {
    RulebookTheme(darkTheme = false) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(
                products = listOf(
                    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
                    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
                    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
                ),
                isLoading = false,
                purchaseState = PurchaseState.Processing("credits_3")
            ),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}

@Preview(showBackground = true, name = "Purchase Screen - Success Light")
@Composable
private fun PurchaseScreenSuccessLightPreview() {
    RulebookTheme(darkTheme = false) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(
                products = listOf(
                    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
                    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
                    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
                ),
                isLoading = false,
                purchaseState = PurchaseState.Success(3)
            ),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}

@Preview(showBackground = true, name = "Purchase Screen - Restoring Light")
@Composable
private fun PurchaseScreenRestoringLightPreview() {
    RulebookTheme(darkTheme = false) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(
                products = listOf(
                    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
                    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
                    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
                ),
                isLoading = false,
                isRestoring = true
            ),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}

@Preview(showBackground = true, name = "Purchase Screen - Error Light")
@Composable
private fun PurchaseScreenErrorLightPreview() {
    RulebookTheme(darkTheme = false) {
        PurchaseScreenContent(
            uiState = PurchaseUiState(
                products = listOf(
                    ProductInfo("credits_1", "1 Credit", "$0.99", 1),
                    ProductInfo("credits_3", "3 Credits", "$2.49", 3),
                    ProductInfo("credits_10", "10 Credits", "$6.99", 10)
                ),
                isLoading = false,
                purchaseState = PurchaseState.Error("Purchase failed (code: 6)")
            ),
            onProductSelected = {},
            onDismiss = {},
            onRestorePurchases = {}
        )
    }
}
