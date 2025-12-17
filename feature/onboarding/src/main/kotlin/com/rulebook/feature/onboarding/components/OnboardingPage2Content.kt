package com.rulebook.feature.onboarding.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.onboarding.OnboardingPage

/**
 * Content composable for onboarding page 2 - Getting Started.
 *
 * Displays:
 * - Illustration placeholder with gift icon representing free credits
 * - Bold headline "3 free scans on us" using displayLarge typography
 * - Subtext "Start building your game library today"
 *
 * Uses brutalist styling with secondary container colors for the gift theme.
 *
 * @param modifier Optional modifier for the content.
 */
@Composable
fun OnboardingPage2Content(
    modifier: Modifier = Modifier
) {
    val pageData = OnboardingPage.GettingStarted

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Gift/Credits illustration - uses secondaryContainer for gift theme
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Headline - brutalist bold
        Text(
            text = pageData.headline,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtext
        Text(
            text = pageData.subtext,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Page 2 - Light")
@Composable
private fun OnboardingPage2ContentLightPreview() {
    RulebookTheme(darkTheme = false) {
        OnboardingPage2Content()
    }
}

@Preview(showBackground = true, name = "Page 2 - Dark")
@Composable
private fun OnboardingPage2ContentDarkPreview() {
    RulebookTheme(darkTheme = true) {
        OnboardingPage2Content()
    }
}
