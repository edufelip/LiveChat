package com.edufelip.livechat.ui.common.navigation

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * Android implementation of the Settings submenu back handler.
 *
 * Uses Navigation Event APIs on Android 13+ for predictive gesture support,
 * falls back to standard BackHandler on earlier versions.
 */
@Composable
actual fun SettingsSubmenuBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    val onBackUpdated by rememberUpdatedState(onBack)
    val navigationEventState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = navigationEventState,
        isBackEnabled = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU,
        onBackCancelled = {},
        onBackCompleted = { onBackUpdated() },
    )

    // Android <13: Use standard BackHandler without predictive animation.
    BackHandler(
        enabled = enabled && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU,
        onBack = onBackUpdated,
    )
}
