package com.edufelip.livechat.ui.util

import platform.Foundation.NSLock

internal actual class CacheLock {
    private val lock = NSLock()

    actual fun <T> withLock(block: () -> T): T {
        lock.lock()
        return try {
            block()
        } finally {
            lock.unlock()
        }
    }
}
