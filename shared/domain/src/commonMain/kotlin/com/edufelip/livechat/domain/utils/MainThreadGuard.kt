package com.edufelip.livechat.domain.utils

internal expect fun isMainThread(): Boolean

internal object MainThreadGuardConfig {
    var isEnabled: Boolean = true
}

internal fun requireMainThread(operation: String) {
    if (!MainThreadGuardConfig.isEnabled) return
    check(isMainThread()) { "$operation must run on the main thread" }
}
