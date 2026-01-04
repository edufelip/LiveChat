package com.edufelip.livechat.ui.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.account.AccountSettingsRoute
import com.edufelip.livechat.ui.features.settings.appearance.AppearanceSettingsRoute
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.features.settings.notifications.NotificationSettingsRoute
import com.edufelip.livechat.ui.features.settings.screens.SettingsScreen
import com.edufelip.livechat.ui.features.settings.screens.SettingsSection
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsNavigationRequest) -> Unit = {},
    onChromeVisibilityChanged: (Boolean) -> Unit = {},
) {
    var activeSection by remember { mutableStateOf<SettingsSection?>(null) }
    val hideChrome =
        activeSection == SettingsSection.Account ||
            activeSection == SettingsSection.Notifications ||
            activeSection == SettingsSection.Appearance

    LaunchedEffect(hideChrome) {
        onChromeVisibilityChanged(!hideChrome)
    }

    if (LocalInspectionMode.current) {
        SettingsScreen(
            modifier = modifier,
            onSectionSelected = onSectionSelected,
        )
        return
    }

    if (activeSection == SettingsSection.Account) {
        AccountSettingsRoute(
            modifier = modifier,
            onBack = { activeSection = null },
        )
        return
    }

    if (activeSection == SettingsSection.Notifications) {
        NotificationSettingsRoute(
            modifier = modifier,
            onBack = { activeSection = null },
        )
        return
    }

    if (activeSection == SettingsSection.Appearance) {
        AppearanceSettingsRoute(
            modifier = modifier,
            onBack = { activeSection = null },
        )
        return
    }

    SettingsScreen(
        modifier = modifier,
        onSectionSelected = { request ->
            if (request.section == SettingsSection.Account) {
                activeSection = request.section
            } else if (request.section == SettingsSection.Notifications) {
                activeSection = request.section
            } else if (request.section == SettingsSection.Appearance) {
                activeSection = request.section
            } else {
                onSectionSelected(request)
            }
        },
    )
}

@DevicePreviews
@Preview
@Composable
private fun SettingsRoutePreview() {
    LiveChatPreviewContainer {
        SettingsRoute()
    }
}
