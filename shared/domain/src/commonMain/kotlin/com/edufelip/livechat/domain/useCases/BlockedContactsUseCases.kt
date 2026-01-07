package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.BlockedContact
import com.edufelip.livechat.domain.repositories.IBlockedContactsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import kotlinx.coroutines.flow.Flow

class ObserveBlockedContactsUseCase(
    private val repository: IBlockedContactsRepository,
) {
    operator fun invoke(): Flow<List<BlockedContact>> = repository.observeBlockedContacts()
}

class BlockContactUseCase(
    private val repository: IBlockedContactsRepository,
    private val messagesRepository: IMessagesRepository,
) {
    suspend operator fun invoke(contact: BlockedContact) {
        repository.blockContact(contact)
        messagesRepository.purgeConversation(contact.userId)
    }
}

class UnblockContactUseCase(
    private val repository: IBlockedContactsRepository,
) {
    suspend operator fun invoke(userId: String) {
        repository.unblockContact(userId)
    }
}
