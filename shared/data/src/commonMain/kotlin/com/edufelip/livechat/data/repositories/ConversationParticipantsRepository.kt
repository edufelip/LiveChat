package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.domain.models.Participant
import com.edufelip.livechat.domain.models.ParticipantRole
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class ConversationParticipantsRepository(
    private val localData: IMessagesLocalData,
    private val sessionProvider: UserSessionProvider,
) : IConversationParticipantsRepository {

    override fun observeParticipant(conversationId: String): Flow<Participant?> =
        localData.observeParticipant(conversationId).onEach { participant ->
            if (participant == null) {
                ensureParticipant(conversationId)
            }
        }

    override suspend fun recordReadState(
        conversationId: String,
        lastReadAt: Long,
        lastReadSeq: Long?,
    ) {
        val participant = ensureParticipant(conversationId)
        localData.upsertParticipant(
            participant.copy(
                lastReadAt = lastReadAt,
                lastReadSeq = lastReadSeq ?: participant.lastReadSeq,
            ),
        )
    }

    override suspend fun setPinned(
        conversationId: String,
        pinned: Boolean,
        pinnedAt: Long?,
    ) {
        val participant = ensureParticipant(conversationId)
        localData.upsertParticipant(participant.copy(pinned = pinned, pinnedAt = pinnedAt))
    }

    override suspend fun setMuteUntil(
        conversationId: String,
        muteUntil: Long?,
    ) {
        val participant = ensureParticipant(conversationId)
        localData.upsertParticipant(participant.copy(muteUntil = muteUntil))
    }

    override suspend fun setArchived(
        conversationId: String,
        archived: Boolean,
    ) {
        val participant = ensureParticipant(conversationId)
        localData.upsertParticipant(participant.copy(archived = archived))
    }

    private suspend fun ensureParticipant(conversationId: String): Participant {
        val existing = localData.getParticipant(conversationId)
        if (existing != null) return existing

        val userId =
            sessionProvider.currentUserId()
                ?: error("User session missing – cannot manage conversation state.")
        val participant =
            Participant(
                conversationId = conversationId,
                userId = userId,
                role = ParticipantRole.Member,
                joinedAt = currentEpochMillis(),
                lastReadAt = 0L,
            )
        localData.upsertParticipant(participant)
        return participant
    }
}
