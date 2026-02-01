package com.edufelip.livechat.ui.features.calls

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import com.edufelip.livechat.preview.DevicePreviews
import com.edufelip.livechat.preview.LiveChatPreviewContainer
import com.edufelip.livechat.ui.features.calls.screens.CallsScreen
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CallsRoute(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        CallsScreen(modifier = modifier)
        return
    }
    CallsScreen(modifier = modifier)
}

@DevicePreviews
@Preview
@Composable
private fun CallsRoutePreview() {
    LiveChatPreviewContainer {
        CallsRoute()
    }
}
