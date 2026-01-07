package com.edufelip.livechat.ui.features.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.account.AccountSettingsRoute
import com.edufelip.livechat.ui.features.settings.appearance.AppearanceSettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsChromeVisibility
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.features.settings.notifications.NotificationSettingsRoute
import com.edufelip.livechat.ui.features.settings.privacy.PrivacySettingsRoute
import com.edufelip.livechat.ui.features.settings.screens.SettingsScreen
import com.edufelip.livechat.ui.features.settings.screens.SettingsSection
import com.edufelip.livechat.ui.platform.openWebViewUrl
import com.edufelip.livechat.ui.resources.liveChatStrings
import com.edufelip.livechat.ui.theme.LocalReduceMotion
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsNavigationRequest) -> Unit = {},
    onChromeVisibilityChanged: (SettingsChromeVisibility) -> Unit = {},
    accountContent: SettingsSectionContent = { routeModifier, onBack ->
        AccountSettingsRoute(
            modifier = routeModifier,
            onBack = onBack,
        )
    },
    notificationsContent: SettingsSectionContent = { routeModifier, onBack ->
        NotificationSettingsRoute(
            modifier = routeModifier,
            onBack = onBack,
        )
    },
    appearanceContent: SettingsSectionContent = { routeModifier, onBack ->
        AppearanceSettingsRoute(
            modifier = routeModifier,
            onBack = onBack,
        )
    },
    privacyContent: SettingsSectionContent = { routeModifier, onBack ->
        val privacyUrl = liveChatStrings().settings.privacyPolicyUrl
        PrivacySettingsRoute(
            modifier = routeModifier,
            onBack = onBack,
            onOpenPrivacyPolicy = {
                if (privacyUrl.isNotBlank()) {
                    openWebViewUrl(privacyUrl)
                }
            },
        )
    },
) {
    val strings = liveChatStrings()
    val reduceMotion = LocalReduceMotion.current
    var destination by rememberSaveable { mutableStateOf(SettingsDestination.List) }
    val chromeVisibility =
        remember(destination) {
            when (destination) {
                SettingsDestination.List ->
                    SettingsChromeVisibility(
                        showTopBar = false,
                        showBottomBar = true,
                    )

                else ->
                    SettingsChromeVisibility(
                        showTopBar = false,
                        showBottomBar = false,
                    )
            }
        }

    LaunchedEffect(chromeVisibility) {
        onChromeVisibilityChanged(chromeVisibility)
    }

    if (LocalInspectionMode.current) {
        SettingsScreen(
            modifier = modifier,
            onSectionSelected = onSectionSelected,
        )
        return
    }

    AnimatedContent(
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
            SettingsDestination.List ->
                SettingsScreen(
                    modifier = modifier,
                    onSectionSelected = { request ->
                        when (request.section) {
                            SettingsSection.Account -> destination = SettingsDestination.Account
                            SettingsSection.Notifications -> destination = SettingsDestination.Notifications
                            SettingsSection.Appearance -> destination = SettingsDestination.Appearance
                            SettingsSection.Privacy -> destination = SettingsDestination.Privacy
                        }
                    },
                )

            SettingsDestination.Account ->
                accountContent(
                    modifier,
                    { destination = SettingsDestination.List },
                )

            SettingsDestination.Notifications ->
                notificationsContent(
                    modifier,
                    { destination = SettingsDestination.List },
                )

            SettingsDestination.Appearance ->
                appearanceContent(
                    modifier,
                    { destination = SettingsDestination.List },
                )

            SettingsDestination.Privacy ->
                privacyContent(
                    modifier,
                    { destination = SettingsDestination.List },
                )
        }
    }
}

private typealias SettingsSectionContent = @Composable (Modifier, () -> Unit) -> Unit

private enum class SettingsDestination {
    List,
    Account,
    Notifications,
    Appearance,
    Privacy,
}

private fun SettingsDestination.animationOrder(): Int =
    when (this) {
        SettingsDestination.List -> 0
        SettingsDestination.Account -> 1
        SettingsDestination.Notifications -> 2
        SettingsDestination.Appearance -> 3
        SettingsDestination.Privacy -> 4
    }

@DevicePreviews
@Preview
@Composable
private fun SettingsRoutePreview() {
    LiveChatPreviewContainer {
        SettingsRoute()
    }
}
