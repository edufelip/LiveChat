package com.edufelip.livechat.data.auth.email

import com.edufelip.livechat.domain.repositories.IEmailAuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseEmailAuthRepository(
    private val firebaseAuth: FirebaseAuth,
) : IEmailAuthRepository {
    override suspend fun sendVerificationEmail(email: String) {
        val user = firebaseAuth.currentUser ?: error("User not authenticated")
        user.verifyBeforeUpdateEmail(email).await()
    }

    override suspend fun isEmailUpdated(expectedEmail: String): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        user.reload().await()
        val updatedEmail = user.email?.trim()
        return updatedEmail.equals(expectedEmail.trim(), ignoreCase = true) && user.isEmailVerified
    }
}
