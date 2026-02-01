package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

class ValidateContactsUseCase(
    private val repository: IContactsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val phoneNumberFormatter: PhoneNumberFormatter,
) {
    operator fun invoke(needsValidation: List<Contact>): Flow<Contact> {
        if (needsValidation.isEmpty()) return emptyFlow()

        val validatedNumbers = mutableSetOf<String>()
        return repository
            .checkRegisteredContacts(needsValidation)
            .onEach { validated ->
                val registered = validated.copy(isRegistered = true)
                validatedNumbers.add(phoneNumberFormatter.normalize(registered.phoneNo))
                repository.updateContacts(listOf(registered))
            }.onCompletion { cause ->
                if (cause != null) return@onCompletion
                val toMarkUnregistered =
                    needsValidation
                        .filter { phoneNumberFormatter.normalize(it.phoneNo) !in validatedNumbers }
                        .map { it.copy(isRegistered = false) }
                if (toMarkUnregistered.isNotEmpty()) {
                    repository.updateContacts(toMarkUnregistered)
                }
            }.flowOn(dispatcher)
    }
}
