package com.project.livechat.composeapp.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.project.livechat.composeapp.preview.DevicePreviews
import com.project.livechat.composeapp.preview.LiveChatPreviewContainer
import com.project.livechat.composeapp.preview.PreviewFixtures
import com.project.livechat.composeapp.ui.features.contacts.ContactsRoute
import com.project.livechat.composeapp.ui.features.conversations.detail.ConversationDetailRoute
import com.project.livechat.composeapp.ui.features.conversations.list.ConversationListRoute
import com.project.livechat.composeapp.ui.features.onboarding.OnboardingFlowScreen
import com.project.livechat.domain.models.Contact
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LiveChatApp(
    modifier: Modifier = Modifier,
    phoneContactsProvider: () -> List<Contact> = { emptyList() },
) {
    MaterialTheme {
        var onboardingComplete by rememberSaveable { mutableStateOf(false) }
        if (!onboardingComplete) {
            OnboardingFlowScreen(
                onFinished = { onboardingComplete = true },
            )
            return@MaterialTheme
        }

        var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Conversations) }
        var activeConversationId by rememberSaveable { mutableStateOf<String?>(null) }

        HomeScaffold(
            modifier = modifier,
            selectedTab = selectedTab,
            onSelectTab = { tab ->
                activeConversationId = null
                selectedTab = tab
            },
            phoneContactsProvider = phoneContactsProvider,
            onOpenConversation = { conversationId ->
                activeConversationId = conversationId
            },
            onBackFromConversation = {
                activeConversationId = null
            },
            activeConversationId = activeConversationId,
        )
    }
}

private enum class HomeTab { Conversations, Contacts }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(
    modifier: Modifier = Modifier,
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    phoneContactsProvider: () -> List<Contact>,
    onOpenConversation: (String) -> Unit,
    onBackFromConversation: () -> Unit,
    activeConversationId: String?,
) {
    val topBarState = rememberTopAppBarState()
    val topBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            when {
                                activeConversationId != null -> "Conversation"
                                selectedTab == HomeTab.Conversations -> "Chats"
                                else -> "Contacts"
                            },
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (activeConversationId != null) {
                        TextButton(onClick = onBackFromConversation) {
                            Text("Back")
                        }
                    }
                },
                scrollBehavior = topBarScrollBehavior,
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Conversations,
                    onClick = { onSelectTab(HomeTab.Conversations) },
                    icon = { Icon(rememberTabIcon(HomeTab.Conversations), contentDescription = null) },
                    label = { Text("Chats") },
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Contacts,
                    onClick = { onSelectTab(HomeTab.Contacts) },
                    icon = { Icon(rememberTabIcon(HomeTab.Contacts), contentDescription = null) },
                    label = { Text("Contacts") },
                )
            }
        },
    ) { padding ->
        when {
            activeConversationId != null ->
                ConversationDetailRoute(
                    modifier = Modifier.padding(padding),
                    conversationId = activeConversationId,
                )

            selectedTab == HomeTab.Conversations ->
                ConversationListRoute(
                    modifier = Modifier.padding(padding),
                    onConversationSelected = onOpenConversation,
                )

            else ->
                ContactsRoute(
                    modifier = Modifier.padding(padding),
                    phoneContactsProvider = phoneContactsProvider,
                )
        }
    }
}

@Composable
private fun rememberTabIcon(tab: HomeTab) =
    when (tab) {
        HomeTab.Conversations -> AppIcons.conversations
        HomeTab.Contacts -> AppIcons.contacts
    }

@DevicePreviews
@Preview
@Composable
private fun LiveChatAppPreview() {
    LiveChatPreviewContainer {
        LiveChatApp()
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScaffoldConversationsPreview() {
    LiveChatPreviewContainer {
        HomeScaffold(
            selectedTab = HomeTab.Conversations,
            onSelectTab = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
            onOpenConversation = {},
            onBackFromConversation = {},
            activeConversationId = null,
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScaffoldDetailPreview() {
    LiveChatPreviewContainer {
        HomeScaffold(
            selectedTab = HomeTab.Conversations,
            onSelectTab = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
            onOpenConversation = {},
            onBackFromConversation = {},
            activeConversationId = PreviewFixtures.conversationUiState.conversationId,
        )
    }
}
