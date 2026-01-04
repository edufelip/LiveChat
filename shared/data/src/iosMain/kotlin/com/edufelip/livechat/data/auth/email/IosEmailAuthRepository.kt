package com.edufelip.livechat.data.auth.email

import com.edufelip.livechat.data.bridge.AuthBridge
import com.edufelip.livechat.data.bridge.AuthBridgeError
import com.edufelip.livechat.domain.repositories.IEmailAuthRepository

class IosEmailAuthRepository(
    private val authBridge: AuthBridge,
) : IEmailAuthRepository {
    override suspend fun sendVerificationEmail(email: String) {
        val error = authBridge.sendEmailVerification(email)
        if (error != null) {
            throw error.asException()
        }
    }

    override suspend fun isEmailUpdated(expectedEmail: String): Boolean {
        val state = authBridge.reloadCurrentUser()
        val error = state.error
        if (error != null) {
            throw error.asException()
        }
        val normalized = expectedEmail.trim()
        val currentEmail = state.email?.trim()
        return currentEmail.equals(normalized, ignoreCase = true) && state.isEmailVerified
    }
}

private fun AuthBridgeError.asException(): IllegalStateException {
    val message = message ?: "Authentication error"
    return IllegalStateException(message)
}
