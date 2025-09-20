package com.project.livechat.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseRestConfig(
    val projectId: String,
    val apiKey: String,
    val usersCollection: String = "users",
    val messagesCollection: String = "messages",
    val conversationsCollection: String = "conversations",
    val websocketEndpoint: String = "",
    val pollingIntervalMs: Long = 5_000L
) {
    val isConfigured: Boolean
        get() = projectId.isNotBlank()

    val queryEndpoint: String
        get() = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents:runQuery"

    val documentsEndpoint: String
        get() = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"
}
