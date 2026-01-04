package com.edufelip.livechat.ui.app

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
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
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.state.collectState
import com.edufelip.livechat.ui.state.rememberAppPresenter
import com.edufelip.livechat.ui.state.rememberAppearanceSettingsPresenter
import com.edufelip.livechat.ui.theme.LiveChatTheme
import com.edufelip.livechat.ui.util.isE2eMode
import com.edufelip.livechat.ui.util.isUiTestMode
import com.edufelip.livechat.ui.util.uiTestOverrides
import org.jetbrains.compose.ui.tooling.preview.Preview

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

            LaunchedEffect(isUiTest, isE2e, uiTestOverrides.resetOnboarding) {
                if ((isUiTest || isE2e) && uiTestOverrides.resetOnboarding) {
                    presenter.resetOnboarding()
                }
            }

            when (state.destination) {
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
                        onShareInvite = onShareInvite,
                        onBackFromConversation = presenter::closeConversation,
                        phoneContactsProvider = phoneContactsProvider,
                        onOpenSettingsSection = onOpenSettingsSection,
                    )
            }
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
            onStartConversationWithContact = { _, _ -> },
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
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
            onStartConversationWithContact = { _, _ -> },
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
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
            state = HomeUiState(activeConversationId = PreviewFixtures.conversationUiState.conversationId),
            onSelectTab = {},
            onOpenConversation = { _, _ -> },
            onStartConversationWithContact = { _, _ -> },
            onShareInvite = {},
            onBackFromConversation = {},
            phoneContactsProvider = { PreviewFixtures.contacts },
            onOpenSettingsSection = {},
        )
    }
}
