package com.rulebook.feature.onboarding

/**
 * Represents the pages in the onboarding flow.
 *
 * Each page contains its content (headline and subtext) that will be displayed
 * in the HorizontalPager.
 *
 * @property headline The bold headline text for the page.
 * @property subtext The explanatory subtext for the page.
 */
enum class OnboardingPage(
    val headline: String,
    val subtext: String
) {
    /**
     * First onboarding page - Value Proposition.
     * Explains what the app does (scanning game boxes for rules).
     */
    ValueProposition(
        headline = "Scan any game box",
        subtext = "Point your camera at a board game and get the rules instantly"
    ),

    /**
     * Second onboarding page - Getting Started.
     * Explains the free credits gift and how to start building the game library.
     */
    GettingStarted(
        headline = "3 free scans on us",
        subtext = "Start building your game library today"
    )
}
