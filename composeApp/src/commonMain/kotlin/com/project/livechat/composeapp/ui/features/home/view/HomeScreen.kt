package com.project.livechat.composeapp.ui.features.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.project.livechat.composeapp.ui.app.navigation.defaultHomeTabs
import com.project.livechat.composeapp.ui.features.contacts.ContactsRoute
import com.project.livechat.composeapp.ui.features.conversations.detail.ConversationDetailRoute
import com.project.livechat.composeapp.ui.features.conversations.list.ConversationListRoute
import com.project.livechat.domain.models.Contact
import com.project.livechat.domain.models.HomeDestination
import com.project.livechat.domain.models.HomeTab
import com.project.livechat.domain.models.HomeUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String) -> Unit,
    onStartConversationWithContact: (Contact) -> Unit,
    onShareInvite: (String) -> Unit,
    onBackFromConversation: () -> Unit,
    phoneContactsProvider: () -> List<Contact>,
) {
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val tabs = remember { defaultHomeTabs }
    val destination = state.destination

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = topBarTitle(destination),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    if (destination is HomeDestination.ConversationDetail) {
                        TextButton(onClick = onBackFromConversation) {
                            Text("Back")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                tabs.forEach { tabItem ->
                    NavigationBarItem(
                        selected = state.selectedTab == tabItem.tab,
                        onClick = { onSelectTab(tabItem.tab) },
                        icon = { Icon(tabItem.icon, contentDescription = null) },
                        label = { Text(tabItem.label) },
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            targetState = destination,
            transitionSpec = {
                val direction =
                    when {
                        targetState.animationOrder() > initialState.animationOrder() -> 1
                        targetState.animationOrder() < initialState.animationOrder() -> -1
                        else -> 0
                    }
                if (direction == 0) {
                    fadeIn() togetherWith fadeOut()
                } else {
                    (slideInHorizontally { fullWidth -> fullWidth / 4 * direction } + fadeIn()) togetherWith
                        (slideOutHorizontally { fullWidth -> -fullWidth / 4 * direction } + fadeOut())
                }
            },
            label = "homeDestinationTransition",
        ) { target ->
            when (target) {
                is HomeDestination.ConversationDetail ->
                    ConversationDetailRoute(
                        modifier = Modifier.fillMaxSize(),
                        conversationId = target.conversationId,
                    )

                HomeDestination.ConversationList ->
                    ConversationListRoute(
                        modifier = Modifier.fillMaxSize(),
                        onConversationSelected = onOpenConversation,
                    )

                HomeDestination.Contacts ->
                    ContactsRoute(
                        modifier = Modifier.fillMaxSize(),
                        phoneContactsProvider = phoneContactsProvider,
                        onContactSelected = onStartConversationWithContact,
                        onShareInvite = onShareInvite,
                    )
            }
        }
    }
}

private fun topBarTitle(destination: HomeDestination): String =
    when (destination) {
        is HomeDestination.ConversationDetail -> "Conversation"
        HomeDestination.ConversationList -> "Chats"
        HomeDestination.Contacts -> "Contacts"
    }

private fun HomeDestination.animationOrder(): Int =
    when (this) {
        is HomeDestination.ConversationDetail -> 2
        HomeDestination.ConversationList -> 0
        HomeDestination.Contacts -> 1
    }
