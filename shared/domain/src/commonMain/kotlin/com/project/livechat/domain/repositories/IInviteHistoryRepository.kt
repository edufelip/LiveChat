package com.project.livechat.domain.repositories

import com.project.livechat.domain.models.InviteHistoryItem
import kotlinx.coroutines.flow.Flow

interface IInviteHistoryRepository {
    val history: Flow<List<InviteHistoryItem>>

    suspend fun record(invite: InviteHistoryItem)
}
