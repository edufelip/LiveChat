package com.edufelip.livechat.data.repositories

import android.content.Context
import com.edufelip.livechat.domain.models.EmailVerificationSession
import com.edufelip.livechat.domain.repositories.IEmailVerificationSessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidEmailVerificationSessionRepository(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IEmailVerificationSessionRepository {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override suspend fun loadSession(userId: String): EmailVerificationSession? =
        withContext(dispatcher) {
            val key = sessionKey(userId) ?: return@withContext null
            val raw = preferences.getString(key, null) ?: return@withContext null
            EmailVerificationSessionCodec.decode(raw)
        }

    override suspend fun saveSession(
        userId: String,
        session: EmailVerificationSession,
    ) {
        withContext(dispatcher) {
            val key = sessionKey(userId) ?: return@withContext
            preferences.edit()
                .putString(key, EmailVerificationSessionCodec.encode(session))
                .apply()
        }
    }

    override suspend fun clearSession(userId: String) {
        withContext(dispatcher) {
            val key = sessionKey(userId) ?: return@withContext
            preferences.edit().remove(key).apply()
        }
    }

    private fun sessionKey(userId: String): String? = userId.takeIf { it.isNotBlank() }?.let { "$SESSION_KEY_PREFIX$it" }

    private companion object {
        private const val PREFERENCES_NAME = "livechat_email_verification"
        private const val SESSION_KEY_PREFIX = "email_verification_session_"
    }
}
