package com.edufelip.livechat.ui.common.navigation

import androidx.compose.runtime.Composable

/**
 * Platform-specific edge swipe back handler.
 *
 * On iOS, this attaches a left-edge swipe gesture to trigger [onBack].
 * On Android, this is a no-op because system back is already handled.
 */
@Composable
expect fun PlatformBackGestureHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
