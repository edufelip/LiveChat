package com.edufelip.livechat.data.bridge

data class AuthBridgeError(
    val domain: String? = null,
    val code: Long? = null,
    val message: String? = null,
)

data class AuthBridgeUserState(
    val email: String? = null,
    val isEmailVerified: Boolean = false,
    val error: AuthBridgeError? = null,
)

interface AuthBridge {
    fun signOut()

    suspend fun sendEmailVerification(email: String): AuthBridgeError?

    suspend fun reloadCurrentUser(): AuthBridgeUserState
}
