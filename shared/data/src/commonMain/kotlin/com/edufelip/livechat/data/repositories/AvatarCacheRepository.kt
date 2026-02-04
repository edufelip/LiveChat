package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.files.MediaFileStore
import com.edufelip.livechat.domain.utils.currentEpochMillis
import com.edufelip.livechat.shared.data.database.AvatarCacheDao
import com.edufelip.livechat.shared.data.database.AvatarCacheEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AvatarCacheRepository(
    private val cacheDao: AvatarCacheDao,
    private val storageBridge: MediaStorageBridge,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val cacheVersion = MutableStateFlow(0L)
    val changes: StateFlow<Long> = cacheVersion

    suspend fun getCachedLocalPath(
        ownerId: String,
        remoteUrl: String,
    ): String? =
        withContext(dispatcher) {
            if (ownerId.isBlank() || remoteUrl.isBlank()) return@withContext null
            val entry = cacheDao.get(ownerId) ?: return@withContext null
            if (entry.remoteUrl != remoteUrl) return@withContext null
            entry.localPath.takeIf { MediaFileStore.exists(it) }
        }

    suspend fun prefetchAvatar(
        ownerId: String,
        remoteUrl: String,
    ): String? =
        withContext(dispatcher) {
            if (ownerId.isBlank() || remoteUrl.isBlank()) return@withContext null
            val entry = cacheDao.get(ownerId)
            val needsRefresh =
                entry == null || entry.remoteUrl != remoteUrl || !MediaFileStore.exists(entry.localPath)
            if (!needsRefresh) return@withContext entry?.localPath

            val previousPath = entry?.localPath
            val bytes =
                runCatching { storageBridge.downloadBytes(remoteUrl, MAX_AVATAR_BYTES) }
                    .getOrNull()
                    ?: return@withContext null
            val newPath = MediaFileStore.saveBytes(prefix = "avatar_", extension = "jpg", data = bytes)
            cacheDao.upsert(
                AvatarCacheEntity(
                    ownerId = ownerId,
                    remoteUrl = remoteUrl,
                    localPath = newPath,
                    updatedAtMillis = currentEpochMillis(),
                ),
            )
            if (!previousPath.isNullOrBlank() && previousPath != newPath) {
                MediaFileStore.delete(previousPath)
            }
            bumpVersion()
            newPath
        }

    suspend fun cacheLocalAvatar(
        ownerId: String,
        remoteUrl: String,
        sourceLocalPath: String,
    ): String? =
        withContext(dispatcher) {
            if (ownerId.isBlank() || remoteUrl.isBlank() || sourceLocalPath.isBlank()) return@withContext null
            val entry = cacheDao.get(ownerId)
            val bytes = MediaFileStore.readBytes(sourceLocalPath) ?: return@withContext null
            val newPath = MediaFileStore.saveBytes(prefix = "avatar_", extension = "jpg", data = bytes)
            cacheDao.upsert(
                AvatarCacheEntity(
                    ownerId = ownerId,
                    remoteUrl = remoteUrl,
                    localPath = newPath,
                    updatedAtMillis = currentEpochMillis(),
                ),
            )
            val previousPath = entry?.localPath
            if (!previousPath.isNullOrBlank() && previousPath != newPath) {
                MediaFileStore.delete(previousPath)
            }
            bumpVersion()
            newPath
        }

    private fun bumpVersion() {
        cacheVersion.value = cacheVersion.value + 1
    }

    private companion object {
        const val MAX_AVATAR_BYTES = 5L * 1024L * 1024L
    }
}
