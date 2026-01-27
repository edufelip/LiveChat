package com.edufelip.livechat.ui.features.home.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.zIndex
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeDestination
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.domain.notifications.InAppNotification
import com.edufelip.livechat.domain.notifications.InAppNotificationCenter
import com.edufelip.livechat.ui.app.navigation.defaultHomeTabs
import com.edufelip.livechat.ui.common.navigation.PlatformBackGestureHandler
import com.edufelip.livechat.ui.components.molecules.InAppNotificationBanner
import com.edufelip.livechat.ui.features.calls.CallsRoute
import com.edufelip.livechat.ui.features.contacts.ContactsRoute
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.conversations.detail.ConversationDetailRoute
import com.edufelip.livechat.ui.features.conversations.list.ConversationListRoute
import com.edufelip.livechat.ui.features.settings.SettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LocalReduceMotion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Layout mode for the Home screen.
 * - [Tabs]: Shows tab-based navigation with bottom bar
 * - [Detail]: Shows full-screen detail overlay without bottom bar
 */
private sealed interface HomeLayout {
    data class Tabs(val destination: HomeDestination.TabDestination) : HomeLayout

    data class Detail(val destination: HomeDestination.DetailDestination) : HomeLayout
}

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
    val destination = state.destination
    val reduceMotion = LocalReduceMotion.current

    // Determine layout mode based on destination type
    val layout: HomeLayout =
        remember(destination) {
            when (destination) {
                is HomeDestination.TabDestination -> HomeLayout.Tabs(destination)
                is HomeDestination.DetailDestination -> HomeLayout.Detail(destination)
            }
        }

    // Keep track of last tab destination for smooth transitions
    var lastTabDestination by remember { mutableStateOf<HomeDestination.TabDestination>(HomeDestination.TabDestination.ConversationList) }
    LaunchedEffect(destination) {
        if (destination is HomeDestination.TabDestination) {
            lastTabDestination = destination
        }
    }

    // In-app notification state
    var currentNotification by remember { mutableStateOf<InAppNotification?>(null) }
    var showNotification by remember { mutableStateOf(false) }
    var dismissJob by remember { mutableStateOf<Job?>(null) }

    val currentDestination by rememberUpdatedState(destination)

    LaunchedEffect(Unit) {
        InAppNotificationCenter.events.collect { notification ->
            val destinationSnapshot = currentDestination
            // Don't show notification if we're viewing that conversation
            if (destinationSnapshot is HomeDestination.DetailDestination.ConversationDetail &&
                destinationSnapshot.conversationId == notification.conversationId
            ) {
                return@collect
            }

            // Cancel any existing dismiss timer
            dismissJob?.cancel()

            currentNotification = notification
            showNotification = true

            // Start new dismiss timer
            dismissJob =
                launch {
                    delay(5000)
                    showNotification = false
                    delay(300) // Wait for animation to complete
                    currentNotification = null
                }
        }
    }

    // Back gesture handling for detail destinations
    val backGestureEnabled = layout is HomeLayout.Detail
    val backGestureAction =
        remember(layout) {
            when (val detail = (layout as? HomeLayout.Detail)?.destination) {
                is HomeDestination.DetailDestination.ConversationDetail -> onBackFromConversation
                HomeDestination.DetailDestination.Contacts -> onCloseContacts
                null -> {
                    {}
                }
            }
        }

    PlatformBackGestureHandler(
        enabled = backGestureEnabled,
        onBack = rememberStableAction(backGestureAction),
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Main content with layout switching
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = layout,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                } else {
                    val isEnteringDetail = targetState is HomeLayout.Detail
                    val isExitingDetail = initialState is HomeLayout.Detail

                    when {
                        isEnteringDetail -> {
                            // Slide in from right when entering detail
                            (
                                slideInHorizontally(
                                    animationSpec = tween(300),
                                ) { fullWidth -> fullWidth / 3 } + fadeIn(animationSpec = tween(300))
                            ) togetherWith
                                fadeOut(animationSpec = tween(200))
                        }
                        isExitingDetail -> {
                            // Slide out to right when exiting detail
                            fadeIn(animationSpec = tween(300)) togetherWith
                                (
                                    slideOutHorizontally(
                                        animationSpec = tween(300),
                                    ) { fullWidth -> fullWidth / 3 } + fadeOut(animationSpec = tween(200))
                                )
                        }
                        else -> {
                            // Tab to tab transition (shouldn't happen at this level)
                            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                        }
                    }
                }
            },
            label = strings.general.homeDestinationTransitionLabel,
        ) { currentLayout ->
            when (currentLayout) {
                is HomeLayout.Tabs -> {
                    HomeTabsLayout(
                        modifier = Modifier.fillMaxSize(),
                        tabDestination = currentLayout.destination,
                        selectedTab = state.selectedTab,
                        onSelectTab = onSelectTab,
                        onOpenConversation = onOpenConversation,
                        onOpenContacts = onOpenContacts,
                        onOpenSettingsSection = onOpenSettingsSection,
                        reduceMotion = reduceMotion,
                    )
                }
                is HomeLayout.Detail -> {
                    HomeDetailLayout(
                        modifier = Modifier.fillMaxSize(),
                        detailDestination = currentLayout.destination,
                        onBackFromConversation = onBackFromConversation,
                        onCloseContacts = onCloseContacts,
                        onStartConversationWithContact = onStartConversationWithContact,
                        onShareInvite = onShareInvite,
                        phoneContactsProvider = phoneContactsProvider,
                    )
                }
            }
        }

        // In-app notification banner overlay
        currentNotification?.let { notification ->
            InAppNotificationBanner(
                notification = notification,
                visible = showNotification,
                onDismiss = {
                    dismissJob?.cancel()
                    showNotification = false
                    currentNotification = null
                },
                onClick = {
                    dismissJob?.cancel()
                    showNotification = false
                    currentNotification = null
                    // Navigate to the conversation
                    notification.conversationId?.let { conversationId ->
                        onOpenConversation(conversationId, null)
                    }
                },
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(10f),
            )
        }
    }
}

/**
 * Layout for tab-based navigation with bottom bar
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HomeTabsLayout(
    tabDestination: HomeDestination.TabDestination,
    selectedTab: HomeTab,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String, String?) -> Unit,
    onOpenContacts: () -> Unit,
    onOpenSettingsSection: (SettingsNavigationRequest) -> Unit,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier,
) {
    val strings = liveChatStrings()
    val homeStrings = strings.home
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

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
            ) {
                tabOptions.forEach { tabItem ->
                    val onTabClick =
                        remember(tabItem.tab, onSelectTab) {
                            { onSelectTab(tabItem.tab) }
                        }
                    NavigationBarItem(
                        selected = selectedTab == tabItem.tab,
                        onClick = onTabClick,
                        icon = { Icon(tabItem.icon, contentDescription = tabItem.label) },
                        label = { Text(tabItem.label) },
                    )
                }
            }
        },
    ) { padding ->
        val contentModifier =
            remember(padding) {
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            }

        // Animate between tab destinations
        AnimatedContent(
            modifier = contentModifier,
            targetState = tabDestination,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(100)) togetherWith fadeOut(animationSpec = tween(100))
                } else {
                    val direction =
                        when {
                            targetState.animationOrder > initialState.animationOrder -> 1
                            targetState.animationOrder < initialState.animationOrder -> -1
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
                HomeDestination.TabDestination.ConversationList ->
                    ConversationListRoute(
                        modifier = Modifier.fillMaxSize(),
                        onConversationSelected = onOpenConversation,
                        onCompose = onOpenContacts,
                        onEmptyStateAction = onOpenContacts,
                    )

                HomeDestination.TabDestination.Calls ->
                    CallsRoute(
                        modifier = Modifier.fillMaxSize(),
                    )

                HomeDestination.TabDestination.Settings ->
                    SettingsRoute(
                        modifier = Modifier.fillMaxSize(),
                        onSectionSelected = onOpenSettingsSection,
                    )
            }
        }
    }
}

/**
 * Layout for full-screen detail destinations
 */
@Composable
private fun HomeDetailLayout(
    detailDestination: HomeDestination.DetailDestination,
    onBackFromConversation: () -> Unit,
    onCloseContacts: () -> Unit,
    onStartConversationWithContact: (Contact, String) -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    phoneContactsProvider: () -> List<Contact>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        when (detailDestination) {
            is HomeDestination.DetailDestination.ConversationDetail ->
                ConversationDetailRoute(
                    modifier = Modifier.fillMaxSize(),
                    conversationId = detailDestination.conversationId,
                    contactName = detailDestination.contactName,
                    onBack = onBackFromConversation,
                )

            HomeDestination.DetailDestination.Contacts ->
                ContactsRoute(
                    modifier = Modifier.fillMaxSize(),
                    phoneContactsProvider = phoneContactsProvider,
                    onContactSelected = onStartConversationWithContact,
                    onShareInvite = onShareInvite,
                    onBack = onCloseContacts,
                )
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
