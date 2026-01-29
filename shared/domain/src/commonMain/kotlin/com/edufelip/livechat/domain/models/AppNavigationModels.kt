package com.edufelip.livechat.domain.models

sealed class AppDestination {
    data object Splash : AppDestination()

    data object Welcome : AppDestination()

    data object Onboarding : AppDestination()

    data object Home : AppDestination()
}

sealed class HomeDestination {
    data object ConversationList : HomeDestination()

    data object Calls : HomeDestination()

    data object Settings : HomeDestination()
}

enum class HomeTab {
    Conversations,
    Calls,
    Settings,
}
