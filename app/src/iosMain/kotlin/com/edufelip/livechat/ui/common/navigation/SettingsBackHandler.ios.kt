package com.edufelip.livechat.ui.common.navigation

import androidx.compose.runtime.Composable

/**
 * iOS implementation of the Settings submenu back handler.
 *
 * iOS uses an edge-swipe gesture to trigger the provided back callback.
 */
@Composable
actual fun SettingsSubmenuBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    PlatformBackGestureHandler(
        enabled = enabled,
        onBack = onBack,
    )
}
