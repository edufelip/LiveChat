package com.edufelip.livechat.ui.platform

import androidx.compose.runtime.Composable

@Composable
expect fun AppLifecycleObserver(
    onForeground: () -> Unit,
    onBackground: () -> Unit,
)
