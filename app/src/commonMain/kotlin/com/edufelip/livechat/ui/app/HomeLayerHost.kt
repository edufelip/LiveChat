package com.edufelip.livechat.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeTab
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.domain.presentation.AppearanceSettingsPresenter
import com.edufelip.livechat.ui.common.navigation.PlatformBackGestureHandler
import com.edufelip.livechat.ui.features.contacts.ContactsRoute
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.conversations.detail.ConversationDetailRoute
import com.edufelip.livechat.ui.features.home.view.HomeScreen
import com.edufelip.livechat.ui.features.settings.account.AccountSettingsRoute
import com.edufelip.livechat.ui.features.settings.appearance.AppearanceSettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.features.settings.model.SettingsSection
import com.edufelip.livechat.ui.features.settings.notifications.NotificationSettingsRoute
import com.edufelip.livechat.ui.features.settings.privacy.PrivacySettingsRoute
import com.edufelip.livechat.ui.platform.openWebViewUrl
import com.edufelip.livechat.ui.resources.LiveChatStrings

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun HomeLayerHost(
    homeState: HomeUiState,
    modifier: Modifier,
    fullScreenModifier: Modifier,
    strings: LiveChatStrings,
    onSelectTab: (HomeTab) -> Unit,
    onOpenConversation: (String, String?) -> Unit,
    onOpenContacts: () -> Unit,
    onCloseContacts: () -> Unit,
    onStartConversationWithContact: (Contact, String) -> Unit,
    onShareInvite: (InviteShareRequest) -> Unit,
    phoneContactsProvider: () -> List<Contact>,
    onCloseConversation: () -> Unit,
    appearanceSettingsPresenter: AppearanceSettingsPresenter,
) {
    var settingsSelection by rememberSaveable(stateSaver = settingsSelectionSaver) {
        mutableStateOf<SettingsSelection?>(null)
    }
    val onSettingsSectionSelected =
        remember {
            { request: SettingsNavigationRequest ->
                settingsSelection = SettingsSelection(request.section, request.targetItemId)
            }
        }

    LaunchedEffect(
        settingsSelection,
        homeState.selectedTab,
        homeState.activeConversationId,
        homeState.isContactsVisible,
    ) {
        if (
            settingsSelection != null &&
            homeState.selectedTab != HomeTab.Settings &&
            homeState.activeConversationId == null &&
            !homeState.isContactsVisible
        ) {
            onSelectTab(HomeTab.Settings)
        }
    }

    val activeConversationId = homeState.activeConversationId
    val isContactsVisible = homeState.isContactsVisible
    val currentSettingsSelection = settingsSelection
    val homeLayer =
        when {
            activeConversationId != null ->
                HomeLayer.ConversationDetail(
                    conversationId = activeConversationId,
                )
            isContactsVisible -> HomeLayer.Contacts
            currentSettingsSelection != null ->
                HomeLayer.SettingsSubsection(
                    section = currentSettingsSelection.section,
                    targetItemId = currentSettingsSelection.targetItemId,
                )
            else -> HomeLayer.Tabs
        }

    AnimatedContent(
        targetState = homeLayer,
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
        contentKey = { it.navigationKey() },
        label = strings.general.homeDestinationTransitionLabel,
    ) { layer ->
        when (layer) {
            HomeLayer.Tabs ->
                HomeScreen(
                    modifier = modifier,
                    state = homeState,
                    onSelectTab = onSelectTab,
                    onOpenConversation = onOpenConversation,
                    onOpenContacts = onOpenContacts,
                    onOpenSettingsSection = onSettingsSectionSelected,
                )
            HomeLayer.Contacts -> {
                PlatformBackGestureHandler(
                    enabled = true,
                    onBack = onCloseContacts,
                )
                ContactsRoute(
                    modifier = fullScreenModifier,
                    phoneContactsProvider = phoneContactsProvider,
                    onContactSelected = onStartConversationWithContact,
                    onShareInvite = onShareInvite,
                    onBack = onCloseContacts,
                )
            }
            is HomeLayer.ConversationDetail -> {
                PlatformBackGestureHandler(
                    enabled = true,
                    onBack = onCloseConversation,
                )
                ConversationDetailRoute(
                    modifier = fullScreenModifier,
                    conversationId = layer.conversationId,
                    contactName = homeState.activeConversationName,
                    onBack = onCloseConversation,
                )
            }
            is HomeLayer.SettingsSubsection -> {
                val onBack = { settingsSelection = null }
                when (layer.section) {
                    SettingsSection.Account ->
                        AccountSettingsRoute(
                            modifier = fullScreenModifier,
                            onBack = onBack,
                            targetItemId = layer.targetItemId,
                        )
                    SettingsSection.Notifications ->
                        NotificationSettingsRoute(
                            modifier = fullScreenModifier,
                            onBack = onBack,
                            targetItemId = layer.targetItemId,
                        )
                    SettingsSection.Appearance ->
                        AppearanceSettingsRoute(
                            modifier = fullScreenModifier,
                            onBack = onBack,
                            targetItemId = layer.targetItemId,
                            presenterOverride = appearanceSettingsPresenter,
                        )
                    SettingsSection.Privacy -> {
                        val privacyUrl = strings.settings.privacyPolicyUrl
                        PrivacySettingsRoute(
                            modifier = fullScreenModifier,
                            onBack = onBack,
                            targetItemId = layer.targetItemId,
                            onOpenPrivacyPolicy = {
                                if (privacyUrl.isNotBlank()) {
                                    openWebViewUrl(privacyUrl)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Immutable
private data class SettingsSelection(
    val section: SettingsSection,
    val targetItemId: String?,
)

private val settingsSelectionSaver: Saver<SettingsSelection?, Any> =
    Saver(
        save = { selection ->
            selection?.let {
                listOf(
                    it.section.name,
                    it.targetItemId,
                )
            }
        },
        restore = { restored ->
            val list = restored as? List<*> ?: return@Saver null
            val sectionName = list.getOrNull(0) as? String ?: return@Saver null
            val target = list.getOrNull(1) as? String
            runCatching { SettingsSection.valueOf(sectionName) }
                .getOrNull()
                ?.let { SettingsSelection(it, target) }
        },
    )

@Immutable
private sealed class HomeLayer {
    data object Tabs : HomeLayer()

    data object Contacts : HomeLayer()

    data class ConversationDetail(
        val conversationId: String,
    ) : HomeLayer()

    data class SettingsSubsection(
        val section: SettingsSection,
        val targetItemId: String?,
    ) : HomeLayer()
}

private fun HomeLayer.animationOrder(): Int =
    when (this) {
        HomeLayer.Tabs -> 0
        HomeLayer.Contacts -> 1
        is HomeLayer.ConversationDetail -> 2
        is HomeLayer.SettingsSubsection -> 3
    }

private fun HomeLayer.navigationKey(): String =
    when (this) {
        HomeLayer.Tabs -> "home-tabs"
        HomeLayer.Contacts -> "home-contacts"
        is HomeLayer.ConversationDetail -> "home-conversation-${this.conversationId}"
        is HomeLayer.SettingsSubsection ->
            "home-settings-${this.section.name}-${this.targetItemId ?: "root"}"
    }
