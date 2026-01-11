package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.bridge.ContactsRemoteBridge
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.remote.FirebaseRestContactsRemoteData
import com.edufelip.livechat.data.remote.PhoneExistsBatchResult
import com.edufelip.livechat.data.remote.PhoneExistsMatch
import com.edufelip.livechat.data.remote.PhoneExistsSingleResult
import com.edufelip.livechat.domain.models.Contact
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseRestContactsRemoteDataTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun checkContactsEmitsRegisteredMatches() =
        runTest(dispatcher) {
            val bridge =
                FakeContactsBridge(
                    batchResult =
                        PhoneExistsBatchResult(
                            registeredPhones = listOf("+15550001111"),
                            matches = listOf(PhoneExistsMatch(phone = "+15550001111", uid = "uid-123")),
                        ),
                )
            val httpClient = HttpClient(MockEngine { respond("", HttpStatusCode.OK) })
            val remote =
                FirebaseRestContactsRemoteData(
                    contactsBridge = bridge,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key", defaultRegionIso = "US"),
                    httpClient = httpClient,
                    dispatcher = dispatcher,
                )

            val contacts =
                listOf(
                    Contact(name = "Ada", phoneNo = "+1 (555) 000-1111"),
                    Contact(name = "Bob", phoneNo = ""),
                )

            val results = remote.checkContacts(contacts).toList()
            assertEquals(1, results.size)
            assertEquals("Ada", results.first().name)
            assertTrue(results.first().isRegistered)
            assertEquals("uid-123", results.first().firebaseUid)
            assertEquals(listOf("+15550001111"), bridge.lastBatchPhones)
        }

    @Test
    fun checkContactsFallsBackToSingleLookupWhenUidMissing() =
        runTest(dispatcher) {
            val bridge =
                FakeContactsBridge(
                    batchResult =
                        PhoneExistsBatchResult(
                            registeredPhones = listOf("+15550002222"),
                            matches = emptyList(),
                        ),
                    singleResult = PhoneExistsSingleResult(exists = true, uid = "uid-456"),
                )
            val httpClient = HttpClient(MockEngine { respond("", HttpStatusCode.OK) })
            val remote =
                FirebaseRestContactsRemoteData(
                    contactsBridge = bridge,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key", defaultRegionIso = "US"),
                    httpClient = httpClient,
                    dispatcher = dispatcher,
                )

            val contacts = listOf(Contact(name = "Ben", phoneNo = "+1 (555) 000-2222"))

            val results = remote.checkContacts(contacts).toList()
            assertEquals(1, results.size)
            assertEquals("uid-456", results.first().firebaseUid)
            assertEquals("+15550002222", bridge.lastSinglePhone)
        }

    @Test
    fun inviteContactReturnsTrueOnSuccess() =
        runTest(dispatcher) {
            val bridge = FakeContactsBridge()
            var requestedUrl: String? = null
            val engine =
                MockEngine { request ->
                    requestedUrl = request.url.toString()
                    respond("", HttpStatusCode.OK)
                }
            val remote =
                FirebaseRestContactsRemoteData(
                    contactsBridge = bridge,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    httpClient =
                        HttpClient(engine) {
                            install(ContentNegotiation) {
                                json()
                            }
                        },
                    dispatcher = dispatcher,
                )

            val result = remote.inviteContact(Contact(name = "Ada", phoneNo = "+15550001111"))
            assertTrue(result)
            val url = requestedUrl ?: error("Expected a request to be issued")
            assertTrue(url.contains("/documents/invites"))
        }

    @Test
    fun inviteContactReturnsFalseOnFailure() =
        runTest(dispatcher) {
            val bridge = FakeContactsBridge()
            val engine =
                MockEngine {
                    throw IllegalStateException("network error")
                }
            val remote =
                FirebaseRestContactsRemoteData(
                    contactsBridge = bridge,
                    config = FirebaseRestConfig(projectId = "demo", apiKey = "key"),
                    httpClient =
                        HttpClient(engine) {
                            install(ContentNegotiation) {
                                json()
                            }
                        },
                    dispatcher = dispatcher,
                )

            val result = remote.inviteContact(Contact(name = "Ada", phoneNo = "+15550001111"))
            assertFalse(result)
        }

    private class FakeContactsBridge(
        private val batchResult: PhoneExistsBatchResult = PhoneExistsBatchResult(emptyList(), emptyList()),
        private val singleResult: PhoneExistsSingleResult = PhoneExistsSingleResult(exists = false, uid = null),
    ) : ContactsRemoteBridge {
        var lastBatchPhones: List<String> = emptyList()
        var lastSinglePhone: String? = null

        override suspend fun phoneExists(phoneE164: String): PhoneExistsSingleResult {
            lastSinglePhone = phoneE164
            return singleResult
        }

        override suspend fun phoneExistsMany(phones: List<String>): PhoneExistsBatchResult {
            lastBatchPhones = phones
            return batchResult
        }

        override suspend fun isUserRegistered(phoneE164: String): Boolean = false
    }
}
