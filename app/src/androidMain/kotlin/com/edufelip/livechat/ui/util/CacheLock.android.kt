package com.edufelip.livechat.ui.util

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal actual class CacheLock {
    private val lock = ReentrantLock()

    actual fun <T> withLock(block: () -> T): T = lock.withLock(block)
}
