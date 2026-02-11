package com.rulebook.core.model

/**
 * Represents the available sort orders for the game library.
 *
 * @property displayName The human-readable name shown in the UI
 */
enum class SortOrder(val displayName: String) {
    /**
     * Sort by last accessed time (most recent first) - Default sort order
     */
    RECENT("Recent"),

    /**
     * Sort alphabetically by game title (A-Z)
     */
    ALPHABETICAL("A-Z"),

    /**
     * Sort by date added (newest first)
     */
    DATE_ADDED("Date Added")
}
