package com.edufelip.livechat.domain.repositories

import com.edufelip.livechat.domain.models.InviteHistoryItem
import kotlinx.coroutines.flow.Flow

interface IInviteHistoryRepository {
    val history: Flow<List<InviteHistoryItem>>

    suspend fun record(invite: InviteHistoryItem)
}
