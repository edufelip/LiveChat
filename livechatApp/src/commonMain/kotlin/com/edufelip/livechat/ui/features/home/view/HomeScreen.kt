package com.edufelip.livechat.ui.features.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.ui.app.navigation.defaultHomeTabs
import com.edufelip.livechat.ui.features.contacts.ContactsRoute
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.conversations.detail.ConversationDetailRoute
import com.edufelip.livechat.ui.features.conversations.list.ConversationListRoute
import com.edufelip.livechat.ui.features.settings.SettingsRoute
import com.edufelip.livechat.ui.features.settings.screens.SettingsSection
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeDestination
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.HomeUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String) -> Unit,
    onStartConversationWithContact: (Contact) -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    onBackFromConversation: () -> Unit,
    phoneContactsProvider: () -> List<Contact>,
    onOpenSettingsSection: (SettingsSection) -> Unit,
) {
    val strings = liveChatStrings()
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
                        text = topBarTitle(destination, strings),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                    )
                },
                navigationIcon = {
                    if (destination is HomeDestination.ConversationDetail) {
                        TextButton(onClick = onBackFromConversation) {
                            Text(strings.home.backCta)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tabItem ->
                    NavigationBarItem(
                        selected = state.selectedTab == tabItem.tab,
                        onClick = { onSelectTab(tabItem.tab) },
                        icon = { Icon(tabItem.icon, contentDescription = tabItem.labelSelector(strings.home)) },
                        label = { Text(tabItem.labelSelector(strings.home)) },
                    )
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            modifier =
                Modifier
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
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                } else {
                    (
                        slideInHorizontally(
                            animationSpec = tween(300),
                        ) { fullWidth -> fullWidth / 4 * direction } + fadeIn(animationSpec = tween(300))
                    ) togetherWith
                        (
                            slideOutHorizontally(
                                animationSpec = tween(300),
                            ) { fullWidth -> -fullWidth / 4 * direction } + fadeOut(animationSpec = tween(200))
                        )
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

                HomeDestination.Settings ->
                    SettingsRoute(
                        modifier = Modifier.fillMaxSize(),
                        onSectionSelected = onOpenSettingsSection,
                    )
            }
        }
    }
}

private fun topBarTitle(
    destination: HomeDestination,
    strings: LiveChatStrings,
): String =
    when (destination) {
        is HomeDestination.ConversationDetail -> strings.home.conversationTitle
        HomeDestination.ConversationList -> strings.home.chatsTab
        HomeDestination.Contacts -> strings.home.contactsTab
        HomeDestination.Settings -> strings.home.settingsTab
    }

private fun HomeDestination.animationOrder(): Int =
    when (this) {
        is HomeDestination.ConversationDetail -> 3
        HomeDestination.ConversationList -> 0
        HomeDestination.Contacts -> 1
        HomeDestination.Settings -> 2
    }
