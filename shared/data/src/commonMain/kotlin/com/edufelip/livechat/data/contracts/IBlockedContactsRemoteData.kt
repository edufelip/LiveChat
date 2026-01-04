package com.edufelip.livechat.data.contracts

import com.edufelip.livechat.domain.models.BlockedContact

interface IBlockedContactsRemoteData {
    suspend fun fetchBlockedContacts(
        userId: String,
        idToken: String,
    ): List<BlockedContact>

    suspend fun blockContact(
        userId: String,
        idToken: String,
        contact: BlockedContact,
    )

    suspend fun unblockContact(
        userId: String,
        idToken: String,
        blockedUserId: String,
    )
}
