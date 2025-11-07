package com.project.livechat.data.repositories

import com.project.livechat.domain.models.InviteHistoryItem
import com.project.livechat.domain.repositories.IInviteHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class InviteHistoryRepository : IInviteHistoryRepository {
    private val mutex = Mutex()
    private val historyFlow = MutableStateFlow<List<InviteHistoryItem>>(emptyList())

    override val history: Flow<List<InviteHistoryItem>> = historyFlow.asStateFlow()

    override suspend fun record(invite: InviteHistoryItem) {
        mutex.withLock {
            historyFlow.value = (listOf(invite) + historyFlow.value).take(MAX_HISTORY)
        }
    }

    private companion object {
        const val MAX_HISTORY = 20
    }
}
