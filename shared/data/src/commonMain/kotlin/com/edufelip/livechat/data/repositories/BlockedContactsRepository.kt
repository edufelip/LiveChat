package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IBlockedContactsRemoteData
import com.edufelip.livechat.domain.models.BlockedContact
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IBlockedContactsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

class BlockedContactsRepository(
    private val remoteData: IBlockedContactsRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IBlockedContactsRepository {
    override fun observeBlockedContacts(): Flow<List<BlockedContact>> =
        sessionProvider.session
            .mapLatest { session ->
                if (session == null) return@mapLatest emptyList()
                remoteData.fetchBlockedContacts(session.userId, session.idToken)
            }
            .flowOn(dispatcher)

    override suspend fun blockContact(contact: BlockedContact) {
        val session = requireSession()
        remoteData.blockContact(session.userId, session.idToken, contact)
    }

    override suspend fun unblockContact(userId: String) {
        val session = requireSession()
        remoteData.unblockContact(session.userId, session.idToken, userId)
    }

    private suspend fun requireSession() =
        sessionProvider.refreshSession(forceRefresh = false)
            ?: error("No active session")
}
