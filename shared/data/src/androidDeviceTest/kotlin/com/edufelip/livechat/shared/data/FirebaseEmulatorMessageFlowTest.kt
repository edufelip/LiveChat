package com.edufelip.livechat.shared.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.livechat.data.bridge.FirebaseMessagesBridge
import com.edufelip.livechat.data.bridge.FirebaseStorageBridge
import com.edufelip.livechat.data.files.MediaFileStore
import com.edufelip.livechat.data.local.MessagesLocalDataSource
import com.edufelip.livechat.data.remote.FirebaseMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.remote.STORAGE_BUCKET_URL
import com.edufelip.livechat.data.repositories.AvatarCacheRepository
import com.edufelip.livechat.data.repositories.ConversationParticipantsRepository
import com.edufelip.livechat.data.repositories.MessagesRepository
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.providers.model.UserSession
import com.edufelip.livechat.shared.data.database.LIVE_CHAT_DB_NAME
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.buildLiveChatDatabase
import com.edufelip.livechat.shared.data.database.createAndroidDatabaseBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FirebaseEmulatorMessageFlowTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val emulatorHost = "10.0.2.2"
    private val authPort = 9099
    private val firestorePort = 8080
    private val storagePort = 9199
    private val functionsPort = 5001
    private lateinit var database: LiveChatDatabase
    private lateinit var storage: FirebaseStorage

    @Before
    fun setup() =
        runBlocking {
            System.setProperty("FIREBASE_EMULATOR_ENABLED", "1")
            System.setProperty("FIREBASE_EMULATOR_HOST", emulatorHost)
            System.setProperty("FIREBASE_AUTH_EMULATOR_PORT", authPort.toString())
            System.setProperty("FIREBASE_FIRESTORE_EMULATOR_PORT", firestorePort.toString())
            System.setProperty("FIREBASE_STORAGE_EMULATOR_PORT", storagePort.toString())
            System.setProperty("FIREBASE_FUNCTIONS_EMULATOR_PORT", functionsPort.toString())

            FirebaseApp.getApps(context).forEach { it.delete() }
            val options =
                FirebaseOptions
                    .Builder()
                    .setProjectId("livechat-emulator")
                    .setApplicationId("1:livechat:android:emulator")
                    .setApiKey("emulator-api-key")
                    .build()
            FirebaseApp.initializeApp(context, options)

            FirebaseAuth.getInstance().useEmulator(emulatorHost, authPort)
            FirebaseFirestore.getInstance().useEmulator(emulatorHost, firestorePort)
            FirebaseFunctions.getInstance().useEmulator(emulatorHost, functionsPort)
            storage =
                FirebaseStorage.getInstance(STORAGE_BUCKET_URL).apply {
                    useEmulator(emulatorHost, storagePort)
                }

            MediaFileStore.configure(File(context.filesDir, "media-test").absolutePath)
            context.deleteDatabase(LIVE_CHAT_DB_NAME)
            database = buildLiveChatDatabase(createAndroidDatabaseBuilder(context))
            FirebaseFirestore.getInstance().clearPersistence().await()
        }

    @After
    fun teardown() {
        FirebaseAuth.getInstance().signOut()
        database.close()
        context.deleteDatabase(LIVE_CHAT_DB_NAME)
    }

    @Test
    fun imageMessageDownloadThenReadDeletesRemoteStorage() =
        runBlocking {
            val auth = FirebaseAuth.getInstance()
            val receiverEmail = "receiver-${UUID.randomUUID()}@test.local"
            val senderEmail = "sender-${UUID.randomUUID()}@test.local"
            val password = "pass1234"

            val receiverResult = auth.createUserWithEmailAndPassword(receiverEmail, password).await()
            val receiverId = receiverResult.user?.uid ?: error("Missing receiver uid")
            auth.signOut()

            val senderResult = auth.createUserWithEmailAndPassword(senderEmail, password).await()
            val senderId = senderResult.user?.uid ?: error("Missing sender uid")

            val senderSession = InMemoryUserSessionProvider(UserSession(userId = senderId, idToken = ""))
            val (senderRepo, _) = buildRepository(senderSession)

            val mediaFile =
                File(context.cacheDir, "img-${UUID.randomUUID()}.jpg").apply {
                    writeBytes(byteArrayOf(1, 2, 3, 4, 5))
                }
            val draft =
                MessageDraft(
                    conversationId = receiverId,
                    senderId = senderId,
                    body = mediaFile.absolutePath,
                    localId = "local-${UUID.randomUUID()}",
                    createdAt = System.currentTimeMillis(),
                    contentType = MessageContentType.Image,
                )
            senderRepo.sendMessage(draft)

            auth.signOut()
            auth.signInWithEmailAndPassword(receiverEmail, password).await()
            val receiverSession = InMemoryUserSessionProvider(UserSession(userId = receiverId, idToken = ""))
            val (receiverRepo, receiverLocal) = buildRepository(receiverSession)

            val messages =
                withTimeout(10_000) {
                    receiverRepo
                        .observeConversation(senderId, pageSize = 50)
                        .first { list -> list.any { it.senderId == senderId } }
                }
            val received = messages.first { it.senderId == senderId }
            assertTrue(File(received.body).exists())
            val remoteUrl = received.metadata["remoteUrl"]
            val remoteUrlValue = requireNotNull(remoteUrl) { "Missing remoteUrl for received message" }

            receiverRepo.markConversationAsRead(
                conversationId = senderId,
                lastReadAt = received.createdAt,
                lastReadSeq = null,
            )

            val stored = receiverLocal.getMessages(senderId)
            val storedMessage = stored.firstOrNull { it.id == received.id } ?: error("Missing local message")
            assertTrue(storedMessage.metadata.containsKey("mediaDeletedAt"))

            val downloadResult =
                runCatching {
                    storage.getReferenceFromUrl(remoteUrlValue).getBytes(1).await()
                }
            assertTrue(downloadResult.isFailure)
            val exception = downloadResult.exceptionOrNull() as? StorageException
            assertEquals(StorageException.ERROR_OBJECT_NOT_FOUND, exception?.errorCode)
        }

    private fun buildRepository(sessionProvider: InMemoryUserSessionProvider): Pair<MessagesRepository, MessagesLocalDataSource> {
        val localData = MessagesLocalDataSource(database)
        val participantsRepository = ConversationParticipantsRepository(localData, sessionProvider)
        val messagesBridge = FirebaseMessagesBridge(FirebaseFirestore.getInstance(), firebaseRestConfig())
        val storageBridge = FirebaseStorageBridge(storage)
        val avatarCache = AvatarCacheRepository(database.avatarCacheDao(), storageBridge, Dispatchers.IO)
        val remoteData =
            FirebaseMessagesRemoteData(
                messagesBridge = messagesBridge,
                storageBridge = storageBridge,
                config = firebaseRestConfig(),
                sessionProvider = sessionProvider,
                dispatcher = Dispatchers.IO,
            )
        val repository =
            MessagesRepository(
                remoteData = remoteData,
                localData = localData,
                sessionProvider = sessionProvider,
                participantsRepository = participantsRepository,
                avatarCache = avatarCache,
                dispatcher = Dispatchers.IO,
            )
        return repository to localData
    }

    private fun firebaseRestConfig(): FirebaseRestConfig =
        FirebaseRestConfig(
            projectId = "livechat-emulator",
            apiKey = "emulator-api-key",
            emulatorHost = emulatorHost,
            emulatorPort = firestorePort,
        )
}
