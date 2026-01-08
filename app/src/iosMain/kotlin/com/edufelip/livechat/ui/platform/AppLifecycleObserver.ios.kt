package com.edufelip.livechat.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationWillEnterForegroundNotification

@Composable
actual fun AppLifecycleObserver(
    onForeground: () -> Unit,
    onBackground: () -> Unit,
) {
    val onForegroundState = rememberUpdatedState(onForeground)
    val onBackgroundState = rememberUpdatedState(onBackground)

    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter
        val foregroundObserver =
            center.addObserverForName(
                name = UIApplicationWillEnterForegroundNotification,
                `object` = null,
                queue = null,
            ) { _ ->
                onForegroundState.value()
            }
        val backgroundObserver =
            center.addObserverForName(
                name = UIApplicationDidEnterBackgroundNotification,
                `object` = null,
                queue = null,
            ) { _ ->
                onBackgroundState.value()
            }
        onDispose {
            center.removeObserver(foregroundObserver)
            center.removeObserver(backgroundObserver)
        }
    }
}
