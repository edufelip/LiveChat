package com.edufelip.livechat.domain.utils

import platform.Foundation.NSThread
import kotlin.native.concurrent.AtomicInt

internal actual fun isMainThread(): Boolean = NSThread.isMainThread

internal actual class AtomicFlag actual constructor(initial: Boolean) {
    private val atomic = AtomicInt(if (initial) 1 else 0)

    actual fun get(): Boolean = atomic.value != 0

    actual fun set(value: Boolean) {
        atomic.value = if (value) 1 else 0
    }
}
