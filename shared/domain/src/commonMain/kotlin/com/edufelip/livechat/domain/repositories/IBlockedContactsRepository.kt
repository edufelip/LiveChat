package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.BlockedContact
import kotlinx.coroutines.flow.Flow

interface IBlockedContactsRepository {
    fun observeBlockedContacts(): Flow<List<BlockedContact>>

    suspend fun blockContact(contact: BlockedContact)

    suspend fun unblockContact(userId: String)
}
