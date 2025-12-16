package com.rulebook.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.BrutalistBorderWidth
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Bottom navigation bar for the Rulebook app with brutalist styling.
 *
 * Displays Library and Settings tabs with proper selection highlighting
 * and navigation callbacks. Implements the brutalist design aesthetic
 * with a thick top border and flat appearance.
 *
 * @param currentRoute The currently active route for highlighting the selected tab
 * @param onNavigate Callback invoked when a tab is tapped, receiving the destination route
 * @param modifier Optional modifier for the navigation bar
 */
@Composable
fun RulebookBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.onSurface

    NavigationBar(
        modifier = modifier
            .drawBehind {
                // Thick top border (brutalist style)
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = BrutalistBorderWidth.toPx()
                )
            },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomBarDestination.items.forEach { destination ->
            val selected = currentRoute == destination.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RulebookTheme.colors.pink,
                    selectedTextColor = RulebookTheme.colors.pink,
                    indicatorColor = RulebookTheme.colors.pink.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
