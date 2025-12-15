package com.rulebook.core.datastore

/**
 * Represents the app's theme mode setting.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        /**
         * Parses a string value to [ThemeMode].
         * Handles case-insensitivity and trims whitespace.
         * Returns [SYSTEM] for null, empty, or unrecognized values.
         */
        fun fromString(value: String?): ThemeMode {
            return when (value?.trim()?.uppercase()) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                "SYSTEM" -> SYSTEM
                else -> SYSTEM
            }
        }
    }
}
