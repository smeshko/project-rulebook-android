package com.rulebook.navigation

/**
 * Configuration for deep links in the Rulebook app.
 *
 * Deep Link Scheme Documentation:
 * ================================
 *
 * The app uses the custom URI scheme "rulebook://" for deep linking.
 *
 * Supported Deep Links:
 * ---------------------
 *
 * 1. Rules Screen:
 *    - Pattern: rulebook://rules/{gameId}
 *    - Example: rulebook://rules/chess-classic
 *    - Opens the rules viewer for the specified game
 *
 * Future Deep Links (to be implemented):
 * --------------------------------------
 *
 * 2. Library Screen:
 *    - Pattern: rulebook://library
 *    - Opens the main library screen
 *
 * 3. Camera Screen:
 *    - Pattern: rulebook://camera
 *    - Opens the camera for capturing rulebook pages
 *
 * 4. Settings Screen:
 *    - Pattern: rulebook://settings
 *    - Opens the app settings
 *
 * Android Manifest Configuration:
 * -------------------------------
 * To enable deep links, add the following intent filter to MainActivity:
 *
 * ```xml
 * <intent-filter>
 *     <action android:name="android.intent.action.VIEW" />
 *     <category android:name="android.intent.category.DEFAULT" />
 *     <category android:name="android.intent.category.BROWSABLE" />
 *     <data android:scheme="rulebook" />
 * </intent-filter>
 * ```
 *
 * Testing Deep Links:
 * -------------------
 * Use adb to test deep links:
 * ```
 * adb shell am start -W -a android.intent.action.VIEW -d "rulebook://rules/chess-classic"
 * ```
 */
object DeepLinkConfig {
    /**
     * The custom URI scheme for the app.
     * All deep links use this scheme prefix: rulebook://
     */
    const val SCHEME = "rulebook"

    /**
     * Host path for rules deep links.
     * Full pattern: rulebook://rules/{gameId}
     */
    const val HOST_RULES = "rules"

    /**
     * Host path for library deep links (future use).
     * Full pattern: rulebook://library
     */
    const val HOST_LIBRARY = "library"

    /**
     * Host path for camera deep links (future use).
     * Full pattern: rulebook://camera
     */
    const val HOST_CAMERA = "camera"

    /**
     * Host path for settings deep links (future use).
     * Full pattern: rulebook://settings
     */
    const val HOST_SETTINGS = "settings"
}
