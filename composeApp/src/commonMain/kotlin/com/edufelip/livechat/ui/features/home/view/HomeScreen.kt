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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeDestination
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.ui.app.navigation.defaultHomeTabs
import com.edufelip.livechat.ui.features.calls.CallsRoute
import com.edufelip.livechat.ui.features.contacts.ContactsRoute
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.conversations.detail.ConversationDetailRoute
import com.edufelip.livechat.ui.features.conversations.list.ConversationListRoute
import com.edufelip.livechat.ui.features.settings.SettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsChromeVisibility
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LocalReduceMotion

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String, String?) -> Unit,
    onStartConversationWithContact: (Contact, String) -> Unit,
    onOpenContacts: () -> Unit,
    onCloseContacts: () -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    onBackFromConversation: () -> Unit,
    phoneContactsProvider: () -> List<Contact>,
    onOpenSettingsSection: (SettingsNavigationRequest) -> Unit,
) {
    val strings = liveChatStrings()
    val homeStrings = strings.home
    val onSelectTabAction = rememberStableAction(onSelectTab)
    val onOpenConversationAction = rememberStableAction(onOpenConversation)
    val onStartConversationWithContactAction = rememberStableAction(onStartConversationWithContact)
    val onOpenContactsAction = rememberStableAction(onOpenContacts)
    val onCloseContactsAction = rememberStableAction(onCloseContacts)
    val onShareInviteAction = rememberStableAction(onShareInvite)
    val onBackFromConversationAction = rememberStableAction(onBackFromConversation)
    val onOpenSettingsSectionAction = rememberStableAction(onOpenSettingsSection)
    val phoneContactsProviderAction = rememberStableProvider(phoneContactsProvider)
    val tabs = remember { defaultHomeTabs }
    val tabOptions =
        remember(homeStrings, tabs) {
            tabs.map { tabItem ->
                HomeTabOption(
                    tab = tabItem.tab,
                    label = tabItem.labelSelector(homeStrings),
                    icon = tabItem.icon,
                )
            }
        }
    val destination = state.destination
    val reduceMotion = LocalReduceMotion.current
    var settingsChrome by remember {
        mutableStateOf(
            SettingsChromeVisibility(
                showTopBar = true,
                showBottomBar = true,
            ),
        )
    }
    val onChromeVisibilityChanged =
        remember {
            { chrome: SettingsChromeVisibility -> settingsChrome = chrome }
        }
    val showBottomBar =
        destination !is HomeDestination.ConversationDetail &&
            destination != HomeDestination.Contacts &&
            (destination != HomeDestination.Settings || settingsChrome.showBottomBar)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    tabOptions.forEach { tabItem ->
                        val onTabClick =
                            remember(tabItem.tab, onSelectTabAction) {
                                { onSelectTabAction(tabItem.tab) }
                            }
                        NavigationBarItem(
                            selected = state.selectedTab == tabItem.tab,
                            onClick = onTabClick,
                            icon = { Icon(tabItem.icon, contentDescription = tabItem.label) },
                            label = { Text(tabItem.label) },
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
                    if (showBottomBar) {
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
            label = strings.general.homeDestinationTransitionLabel,
        ) { target ->
            when (target) {
                is HomeDestination.ConversationDetail ->
                    ConversationDetailRoute(
                        modifier = Modifier.fillMaxSize(),
                        conversationId = target.conversationId,
                        contactName = target.contactName,
                        onBack = onBackFromConversationAction,
                    )

                HomeDestination.ConversationList ->
                    ConversationListRoute(
                        modifier = Modifier.fillMaxSize(),
                        onConversationSelected = onOpenConversationAction,
                        onCompose = onOpenContactsAction,
                        onEmptyStateAction = onOpenContactsAction,
                    )

                HomeDestination.Contacts ->
                    ContactsRoute(
                        modifier = Modifier.fillMaxSize(),
                        phoneContactsProvider = phoneContactsProviderAction,
                        onContactSelected = onStartConversationWithContactAction,
                        onShareInvite = onShareInviteAction,
                        onBack = onCloseContactsAction,
                    )

                HomeDestination.Calls ->
                    CallsRoute(
                        modifier = Modifier.fillMaxSize(),
                    )

                HomeDestination.Settings ->
                    SettingsRoute(
                        modifier = Modifier.fillMaxSize(),
                        onSectionSelected = onOpenSettingsSectionAction,
                        onChromeVisibilityChanged = onChromeVisibilityChanged,
                    )
            }
        }
    }
}

private data class HomeTabOption(
    val tab: HomeTab,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun <T> rememberStableProvider(provider: () -> T): () -> T {
    val providerState = rememberUpdatedState(provider)
    return remember { { providerState.value() } }
}

@Composable
private fun <T> rememberStableAction(action: (T) -> Unit): (T) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value -> actionState.value(value) } }
}

@Composable
private fun <T1, T2> rememberStableAction(action: (T1, T2) -> Unit): (T1, T2) -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { value1, value2 -> actionState.value(value1, value2) } }
}

@Composable
private fun rememberStableAction(action: () -> Unit): () -> Unit {
    val actionState = rememberUpdatedState(action)
    return remember { { actionState.value() } }
}

private fun HomeDestination.animationOrder(): Int =
    when (this) {
        is HomeDestination.ConversationDetail -> 4
        HomeDestination.ConversationList -> 0
        HomeDestination.Calls -> 1
        HomeDestination.Settings -> 2
        HomeDestination.Contacts -> 3
    }
