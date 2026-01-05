package com.edufelip.livechat.shared.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edufelip.livechat.data.bridge.FirebaseContactsBridge
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FirebaseEmulatorFunctionsContactsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val emulatorHost = "10.0.2.2"
    private val authPort = 9099
    private val firestorePort = 8080
    private val functionsPort = 5001
    private val projectId = "livechat-emulator"

    @Before
    fun setup() =
        runBlocking {
            FirebaseApp.getApps(context).forEach { it.delete() }
            val options =
                FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApplicationId("1:livechat:android:emulator")
                    .setApiKey("emulator-api-key")
                    .build()
            FirebaseApp.initializeApp(context, options)

            FirebaseAuth.getInstance().useEmulator(emulatorHost, authPort)
            FirebaseFirestore.getInstance().useEmulator(emulatorHost, firestorePort)
            FirebaseFunctions.getInstance().useEmulator(emulatorHost, functionsPort)

            clearAuthEmulator()
            signInWithEmail()
        }

    @After
    fun teardown() {
        FirebaseAuth.getInstance().signOut()
    }

    @Test
    fun phoneExistsReturnsTrueForRegisteredPhone() =
        runBlocking {
            val phone = "+15550001111"
            val uid = "uid-phone-${UUID.randomUUID()}"
            createUserWithPhone(phone, uid)

            val bridge =
                FirebaseContactsBridge(
                    firestore = FirebaseFirestore.getInstance(),
                    functions = FirebaseFunctions.getInstance(),
                    config = firebaseRestConfig(),
                )

            val result = bridge.phoneExists(phone)
            assertTrue(result.exists)
            assertEquals(uid, result.uid)
        }

    @Test
    fun phoneExistsReturnsFalseForUnknownPhone() =
        runBlocking {
            val bridge =
                FirebaseContactsBridge(
                    firestore = FirebaseFirestore.getInstance(),
                    functions = FirebaseFunctions.getInstance(),
                    config = firebaseRestConfig(),
                )

            val result = bridge.phoneExists("+15550009999")
            assertTrue(result.exists.not())
            assertEquals(null, result.uid)
        }

    @Test
    fun phoneExistsManyReturnsRegisteredMatches() =
        runBlocking {
            val phone = "+15550002222"
            val uid = "uid-phone-${UUID.randomUUID()}"
            createUserWithPhone(phone, uid)

            val bridge =
                FirebaseContactsBridge(
                    firestore = FirebaseFirestore.getInstance(),
                    functions = FirebaseFunctions.getInstance(),
                    config = firebaseRestConfig(),
                )

            val result = bridge.phoneExistsMany(listOf(phone, "+15550003333"))
            assertTrue(result.registeredPhones.contains(phone))
            val match = result.matches.firstOrNull { it.phone == phone }
            val matchValue = requireNotNull(match) { "Expected match for $phone" }
            assertEquals(uid, matchValue.uid)
        }

    @Test
    fun phoneExistsManyRejectsEmptyPayload() =
        runBlocking {
            val functions = FirebaseFunctions.getInstance()
            val error =
                runCatching {
                    functions.getHttpsCallable("phoneExistsMany")
                        .call(mapOf("phones" to emptyList<String>()))
                        .await()
                }.exceptionOrNull()

            val functionsError =
                requireNotNull(error as? FirebaseFunctionsException) {
                    "Expected FirebaseFunctionsException but got: ${error?.javaClass?.simpleName}"
                }
            assertEquals(FirebaseFunctionsException.Code.INVALID_ARGUMENT, functionsError.code)
        }

    private fun firebaseRestConfig(): FirebaseRestConfig =
        FirebaseRestConfig(
            projectId = projectId,
            apiKey = "emulator-api-key",
            emulatorHost = emulatorHost,
            emulatorPort = firestorePort,
        )

    private suspend fun signInWithEmail() {
        val auth = FirebaseAuth.getInstance()
        val email = "emulator-${UUID.randomUUID()}@test.local"
        val password = "pass1234"
        auth.createUserWithEmailAndPassword(email, password).await()
        auth.signInWithEmailAndPassword(email, password).await()
    }

    private fun clearAuthEmulator() {
        val url = URL("http://$emulatorHost:$authPort/emulator/v1/projects/$projectId/accounts")
        val connection =
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("Authorization", "Bearer owner")
            }
        connection.responseCode
        connection.disconnect()
    }

    private fun createUserWithPhone(
        phone: String,
        uid: String,
    ) {
        val url = URL("http://$emulatorHost:$authPort/emulator/v1/projects/$projectId/accounts")
        val body = """{"localId":"$uid","phoneNumber":"$phone"}"""
        val connection =
            (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer owner")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                outputStream.use { it.write(body.toByteArray()) }
            }
        val code = connection.responseCode
        if (code !in 200..299) {
            val error = connection.errorStream?.readBytes()?.decodeToString()
            error("Failed to create phone user: $code $error")
        }
        connection.disconnect()
    }
}
