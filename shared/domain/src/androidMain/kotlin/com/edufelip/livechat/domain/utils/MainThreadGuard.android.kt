package com.edufelip.livechat.domain.utils

import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

internal actual fun isMainThread(): Boolean {
    val mainLooper = Looper.getMainLooper() ?: return true
    return Looper.myLooper() == mainLooper
}

internal actual class AtomicFlag actual constructor(initial: Boolean) {
    private val atomic = AtomicBoolean(initial)

    actual fun get(): Boolean = atomic.get()

    actual fun set(value: Boolean) {
        atomic.set(value)
    }
}
