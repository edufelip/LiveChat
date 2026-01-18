package com.edufelip.livechat.ui.features.settings.model

/**
 * State holder for settings item highlight behavior.
 *
 * Encapsulates the logic for highlighting a specific item when navigating
 * from search results. Follows single responsibility principle by managing
 * only highlight state and timing.
 *
 * Design decisions:
 * - Immutable data class for thread safety
 * - Nullable targetItemId to represent "no highlight" state
 * - Simple boolean flag for highlight state (can be extended with animations)
 *
 * @param targetItemId The ID of the item to highlight, or null if no highlight
 * @param isHighlighted Whether the item is currently highlighted
 */
data class SettingsHighlightState(
    val targetItemId: String? = null,
    val isHighlighted: Boolean = false,
) {
    /**
     * Checks if a specific item should be highlighted.
     *
     * @param itemId The ID of the item to check
     * @return true if this item should be highlighted, false otherwise
     */
    fun shouldHighlight(itemId: String): Boolean {
        return isHighlighted && targetItemId == itemId
    }

    companion object {
        /**
         * Creates an initial state with no highlight.
         */
        fun initial() = SettingsHighlightState()

        /**
         * Creates a state ready to highlight a specific item.
         */
        fun forItem(itemId: String) =
            SettingsHighlightState(
                targetItemId = itemId,
                isHighlighted = true,
            )
    }
}
