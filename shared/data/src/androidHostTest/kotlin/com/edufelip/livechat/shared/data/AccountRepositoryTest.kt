package com.edufelip.livechat.shared.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.data.repositories.AccountRepository
import com.edufelip.livechat.data.repositories.AvatarCacheRepository
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.models.AccountProfile
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.shared.data.database.AvatarCacheDao
import com.edufelip.livechat.shared.data.database.AvatarCacheEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AccountRepositoryTest {
    @Test
    fun observeAccountProfile_keepsPendingDisplayNameUntilRemoteMatches() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val sessionProvider =
                InMemoryUserSessionProvider(
                    UserSession(
                        userId = "user-123",
                        idToken = "token-1",
                        phoneNumber = "+15551234567",
                    ),
                    dispatcher = dispatcher,
                )
            val remoteData =
                FakeAccountRemoteData(
                    profile =
                        AccountProfile(
                            userId = "user-123",
                            displayName = "old",
                            phoneNumber = "+15551234567",
                        ),
                )
            val repository =
                AccountRepository(
                    remoteData = remoteData,
                    sessionProvider = sessionProvider,
                    storageBridge = FakeMediaStorageBridge,
                    avatarCache = AvatarCacheRepository(FakeAvatarCacheDao(), FakeMediaStorageBridge, dispatcher),
                    dispatcher = dispatcher,
                )

            val emissions = mutableListOf<AccountProfile?>()
            val job =
                launch {
                    repository.observeAccountProfile().collect { emissions.add(it) }
                }

            advanceUntilIdle()

            repository.updateDisplayName("new")
            sessionProvider.setSession(
                UserSession(
                    userId = "user-123",
                    idToken = "token-2",
                    phoneNumber = "+15551234567",
                ),
            )

            advanceUntilIdle()
            assertEquals("new", emissions.last()?.displayName)

            remoteData.profile = remoteData.profile?.copy(displayName = "new")
            sessionProvider.setSession(
                UserSession(
                    userId = "user-123",
                    idToken = "token-3",
                    phoneNumber = "+15551234567",
                ),
            )

            advanceUntilIdle()
            assertEquals("new", emissions.last()?.displayName)

            job.cancel()
        }

    @Test
    fun updatePhoto_skipsDeletingCurrentAvatarObject() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val sessionProvider =
                InMemoryUserSessionProvider(
                    UserSession(
                        userId = "user-123",
                        idToken = "token-1",
                        phoneNumber = "+15551234567",
                    ),
                    dispatcher = dispatcher,
                )
            val previousUrl = buildDownloadUrl("user-123", token = "old-token")
            val remoteData =
                FakeAccountRemoteData(
                    profile =
                        AccountProfile(
                            userId = "user-123",
                            displayName = "",
                            phoneNumber = "+15551234567",
                            photoUrl = previousUrl,
                        ),
                )
            val storageBridge = RecordingMediaStorageBridge("https://example.com/files/avatar.jpg")
            val repository =
                AccountRepository(
                    remoteData = remoteData,
                    sessionProvider = sessionProvider,
                    storageBridge = storageBridge,
                    avatarCache = AvatarCacheRepository(FakeAvatarCacheDao(), storageBridge, dispatcher),
                    dispatcher = dispatcher,
                )

            val job = launch { repository.observeAccountProfile().collect {} }
            advanceUntilIdle()

            storageBridge.downloadUrl = buildDownloadUrl("user-123", token = "new-token")
            val tempFile = File.createTempFile("avatar", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
            repository.updatePhoto(tempFile.absolutePath)

            assertEquals(0, storageBridge.deleteRequests.size)
            job.cancel()
        }

    private class FakeAccountRemoteData(
        var profile: AccountProfile?,
    ) : IAccountRemoteData {
        override suspend fun fetchAccountProfile(
            userId: String,
            idToken: String,
        ): AccountProfile? = profile

        override suspend fun updateDisplayName(
            userId: String,
            idToken: String,
            displayName: String,
        ) = Unit

        override suspend fun updateStatusMessage(
            userId: String,
            idToken: String,
            statusMessage: String,
        ) = Unit

        override suspend fun updateEmail(
            userId: String,
            idToken: String,
            email: String,
        ) = Unit

        override suspend fun updatePhotoUrl(
            userId: String,
            idToken: String,
            photoUrl: String,
        ) = Unit

        override suspend fun ensureUserDocument(
            userId: String,
            idToken: String,
            phoneNumber: String?,
        ) = Unit

        override suspend fun deleteAccount(
            userId: String,
            idToken: String,
        ) = Unit
    }

    private class RecordingMediaStorageBridge(
        var downloadUrl: String,
    ) : MediaStorageBridge {
        val deleteRequests = mutableListOf<String>()

        override suspend fun uploadBytes(
            objectPath: String,
            bytes: ByteArray,
        ): String = downloadUrl

        override suspend fun downloadBytes(
            remoteUrl: String,
            maxBytes: Long,
        ): ByteArray = byteArrayOf()

        override suspend fun deleteRemote(remoteUrl: String) {
            deleteRequests += remoteUrl
        }
    }

    private object FakeMediaStorageBridge : MediaStorageBridge {
        override suspend fun uploadBytes(
            objectPath: String,
            bytes: ByteArray,
        ): String = "https://example.com/media.jpg"

        override suspend fun downloadBytes(
            remoteUrl: String,
            maxBytes: Long,
        ): ByteArray = byteArrayOf()

        override suspend fun deleteRemote(remoteUrl: String) = Unit
    }

    private fun buildDownloadUrl(
        userId: String,
        token: String,
    ): String {
        val path = "profile_photos/$userId/avatar.jpg".replace("/", "%2F")
        return "https://firebasestorage.googleapis.com/v0/b/app/o/$path?alt=media&token=$token"
    }

    private class FakeAvatarCacheDao : AvatarCacheDao {
        private val store = mutableMapOf<String, AvatarCacheEntity>()

        override suspend fun get(ownerId: String): AvatarCacheEntity? = store[ownerId]

        override suspend fun upsert(entity: AvatarCacheEntity) {
            store[entity.ownerId] = entity
        }

        override suspend fun delete(ownerId: String) {
            store.remove(ownerId)
        }

        override suspend fun deleteAll() {
            store.clear()
        }
    }
}
