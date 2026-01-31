package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.domain.models.EmailVerificationSession
import com.edufelip.livechat.domain.repositories.IEmailVerificationSessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

class IosEmailVerificationSessionRepository(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IEmailVerificationSessionRepository {
    override suspend fun loadSession(userId: String): EmailVerificationSession? =
        withContext(dispatcher) {
            val key = sessionKey(userId) ?: return@withContext null
            val raw = defaults.stringForKey(key) ?: return@withContext null
            EmailVerificationSessionCodec.decode(raw)
        }

    override suspend fun saveSession(
        userId: String,
        session: EmailVerificationSession,
    ) {
        withContext(dispatcher) {
            val key = sessionKey(userId) ?: return@withContext
            defaults.setObject(EmailVerificationSessionCodec.encode(session), forKey = key)
        }
    }

    override suspend fun clearSession(userId: String) {
        withContext(dispatcher) {
            val key = sessionKey(userId) ?: return@withContext
            defaults.removeObjectForKey(key)
        }
    }

    private fun sessionKey(userId: String): String? = userId.takeIf { it.isNotBlank() }?.let { "$SESSION_KEY_PREFIX$it" }

    private companion object {
        private const val SESSION_KEY_PREFIX = "email_verification_session_"
    }
}
