package com.edufelip.livechat.ui.common.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * iOS implementation of the Settings submenu back handler.
 *
 * iOS already provides native swipe-back gestures through UIKit navigation.
 * This implementation only handles programmatic back navigation (e.g., back button taps).
 */
@Composable
actual fun SettingsSubmenuBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // iOS native swipe-back gesture is handled by UIKit
    // We only need to handle programmatic back navigation
    BackHandler(enabled = enabled, onBack = onBack)
}
