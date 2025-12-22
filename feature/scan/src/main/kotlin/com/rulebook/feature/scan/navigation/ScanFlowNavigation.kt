package com.rulebook.feature.scan.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rulebook.feature.scan.ScanFlowScreen
import com.rulebook.feature.scan.ScanFlowViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Route constant for scan flow navigation.
 */
private const val SCAN_FLOW_ROUTE = "scan_flow/{imageUri}"
private const val ARG_IMAGE_URI = "imageUri"

/**
 * Creates a scan flow route with the provided image URI.
 *
 * @param imageUri The URI string of the captured/selected image.
 * @return The complete route string for navigation.
 */
fun createScanFlowRoute(imageUri: String): String {
    return "scan_flow/$imageUri"
}

/**
 * Adds the scan flow screen to the navigation graph.
 *
 * The scan flow screen orchestrates the entire scan process from credit check
 * through image analysis to rules generation.
 *
 * @param onNavigateToPaywall Callback when user has zero credits, navigates to paywall.
 * @param onProceedToAnalysis Callback when ready to proceed to analysis (Story 5.2+).
 * @param onNavigateBack Callback to navigate back/cancel the scan flow.
 */
fun NavGraphBuilder.scanFlowScreen(
    onNavigateToPaywall: () -> Unit,
    onProceedToAnalysis: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    composable(
        route = SCAN_FLOW_ROUTE,
        arguments = listOf(
            navArgument(ARG_IMAGE_URI) {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val imageUri = backStackEntry.arguments?.getString(ARG_IMAGE_URI) ?: ""

        // Create SavedStateHandle with imageUri
        val savedStateHandle = SavedStateHandle().apply {
            set(ARG_IMAGE_URI, imageUri)
        }

        val viewModel: ScanFlowViewModel = koinViewModel { parametersOf(savedStateHandle) }

        ScanFlowScreen(
            viewModel = viewModel,
            onNavigateToPaywall = onNavigateToPaywall,
            onProceedToAnalysis = onProceedToAnalysis
        )
    }
}

/**
 * Navigates to the scan flow screen with the provided image URI.
 *
 * @param imageUri The URI of the captured or selected image to analyze.
 */
fun NavController.navigateToScanFlow(imageUri: String) {
    navigate(createScanFlowRoute(imageUri))
}
