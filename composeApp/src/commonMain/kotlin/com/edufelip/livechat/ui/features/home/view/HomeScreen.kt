package com.edufelip.livechat.ui.features.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeDestination
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.ui.app.navigation.defaultHomeTabs
import com.edufelip.livechat.ui.features.contacts.ContactsRoute
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.conversations.detail.ConversationDetailRoute
import com.edufelip.livechat.ui.features.conversations.list.ConversationListRoute
import com.edufelip.livechat.ui.features.settings.SettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.resources.LiveChatStrings
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LocalReduceMotion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String, String?) -> Unit,
    onStartConversationWithContact: (Contact, String) -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    onBackFromConversation: () -> Unit,
    phoneContactsProvider: () -> List<Contact>,
    onOpenSettingsSection: (SettingsNavigationRequest) -> Unit,
) {
    val strings = liveChatStrings()
    val topBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topBarState)
    val tabs = remember { defaultHomeTabs }
    val destination = state.destination
    val reduceMotion = LocalReduceMotion.current
    var showSettingsChrome by remember { mutableStateOf(true) }
    val showChrome =
        destination !is HomeDestination.ConversationDetail &&
            (destination != HomeDestination.Settings || showSettingsChrome)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        topBar = {
            if (showChrome) {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle(destination, strings),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp),
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    tabs.forEach { tabItem ->
                        NavigationBarItem(
                            selected = state.selectedTab == tabItem.tab,
                            onClick = { onSelectTab(tabItem.tab) },
                            icon = { Icon(tabItem.icon, contentDescription = tabItem.labelSelector(strings.home)) },
                            label = { Text(tabItem.labelSelector(strings.home)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        val bodyModifier =
            Modifier
                .padding(padding)
                .fillMaxSize()
                .then(
                    if (showChrome) {
                        Modifier
                    } else {
                        Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    },
                )

        AnimatedContent(
            modifier = bodyModifier,
            targetState = destination,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                } else {
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
                }
            },
            label = "homeDestinationTransition",
        ) { target ->
            when (target) {
                is HomeDestination.ConversationDetail ->
                    ConversationDetailRoute(
                        modifier = Modifier.fillMaxSize(),
                        conversationId = target.conversationId,
                        contactName = target.contactName,
                        onBack = onBackFromConversation,
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
                        onChromeVisibilityChanged = { showSettingsChrome = it },
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
