package com.project.livechat.composeapp.ui.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.features.onboarding.OnboardingFlowScreen
import com.project.livechat.composeapp.ui.features.home.view.HomeScreen
import com.project.livechat.composeapp.ui.state.collectState
import com.project.livechat.composeapp.ui.state.rememberAppPresenter
import com.project.livechat.domain.models.AppDestination
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.HomeUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LiveChatApp(
    modifier: Modifier = Modifier,
    phoneContactsProvider: () -> List<Contact> = { emptyList() },
) {
    MaterialTheme {
        if (LocalInspectionMode.current) {
            HomeScreen(
                modifier = modifier,
                state = HomeUiState(),
                onSelectTab = {},
                onOpenConversation = {},
                onBackFromConversation = {},
                phoneContactsProvider = phoneContactsProvider,
            )
            return@MaterialTheme
        }

        val presenter = rememberAppPresenter()
        val state by presenter.collectState()

        when (state.destination) {
            AppDestination.Onboarding ->
                OnboardingFlowScreen(
                    onFinished = { presenter.onOnboardingFinished() },
                )

            is AppDestination.Home ->
                HomeScreen(
                    modifier = modifier,
                    state = state.home,
                    onSelectTab = presenter::selectTab,
                    onOpenConversation = presenter::openConversation,
                    onBackFromConversation = presenter::closeConversation,
                    phoneContactsProvider = phoneContactsProvider,
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
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
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
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
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
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
        )
    }
}
