package com.edufelip.livechat.domain.utils

internal expect fun isMainThread(): Boolean

internal fun requireMainThread(operation: String) {
    check(isMainThread()) { "$operation must run on the main thread" }
}
