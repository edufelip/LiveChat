package com.edufelip.livechat.domain.utils

internal expect fun isMainThread(): Boolean

internal expect class AtomicFlag(initial: Boolean) {
    fun get(): Boolean

    fun set(value: Boolean)
}

internal object MainThreadGuardConfig {
    private val enabled = AtomicFlag(true)

    var isEnabled: Boolean
        get() = enabled.get()
        set(value) {
            enabled.set(value)
        }
}

internal fun requireMainThread(operation: String) {
    if (!MainThreadGuardConfig.isEnabled) return
    check(isMainThread()) { "$operation must run on the main thread" }
}
