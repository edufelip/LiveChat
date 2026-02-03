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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update

class AccountRepository(
    private val remoteData: IAccountRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val storageBridge: MediaStorageBridge,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IAccountRepository {
    private val profileCache = MutableStateFlow<AccountProfile?>(null)
    private val pendingUpdates = MutableStateFlow<PendingProfileUpdates?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeAccountProfile(): Flow<AccountProfile?> =
        merge(
            profileCache.filterNotNull(),
            sessionProvider.session.mapLatest { session ->
                if (session == null) {
                    pendingUpdates.value = null
                    profileCache.value = null
                    return@mapLatest null
                }
                val fallbackPhone = session.phoneNumber?.takeIf { it.isNotBlank() }
                val profile = remoteData.fetchAccountProfile(session.userId, session.idToken)
                val mergedProfile =
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
                val resolvedProfile = applyPendingOverrides(mergedProfile)
                profileCache.value = resolvedProfile
                resolvedProfile
            },
        ).distinctUntilChanged().flowOn(dispatcher)

    override suspend fun updateDisplayName(displayName: String) {
        println("📦 AccountRepository.updateDisplayName: '$displayName'")
        val session = requireSession(forceRefresh = false)
        println("  - Session: userId=${session.userId}, token=${session.idToken.take(10)}...")
        remoteData.updateDisplayName(session.userId, session.idToken, displayName)
        println("  ✅ Remote update completed")
        setPendingUpdate(displayName = displayName)
        profileCache.update { current ->
            val base =
                current
                    ?: AccountProfile(
                        userId = session.userId,
                        displayName = "",
                        phoneNumber = session.phoneNumber,
                    )
            base.copy(
                displayName = displayName,
                phoneNumber = base.phoneNumber ?: session.phoneNumber,
            )
        }
    }

    override suspend fun updateStatusMessage(statusMessage: String) {
        println("📦 AccountRepository.updateStatusMessage: '$statusMessage'")
        val session = requireSession(forceRefresh = false)
        println("  - Session: userId=${session.userId}, token=${session.idToken.take(10)}...")
        remoteData.updateStatusMessage(session.userId, session.idToken, statusMessage)
        println("  ✅ Remote update completed")
        setPendingUpdate(statusMessage = statusMessage)
        profileCache.update { current ->
            val base =
                current
                    ?: AccountProfile(
                        userId = session.userId,
                        displayName = "",
                        phoneNumber = session.phoneNumber,
                    )
            base.copy(
                statusMessage = statusMessage,
                phoneNumber = base.phoneNumber ?: session.phoneNumber,
            )
        }
    }

    override suspend fun updateEmail(email: String) {
        println("📦 AccountRepository.updateEmail: '$email'")
        val session = requireSession(forceRefresh = false)
        println("  - Session: userId=${session.userId}, token=${session.idToken.take(10)}...")
        remoteData.updateEmail(session.userId, session.idToken, email)
        println("  ✅ Remote update completed")
        setPendingUpdate(email = email)
        profileCache.update { current ->
            val base =
                current
                    ?: AccountProfile(
                        userId = session.userId,
                        displayName = "",
                        phoneNumber = session.phoneNumber,
                    )
            base.copy(
                email = email,
                phoneNumber = base.phoneNumber ?: session.phoneNumber,
            )
        }
    }

    override suspend fun updatePhoto(localPath: String): String {
        val session = requireSession(forceRefresh = false)
        val previousPhotoUrl = profileCache.value?.photoUrl?.takeIf { it.isNotBlank() }
        val bytes = MediaFileStore.readBytes(localPath) ?: error("Missing photo at $localPath")
        val uploadBytes =
            ImageCompressor.compressJpeg(
                bytes = bytes,
                maxDimensionPx = PROFILE_PHOTO_MAX_DIMENSION_PX,
                qualityPercent = PROFILE_PHOTO_JPEG_QUALITY,
            ) ?: bytes
        val objectPath = "profile_photos/${session.userId}/avatar.jpg"
        val downloadUrl = storageBridge.uploadBytes(objectPath, uploadBytes)
        remoteData.updatePhotoUrl(session.userId, session.idToken, downloadUrl)
        setPendingUpdate(photoUrl = downloadUrl)
        profileCache.update { current ->
            val base =
                current
                    ?: AccountProfile(
                        userId = session.userId,
                        displayName = "",
                        phoneNumber = session.phoneNumber,
                    )
            base.copy(
                photoUrl = downloadUrl,
                phoneNumber = base.phoneNumber ?: session.phoneNumber,
            )
        }
        val encodedObjectPath = objectPath.replace("/", "%2F")
        val shouldDeletePrevious =
            previousPhotoUrl != null &&
                previousPhotoUrl != downloadUrl &&
                !previousPhotoUrl.contains(encodedObjectPath)
        if (shouldDeletePrevious) {
            runCatching { storageBridge.deleteRemote(previousPhotoUrl) }
        }
        return downloadUrl
    }

    override suspend fun deleteAccount() {
        val session = requireSession(forceRefresh = true)
        remoteData.deleteAccount(session.userId, session.idToken)
    }

    private suspend fun requireSession(forceRefresh: Boolean) =
        sessionProvider.refreshSession(forceRefresh)
            ?: error("No active session")

    private fun setPendingUpdate(
        displayName: String? = null,
        statusMessage: String? = null,
        email: String? = null,
        photoUrl: String? = null,
    ) {
        val now = currentEpochMillis()
        val existing = pendingUpdates.value
        pendingUpdates.value =
            PendingProfileUpdates(
                displayName = displayName ?: existing?.displayName,
                statusMessage = statusMessage ?: existing?.statusMessage,
                email = email ?: existing?.email,
                photoUrl = photoUrl ?: existing?.photoUrl,
                updatedAtMillis = now,
            )
    }

    private fun applyPendingOverrides(profile: AccountProfile): AccountProfile {
        val pending = pendingUpdates.value ?: return profile
        val now = currentEpochMillis()
        if (now - pending.updatedAtMillis > PENDING_UPDATE_TIMEOUT_MS) {
            pendingUpdates.value = null
            return profile
        }

        val pendingMatchesRemote =
            (pending.displayName == null || pending.displayName == profile.displayName) &&
                (pending.statusMessage == null || pending.statusMessage == profile.statusMessage) &&
                (pending.email == null || pending.email == profile.email) &&
                (pending.photoUrl == null || pending.photoUrl == profile.photoUrl)

        if (pendingMatchesRemote) {
            pendingUpdates.value = null
            return profile
        }

        return profile.copy(
            displayName = pending.displayName ?: profile.displayName,
            statusMessage = pending.statusMessage ?: profile.statusMessage,
            email = pending.email ?: profile.email,
            photoUrl = pending.photoUrl ?: profile.photoUrl,
        )
    }

    private data class PendingProfileUpdates(
        val displayName: String? = null,
        val statusMessage: String? = null,
        val email: String? = null,
        val photoUrl: String? = null,
        val updatedAtMillis: Long,
    )

    private companion object {
        const val PROFILE_PHOTO_MAX_DIMENSION_PX = 512
        const val PROFILE_PHOTO_JPEG_QUALITY = 82
        const val PENDING_UPDATE_TIMEOUT_MS = 30_000L
    }
}
