package com.edufelip.livechat.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import com.edufelip.livechat.domain.utils.currentEpochMillis

object AvatarImageCache {
    private const val MAX_ENTRIES = 120
    private const val CACHE_TTL_MS = 30 * 60 * 1000L
    const val MIN_REFRESH_INTERVAL_MS = 5 * 60 * 1000L
    private val cache = LinkedHashMap<String, Entry>()
    private val lock = CacheLock()

    fun getEntry(key: String): Entry? =
        lock.withLock {
            val entry = cache[key] ?: return@withLock null
            cache.remove(key)
            cache[key] = entry
            entry
        }

    fun put(
        key: String,
        bitmap: ImageBitmap,
    ) {
        lock.withLock {
            cache.remove(key)
            cache[key] = Entry(bitmap = bitmap, cachedAt = currentTimeMillis())
            trimToSize(MAX_ENTRIES)
        }
    }

    fun remove(key: String) {
        lock.withLock { cache.remove(key) }
    }

    fun clear() {
        lock.withLock { cache.clear() }
    }

    private fun trimToSize(maxSize: Int) {
        while (cache.size > maxSize) {
            val iterator = cache.entries.iterator()
            if (!iterator.hasNext()) break
            iterator.next()
            iterator.remove()
        }
    }

    fun isStale(entry: Entry): Boolean {
        val age = currentTimeMillis() - entry.cachedAt
        return age >= CACHE_TTL_MS
    }

    fun timeUntilStale(entry: Entry): Long {
        val remaining = (entry.cachedAt + CACHE_TTL_MS) - currentTimeMillis()
        return remaining.coerceAtLeast(0L)
    }

    private fun currentTimeMillis(): Long = currentEpochMillis()

    data class Entry(
        val bitmap: ImageBitmap,
        val cachedAt: Long,
    )
}
