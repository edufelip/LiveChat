package com.edufelip.livechat.domain.models

data class AppUiState(
    val isOnboardingComplete: Boolean = false,
    val home: HomeUiState = HomeUiState(),
) {
    val destination: AppDestination
        get() =
            if (!isOnboardingComplete) {
                AppDestination.Onboarding
            } else {
                AppDestination.Home(home.destination)
            }
}

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.Conversations,
    val activeConversationId: String? = null,
) {
    val destination: HomeDestination
        get() =
            when {
                activeConversationId != null -> HomeDestination.ConversationDetail(activeConversationId)
                selectedTab == HomeTab.Contacts -> HomeDestination.Contacts
                selectedTab == HomeTab.Settings -> HomeDestination.Settings
                else -> HomeDestination.ConversationList
            }
}
