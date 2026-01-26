package com.edufelip.livechat.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseRestConfig(
    val projectId: String,
    val apiKey: String,
    val emulatorHost: String? = null,
    val emulatorPort: Int? = null,
    val usersCollection: String = "users",
    val messagesCollection: String = "items",
    val conversationsCollection: String = "inboxes",
    val presenceCollection: String = "presence",
    val invitesCollection: String = "invites",
    val websocketEndpoint: String = "",
    val pollingIntervalMs: Long = 5_000L,
    val defaultRegionIso: String? = null,
) {
    val isConfigured: Boolean
        get() = projectId.isNotBlank()

    val queryEndpoint: String
        get() = "${firestoreBaseUrl()}/v1/projects/$projectId/databases/(default)/documents:runQuery"

    val documentsEndpoint: String
        get() = "${firestoreBaseUrl()}/v1/projects/$projectId/databases/(default)/documents"

    private fun firestoreBaseUrl(): String {
        val host = emulatorHost?.takeIf { it.isNotBlank() }
        val port = emulatorPort?.takeIf { it > 0 }
        return if (host != null && port != null) {
            "http://$host:$port"
        } else {
            "https://firestore.googleapis.com"
        }
    }
}
