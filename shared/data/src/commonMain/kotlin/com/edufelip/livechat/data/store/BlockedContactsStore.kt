package com.edufelip.livechat.data.store

import com.edufelip.livechat.domain.repositories.IBlockedContactsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BlockedContactsStore(
    repository: IBlockedContactsRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val blockedUserIdsState = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds = blockedUserIdsState.asStateFlow()

    init {
        scope.launch {
            repository.observeBlockedContacts()
                .catch { throwable ->
                    println("BlockedContactsStore: observe failed ${throwable.message}")
                }
                .collectLatest { contacts ->
                    blockedUserIdsState.value = contacts.map { it.userId }.toSet()
                }
        }
    }

    fun currentBlockedUserIds(): Set<String> = blockedUserIdsState.value

    fun isBlocked(userId: String): Boolean = userId.isNotBlank() && blockedUserIdsState.value.contains(userId)
}
