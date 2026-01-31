package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.data.files.MediaFileStore
import com.edufelip.livechat.data.media.ImageCompressor
import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IAccountRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

class AccountRepository(
    private val remoteData: IAccountRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val storageBridge: MediaStorageBridge,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAccountRepository {
    override fun observeAccountProfile(): Flow<AccountProfile?> =
        sessionProvider.session
            .mapLatest { session ->
                if (session == null) return@mapLatest null
                val fallbackPhone = session.phoneNumber?.takeIf { it.isNotBlank() }
                val profile = remoteData.fetchAccountProfile(session.userId, session.idToken)
                when {
                    profile == null ->
                        AccountProfile(
                            userId = session.userId,
                            displayName = "",
                            phoneNumber = fallbackPhone,
                        )
                    profile.phoneNumber.isNullOrBlank() && fallbackPhone != null ->
                        profile.copy(phoneNumber = fallbackPhone)
                    else -> profile
                }
            }
            .flowOn(dispatcher)

    override suspend fun updateDisplayName(displayName: String) {
        val session = requireSession(forceRefresh = false)
        remoteData.updateDisplayName(session.userId, session.idToken, displayName)
    }

    override suspend fun updateStatusMessage(statusMessage: String) {
        val session = requireSession(forceRefresh = false)
        remoteData.updateStatusMessage(session.userId, session.idToken, statusMessage)
    }

    override suspend fun updateEmail(email: String) {
        val session = requireSession(forceRefresh = false)
        remoteData.updateEmail(session.userId, session.idToken, email)
    }

    override suspend fun updatePhoto(localPath: String): String {
        val session = requireSession(forceRefresh = false)
        val bytes = MediaFileStore.readBytes(localPath) ?: error("Missing photo at $localPath")
        val uploadBytes =
            ImageCompressor.compressJpeg(
                bytes = bytes,
                maxDimensionPx = PROFILE_PHOTO_MAX_DIMENSION_PX,
                qualityPercent = PROFILE_PHOTO_JPEG_QUALITY,
            ) ?: bytes
        val objectPath = "profile_photos/${session.userId}/${currentEpochMillis()}.jpg"
        val downloadUrl = storageBridge.uploadBytes(objectPath, uploadBytes)
        remoteData.updatePhotoUrl(session.userId, session.idToken, downloadUrl)
        return downloadUrl
    }

    override suspend fun deleteAccount() {
        val session = requireSession(forceRefresh = true)
        remoteData.deleteAccount(session.userId, session.idToken)
    }

    private suspend fun requireSession(forceRefresh: Boolean) =
        sessionProvider.refreshSession(forceRefresh)
            ?: error("No active session")

    private companion object {
        const val PROFILE_PHOTO_MAX_DIMENSION_PX = 512
        const val PROFILE_PHOTO_JPEG_QUALITY = 82
    }
}
