package com.edufelip.livechat.composeapp.ui.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.composeapp.preview.DevicePreviews
import com.edufelip.livechat.composeapp.preview.LiveChatPreviewContainer
import com.edufelip.livechat.composeapp.ui.features.settings.screens.SettingsScreen
import com.edufelip.livechat.composeapp.ui.features.settings.screens.SettingsSection
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsSection) -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        SettingsScreen(
            modifier = modifier,
            onSectionSelected = onSectionSelected,
        )
        return
    }

    SettingsScreen(
        modifier = modifier,
        onSectionSelected = onSectionSelected,
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
