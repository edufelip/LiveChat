package com.edufelip.livechat.ui.common.navigation

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CancellationException

/**
 * Android implementation of the Settings submenu back handler.
 *
 * Uses PredictiveBackHandler on Android 13+ for modern gesture support,
 * falls back to standard BackHandler on earlier versions.
 */
@Composable
actual fun SettingsSubmenuBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PredictiveBackHandler(enabled = enabled) { progress ->
            try {
                // Collect the back gesture progress for visual preview
                progress.collect { backEvent ->
                    // The system handles the visual preview automatically
                    // We just need to collect the flow
                }
                // Gesture completed - trigger navigation
                onBack()
            } catch (e: CancellationException) {
                // User cancelled the gesture - do nothing
            }
        }
    } else {
        // Android <13: Use standard BackHandler without predictive animation
        BackHandler(enabled = enabled, onBack = onBack)
    }
}
