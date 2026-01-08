package com.edufelip.livechat.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformContext(): Any? = LocalContext.current
