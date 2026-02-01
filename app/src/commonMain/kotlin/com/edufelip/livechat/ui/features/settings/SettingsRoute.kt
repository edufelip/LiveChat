package com.edufelip.livechat.ui.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.settings.model.SettingsNavigationRequest
import com.edufelip.livechat.ui.features.settings.screens.SettingsScreen
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    onSectionSelected: (SettingsNavigationRequest) -> Unit = {},
) {
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
