package com.edufelip.livechat.domain.models

sealed class AppDestination {
    data object Welcome : AppDestination()

    data object Onboarding : AppDestination()

    data class Home(val destination: HomeDestination) : AppDestination()
}

/**
 * Represents navigation destinations within the Home context.
 *
 * Destinations are categorized into two types:
 * - [TabDestination]: Shown within the main tab navigation with a bottom bar
 * - [DetailDestination]: Full-screen overlays without the bottom navigation bar
 *
 * This type hierarchy follows the Open/Closed Principle - new destination types
 * can be added without modifying existing navigation logic. Each destination
 * defines its own animation order for transition sequencing.
 */
sealed class HomeDestination {
    /**
     * Tab-based destinations that appear in the main navigation with a bottom bar.
     * Animation order determines slide direction (lower → higher slides right).
     */
    sealed class TabDestination(val animationOrder: Int) : HomeDestination() {
        data object ConversationList : TabDestination(animationOrder = 0)

        data object Calls : TabDestination(animationOrder = 1)

        data object Settings : TabDestination(animationOrder = 2)
    }

    /**
     * Detail destinations that appear as full-screen overlays without the bottom bar
     */
    sealed class DetailDestination : HomeDestination() {
        data object Contacts : DetailDestination()

        data class ConversationDetail(
            val conversationId: String,
            val contactName: String? = null,
        ) : DetailDestination()
    }
}

enum class HomeTab {
    Conversations,
    Calls,
    Settings,
}
