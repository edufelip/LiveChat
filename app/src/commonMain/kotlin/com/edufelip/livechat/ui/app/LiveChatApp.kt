package com.edufelip.livechat.ui.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.analytics.rememberAnalyticsController
import com.edufelip.livechat.domain.models.AppDestination
import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.preview.PreviewFixtures
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.home.view.HomeScreen
import com.edufelip.livechat.ui.features.onboarding.OnboardingFlowScreen
import com.edufelip.livechat.ui.features.onboarding.WelcomeScreen
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.platform.AppLifecycleObserver
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAppPresenter
import com.edufelip.livechat.ui.state.rememberAppearanceSettingsPresenter
import com.edufelip.livechat.ui.state.rememberPrivacySettingsPresenter
import com.edufelip.livechat.ui.theme.LiveChatTheme
import com.edufelip.livechat.ui.theme.LocalReduceMotion
import com.edufelip.livechat.ui.util.isE2eMode
import com.edufelip.livechat.ui.util.isUiTestMode
import com.edufelip.livechat.ui.util.uiTestOverrides
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LiveChatApp(
    modifier: Modifier = Modifier,
    phoneContactsProvider: () -> List<Contact> = { emptyList() },
    onShareInvite: (InviteShareRequest) -> Unit = {},
    onOpenSettingsSection: (SettingsNavigationRequest) -> Unit = {},
) {
    val isInspection = LocalInspectionMode.current
    val appearanceSettings =
        if (isInspection) {
            AppearanceSettings()
        } else {
            val appearancePresenter = rememberAppearanceSettingsPresenter()
            val appearanceState by appearancePresenter.collectState()
            appearanceState.settings
        }
    LiveChatTheme(
        themeMode = appearanceSettings.themeMode,
        textScale = appearanceSettings.textScale,
        reduceMotion = appearanceSettings.reduceMotion,
        highContrast = appearanceSettings.highContrast,
    ) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            val contentModifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            if (isInspection) {
                HomeScreen(
                    modifier = contentModifier,
                    state = HomeUiState(),
                    onSelectTab = {},
                    onOpenConversation = { _, _ -> },
                    onStartConversationWithContact = { _, _ -> },
                    onOpenContacts = {},
                    onCloseContacts = {},
                    onShareInvite = onShareInvite,
                    onBackFromConversation = {},
                    phoneContactsProvider = phoneContactsProvider,
                    onOpenSettingsSection = onOpenSettingsSection,
                )
                return@LiveChatTheme
            }

            val presenter = rememberAppPresenter()
            val state by presenter.collectState()
            val uiTestOverrides = uiTestOverrides()
            val isUiTest = isUiTestMode()
            val isE2e = isE2eMode()
            val reduceMotion = LocalReduceMotion.current
            val strings = liveChatStrings()
            val privacyPresenter = rememberPrivacySettingsPresenter()
            val privacyState by privacyPresenter.collectState()
            val analyticsController = rememberAnalyticsController()

            AppLifecycleObserver(
                onForeground = presenter::onAppForeground,
                onBackground = presenter::onAppBackground,
            )

            LaunchedEffect(privacyState.settings.shareUsageData) {
                analyticsController.setCollectionEnabled(privacyState.settings.shareUsageData)
            }

            LaunchedEffect(presenter) {
                presenter.onAppForeground()
            }

            LaunchedEffect(isUiTest, isE2e, uiTestOverrides.resetOnboarding) {
                if ((isUiTest || isE2e) && uiTestOverrides.resetOnboarding) {
                    presenter.resetOnboarding()
                }
            }

            AnimatedContent(
                targetState = state.destination,
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
                contentKey = { it.navigationKey() },
                label = strings.general.homeDestinationTransitionLabel,
            ) { destination ->
                when (destination) {
                    AppDestination.Welcome ->
                        WelcomeScreen(
                            modifier = contentModifier,
                            onContinue = presenter::onWelcomeFinished,
                        )
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
                            onOpenContacts = presenter::openContacts,
                            onCloseContacts = presenter::closeContacts,
                            onShareInvite = onShareInvite,
                            onBackFromConversation = presenter::closeConversation,
                            phoneContactsProvider = phoneContactsProvider,
                            onOpenSettingsSection = onOpenSettingsSection,
                        )
                }
            }
        }
    }
}

@DevicePreviews
@Preview
@Composable
private fun LiveChatAppPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        val previewContacts = remember(strings) { PreviewFixtures.contacts(strings) }
        HomeScreen(
            state = HomeUiState(),
            onSelectTab = {},
            onOpenConversation = { _, _ -> },
            onStartConversationWithContact = { _, _ -> },
            onOpenContacts = {},
            onCloseContacts = {},
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { previewContacts },
            onOpenSettingsSection = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScreenConversationsPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        val previewContacts = remember(strings) { PreviewFixtures.contacts(strings) }
        HomeScreen(
            state = HomeUiState(),
            onSelectTab = {},
            onOpenConversation = { _, _ -> },
            onStartConversationWithContact = { _, _ -> },
            onOpenContacts = {},
            onCloseContacts = {},
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { previewContacts },
            onOpenSettingsSection = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScreenDetailPreview() {
    LiveChatPreviewContainer {
        val strings = liveChatStrings()
        val previewContacts = remember(strings) { PreviewFixtures.contacts(strings) }
        val previewConversation = remember(strings) { PreviewFixtures.conversationUiState(strings) }
        HomeScreen(
            state = HomeUiState(activeConversationId = previewConversation.conversationId),
            onSelectTab = {},
            onOpenConversation = { _, _ -> },
            onStartConversationWithContact = { _, _ -> },
            onOpenContacts = {},
            onCloseContacts = {},
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { previewContacts },
            onOpenSettingsSection = {},
        )
    }
}

private fun AppDestination.animationOrder(): Int =
    when (this) {
        AppDestination.Welcome -> 0
        AppDestination.Onboarding -> 1
        is AppDestination.Home -> 2
    }

private fun AppDestination.navigationKey(): String =
    when (this) {
        AppDestination.Welcome -> "welcome"
        AppDestination.Onboarding -> "onboarding"
        is AppDestination.Home -> "home"
    }
