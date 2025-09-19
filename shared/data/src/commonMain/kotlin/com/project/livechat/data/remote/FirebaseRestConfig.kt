package com.project.livechat.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseRestConfig(
    val projectId: String,
    val apiKey: String,
    val usersCollection: String = "users"
) {
    val isConfigured: Boolean
        get() = projectId.isNotBlank()

    val endpoint: String
        get() = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents:runQuery"
}
