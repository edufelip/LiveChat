package com.edufelip.livechat.ui.util

internal expect class CacheLock() {
    fun <T> withLock(block: () -> T): T
}
