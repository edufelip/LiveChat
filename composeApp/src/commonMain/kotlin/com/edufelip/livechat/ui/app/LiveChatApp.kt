package com.edufelip.livechat.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.home.view.HomeScreen
import com.edufelip.livechat.ui.features.onboarding.OnboardingFlowScreen
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAppPresenter
import com.edufelip.livechat.ui.theme.LiveChatTheme
import com.edufelip.livechat.domain.models.AppDestination
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LiveChatApp(
    modifier: Modifier = Modifier,
    phoneContactsProvider: () -> List<Contact> = { emptyList() },
    onShareInvite: (InviteShareRequest) -> Unit = {},
    onOpenSettingsSection: (SettingsNavigationRequest) -> Unit = {},
) {
    LiveChatTheme {
        val contentModifier = modifier
        if (LocalInspectionMode.current) {
            HomeScreen(
                modifier = contentModifier,
                state = HomeUiState(),
                onSelectTab = {},
                onOpenConversation = {},
                onStartConversationWithContact = { _, _ -> },
                onShareInvite = onShareInvite,
                onBackFromConversation = {},
                phoneContactsProvider = phoneContactsProvider,
                onOpenSettingsSection = onOpenSettingsSection,
            )
            return@LiveChatTheme
        }

        val presenter = rememberAppPresenter()
        val state by presenter.collectState()

        when (state.destination) {
            AppDestination.Onboarding ->
                OnboardingFlowScreen(
                    modifier = contentModifier,
                    onFinished = { presenter.onOnboardingFinished() },
                )

            is AppDestination.Home ->
                HomeScreen(
                    modifier = contentModifier,
                    state = state.home,
                    onSelectTab = presenter::selectTab,
                    onOpenConversation = presenter::openConversation,
                    onStartConversationWithContact = presenter::startConversationWith,
                    onShareInvite = onShareInvite,
                    onBackFromConversation = presenter::closeConversation,
                    phoneContactsProvider = phoneContactsProvider,
                    onOpenSettingsSection = onOpenSettingsSection,
                )
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun LiveChatAppPreview() {
    LiveChatPreviewContainer {
        HomeScreen(
            state = HomeUiState(),
            onSelectTab = {},
            onOpenConversation = {},
            onStartConversationWithContact = { _, _ -> },
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
            onOpenSettingsSection = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScreenConversationsPreview() {
    LiveChatPreviewContainer {
        HomeScreen(
            state = HomeUiState(),
            onSelectTab = {},
            onOpenConversation = {},
            onStartConversationWithContact = { _, _ -> },
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
            onOpenSettingsSection = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScreenDetailPreview() {
    LiveChatPreviewContainer {
        HomeScreen(
            state = HomeUiState(activeConversationId = PreviewFixtures.conversationUiState.conversationId),
            onSelectTab = {},
            onOpenConversation = {},
            onStartConversationWithContact = { _, _ -> },
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
            onOpenSettingsSection = {},
        )
    }
}
