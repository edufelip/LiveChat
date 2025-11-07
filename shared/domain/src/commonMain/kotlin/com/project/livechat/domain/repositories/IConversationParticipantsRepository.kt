package com.project.livechat.domain.repositories

import com.project.livechat.domain.models.Participant
import kotlinx.coroutines.flow.Flow

interface IConversationParticipantsRepository {
    fun observeParticipant(conversationId: String): Flow<Participant?>

    suspend fun recordReadState(
        conversationId: String,
        lastReadAt: Long,
        lastReadSeq: Long?,
    )

    suspend fun setPinned(
        conversationId: String,
        pinned: Boolean,
        pinnedAt: Long?,
    )

    suspend fun setMuteUntil(
        conversationId: String,
        muteUntil: Long?,
    )

    suspend fun setArchived(
        conversationId: String,
        archived: Boolean,
    )
}
