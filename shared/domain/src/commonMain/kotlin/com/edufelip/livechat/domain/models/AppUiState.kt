package com.edufelip.livechat.domain.models

data class AppUiState(
    val isOnboardingComplete: Boolean = false,
    val hasSeenWelcome: Boolean = false,
    val home: HomeUiState = HomeUiState(),
) {
    val destination: AppDestination
        get() =
            if (!isOnboardingComplete) {
                if (hasSeenWelcome) {
                    AppDestination.Onboarding
                } else {
                    AppDestination.Welcome
                }
            } else {
                AppDestination.Home(home.destination)
            }
}

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.Conversations,
    val activeConversationId: String? = null,
    val activeConversationName: String? = null,
    val isContactsVisible: Boolean = false,
) {
    val destination: HomeDestination
        get() =
            when {
                activeConversationId != null -> HomeDestination.ConversationDetail(activeConversationId, activeConversationName)
                isContactsVisible -> HomeDestination.Contacts
                selectedTab == HomeTab.Calls -> HomeDestination.Calls
                selectedTab == HomeTab.Settings -> HomeDestination.Settings
                else -> HomeDestination.ConversationList
            }
}
