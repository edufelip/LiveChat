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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.analytics.rememberAnalyticsController
import com.edufelip.livechat.domain.models.AppDestination
import com.edufelip.livechat.domain.models.AppUiState
import com.edufelip.livechat.domain.models.AppearanceSettings
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.models.HomeUiState
import com.edufelip.livechat.notifications.NotificationNavigation
import com.edufelip.livechat.notifications.PlatformTokenRegistrar
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.components.molecules.InAppNotificationHost
import com.edufelip.livechat.ui.features.contacts.model.InviteShareRequest
import com.edufelip.livechat.ui.features.home.view.HomeScreen
import com.edufelip.livechat.ui.features.onboarding.OnboardingFlowScreen
import com.edufelip.livechat.ui.features.onboarding.WelcomeScreen
import com.edufelip.livechat.ui.platform.AppForegroundTracker
import com.edufelip.livechat.ui.platform.AppLifecycleObserver
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.resources.rememberLiveChatStrings
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
) {
    val isInspection = LocalInspectionMode.current
    val baseStrings = rememberLiveChatStrings()
    val appPresenter = if (isInspection) null else rememberAppPresenter()
    val appState by (appPresenter?.collectState() ?: remember { mutableStateOf(AppUiState()) })
    val privacyPolicyUrl = appState.privacyPolicyUrl.ifBlank { baseStrings.settings.privacyPolicyUrl }
    val resolvedStrings =
        remember(baseStrings, privacyPolicyUrl) {
            baseStrings.copy(
                settings = baseStrings.settings.copy(privacyPolicyUrl = privacyPolicyUrl),
                onboarding = baseStrings.onboarding.copy(welcomePrivacyUrl = privacyPolicyUrl),
            )
        }
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
        strings = resolvedStrings,
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
            val fullScreenModifier =
                contentModifier.windowInsetsPadding(WindowInsets.navigationBars)
            if (isInspection) {
                HomeScreen(
                    modifier = contentModifier,
                    state = HomeUiState(),
                    onSelectTab = {},
                    onOpenConversation = { _, _ -> },
                    onOpenContacts = {},
                    onOpenSettingsSection = {},
                )
                return@LiveChatTheme
            }

            val presenter = checkNotNull(appPresenter)
            val state = appState
            val uiTestOverrides = uiTestOverrides()
            val isUiTest = isUiTestMode()
            val isE2e = isE2eMode()
            val reduceMotion = LocalReduceMotion.current
            val strings = liveChatStrings()
            val privacyPresenter = rememberPrivacySettingsPresenter()
            val privacyState by privacyPresenter.collectState()
            val analyticsController = rememberAnalyticsController()

            AppLifecycleObserver(
                onForeground = {
                    AppForegroundTracker.onForeground()
                    presenter.onAppForeground()
                },
                onBackground = {
                    AppForegroundTracker.onBackground()
                    presenter.onAppBackground()
                },
            )

            LaunchedEffect(privacyState.settings.shareUsageData) {
                analyticsController.setCollectionEnabled(privacyState.settings.shareUsageData)
            }

            LaunchedEffect(Unit) {
                NotificationNavigation.events.collect { target ->
                    presenter.openConversation(target.conversationId, target.senderName)
                }
            }

            LaunchedEffect(presenter) {
                presenter.onAppForeground()
            }

            // Register FCM token when user is authenticated
            LaunchedEffect(state.isOnboardingComplete) {
                if (state.isOnboardingComplete) {
                    PlatformTokenRegistrar.registerCurrentToken()
                }
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
                    AppDestination.Splash ->
                        SplashScreen(
                            modifier = contentModifier,
                            message = strings.conversation.loadingList,
                        )
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

                    AppDestination.Home ->
                        HomeLayerHost(
                            homeState = state.home,
                            modifier = contentModifier,
                            fullScreenModifier = fullScreenModifier,
                            strings = strings,
                            reduceMotion = reduceMotion,
                            onSelectTab = presenter::selectTab,
                            onOpenConversation = presenter::openConversation,
                            onOpenContacts = presenter::openContacts,
                            onCloseContacts = presenter::closeContacts,
                            onStartConversationWithContact = presenter::startConversationWith,
                            onShareInvite = onShareInvite,
                            phoneContactsProvider = phoneContactsProvider,
                            onCloseConversation = presenter::closeConversation,
                        )
                }
            }

            InAppNotificationHost(
                modifier = Modifier.align(Alignment.TopCenter),
                onOpenConversation = { conversationId ->
                    presenter.openConversation(conversationId)
                },
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
            onOpenConversation = { _, _ -> },
            onOpenContacts = {},
            onOpenSettingsSection = {},
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
            onOpenConversation = { _, _ -> },
            onOpenContacts = {},
            onOpenSettingsSection = {},
        )
    }
}

@DevicePreviews
@Preview
@Composable
private fun HomeScreenDetailPreview() {
    LiveChatPreviewContainer {
        HomeScreen(
            state = HomeUiState(),
            onSelectTab = {},
            onOpenConversation = { _, _ -> },
            onOpenContacts = {},
            onOpenSettingsSection = {},
        )
    }
}

private fun AppDestination.animationOrder(): Int =
    when (this) {
        AppDestination.Splash -> 0
        AppDestination.Welcome -> 1
        AppDestination.Onboarding -> 2
        AppDestination.Home -> 3
    }

private fun AppDestination.navigationKey(): String =
    when (this) {
        AppDestination.Splash -> "splash"
        AppDestination.Welcome -> "welcome"
        AppDestination.Onboarding -> "onboarding"
        AppDestination.Home -> "home"
    }
