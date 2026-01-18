package com.edufelip.livechat.ui.common.navigation

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
    // Programmatic back navigation is handled by the UI components (e.g. BackButton)
    // No additional back handling is needed here as there's no system back button on iOS
}
