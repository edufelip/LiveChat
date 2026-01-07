package com.edufelip.livechat.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
actual fun AppLifecycleObserver(
    onForeground: () -> Unit,
    onBackground: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val onForegroundState = rememberUpdatedState(onForeground)
    val onBackgroundState = rememberUpdatedState(onBackground)

    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> onForegroundState.value()
                    Lifecycle.Event.ON_STOP -> onBackgroundState.value()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
