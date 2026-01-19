package com.edufelip.livechat.ui.common.navigation

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic back handler for Settings submenu screens.
 *
 * On Android 13+, this provides predictive back gesture support with visual preview.
 * On Android <13, it uses standard BackHandler without visual preview.
 * On iOS, a left-edge swipe gesture triggers the provided back callback.
 *
 * @param enabled Whether the back handler is currently enabled
 * @param onBack Callback invoked when the back gesture or button is triggered
 */
@Composable
expect fun SettingsSubmenuBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
