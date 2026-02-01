package com.edufelip.livechat.domain.utils

import platform.Foundation.NSThread
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal actual fun isMainThread(): Boolean = NSThread.isMainThread

@OptIn(ExperimentalAtomicApi::class)
internal actual class AtomicFlag actual constructor(
    initial: Boolean,
) {
    private val atomic = AtomicInt(if (initial) 1 else 0)

    actual fun get(): Boolean = atomic.load() != 0

    actual fun set(value: Boolean) {
        atomic.store(if (value) 1 else 0)
    }
}
