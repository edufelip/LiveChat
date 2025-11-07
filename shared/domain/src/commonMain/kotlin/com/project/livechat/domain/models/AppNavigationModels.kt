package com.project.livechat.domain.models

sealed class AppDestination {
    data object Onboarding : AppDestination()

    data class Home(val destination: HomeDestination) : AppDestination()
}

sealed class HomeDestination {
    data object ConversationList : HomeDestination()

    data object Contacts : HomeDestination()

    data object Settings : HomeDestination()

    data class ConversationDetail(val conversationId: String) : HomeDestination()
}

enum class HomeTab {
    Conversations,
    Contacts,
    Settings,
}
