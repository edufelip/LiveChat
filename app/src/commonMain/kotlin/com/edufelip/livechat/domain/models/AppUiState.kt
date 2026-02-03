package com.edufelip.livechat.domain.models

import androidx.compose.runtime.Immutable
import com.edufelip.livechat.domain.config.RemoteConfigDefaults

@Immutable
data class AppUiState(
    val isOnboardingComplete: Boolean = false,
    val hasSeenWelcome: Boolean = false,
    val isAppReady: Boolean = true,
    val privacyPolicyUrl: String = RemoteConfigDefaults.PRIVACY_POLICY_URL,
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
                AppDestination.Home
            }
}

@Immutable
data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.Conversations,
    val activeConversationId: String? = null,
    val activeConversationName: String? = null,
    val isContactsVisible: Boolean = false,
) {
    val destination: HomeDestination
        get() =
            when {
                selectedTab == HomeTab.Calls -> HomeDestination.Calls
                selectedTab == HomeTab.Settings -> HomeDestination.Settings
                else -> HomeDestination.ConversationList
            }
}
