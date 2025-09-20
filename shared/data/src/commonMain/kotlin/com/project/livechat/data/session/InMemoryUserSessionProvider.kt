package com.project.livechat.data.session

import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.domain.providers.model.UserSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class InMemoryUserSessionProvider(
    initialSession: UserSession? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : UserSessionProvider {

    private val sessionState = MutableStateFlow(initialSession)

    override val session: Flow<UserSession?>
        get() = sessionState.asStateFlow()

    override suspend fun refreshSession(forceRefresh: Boolean): UserSession? {
        return sessionState.value
    }

    override fun currentUserId(): String? = sessionState.value?.userId

    suspend fun updateSession(session: UserSession?) {
        withContext(dispatcher) {
            sessionState.emit(session)
        }
    }

    fun setSession(session: UserSession?) {
        sessionState.value = session
    }
}
