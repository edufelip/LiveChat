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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edufelip.livechat.domain.models.HomeDestination
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.ui.app.navigation.defaultHomeTabs
import com.edufelip.livechat.ui.features.calls.CallsRoute
import com.edufelip.livechat.ui.features.conversations.list.ConversationListRoute
import com.edufelip.livechat.ui.features.settings.SettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.resources.liveChatStrings

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HomeScreen(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String, String?) -> Unit,
    onOpenContacts: () -> Unit,
    onOpenSettingsSection: (SettingsNavigationRequest) -> Unit,
) {
    val strings = liveChatStrings()
    val homeStrings = strings.home
    val onSelectTabAction = rememberStableAction(onSelectTab)
    val onOpenConversationAction = rememberStableAction(onOpenConversation)
    val onOpenContactsAction = rememberStableAction(onOpenContacts)
    val onOpenSettingsSectionAction = rememberStableAction(onOpenSettingsSection)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
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
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            val bodyModifier =
                Modifier
                    .padding(padding)
                    .fillMaxSize()

            AnimatedContent(
                modifier = bodyModifier,
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
                label = strings.general.homeDestinationTransitionLabel,
            ) { target ->
                when (target) {
                    HomeDestination.ConversationList ->
                        ConversationListRoute(
                            modifier = Modifier.fillMaxSize(),
                            onConversationSelected = onOpenConversationAction,
                            onCompose = onOpenContactsAction,
                            onEmptyStateAction = onOpenContactsAction,
                        )

                    HomeDestination.Calls ->
                        CallsRoute(
                            modifier = Modifier.fillMaxSize(),
                        )

                    HomeDestination.Settings ->
                        SettingsRoute(
                            modifier = Modifier.fillMaxSize(),
                            onSectionSelected = onOpenSettingsSectionAction,
                        )
                }
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
        HomeDestination.ConversationList -> 0
        HomeDestination.Calls -> 1
        HomeDestination.Settings -> 2
    }
