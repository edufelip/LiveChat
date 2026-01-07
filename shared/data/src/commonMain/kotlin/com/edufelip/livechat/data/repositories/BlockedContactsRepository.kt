package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IBlockedContactsRemoteData
import com.edufelip.livechat.domain.models.BlockedContact
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IBlockedContactsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockedContactsRepository(
    private val remoteData: IBlockedContactsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IBlockedContactsRepository {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val blockedState = MutableStateFlow<List<BlockedContact>>(emptyList())

    init {
        scope.launch {
            sessionProvider.session.collectLatest { session ->
                if (session == null) {
                    blockedState.value = emptyList()
                    return@collectLatest
                }
                blockedState.value =
                    runCatching { remoteData.fetchBlockedContacts(session.userId, session.idToken) }
                        .getOrDefault(emptyList())
            }
        }
    }

    override fun observeBlockedContacts(): Flow<List<BlockedContact>> = blockedState.asStateFlow()

    override suspend fun blockContact(contact: BlockedContact) {
        val session = requireSession()
        remoteData.blockContact(session.userId, session.idToken, contact)
        blockedState.value =
            blockedState.value.filterNot { it.userId == contact.userId } +
            contact
    }

    override suspend fun unblockContact(userId: String) {
        val session = requireSession()
        remoteData.unblockContact(session.userId, session.idToken, userId)
        blockedState.value = blockedState.value.filterNot { it.userId == userId }
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
