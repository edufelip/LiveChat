package com.edufelip.livechat.ui.platform

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformContext(): Any? {
    val context = LocalContext.current
    // Try to find the Activity from the context chain
    return context.findActivity() ?: context
}

private tailrec fun Context.findActivity(): Context? =
    when (this) {
        is android.app.Activity -> this
        is ContextWrapper -> baseContext?.findActivity()
        else -> null
    }
