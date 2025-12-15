package com.rulebook.core.datastore

/**
 * Represents the app's theme mode setting.
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromString(value: String?): ThemeMode {
            return when (value?.uppercase()) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                "SYSTEM" -> SYSTEM
                else -> SYSTEM
            }
        }
    }
}
