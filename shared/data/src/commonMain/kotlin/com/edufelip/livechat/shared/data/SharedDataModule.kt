package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.data.contracts.IContactsLocalData
import com.edufelip.livechat.data.contracts.IMessagesLocalData
import com.edufelip.livechat.data.local.ContactsLocalDataSource
import com.edufelip.livechat.data.local.MessagesLocalDataSource
import com.edufelip.livechat.data.repositories.AccountRepository
import com.edufelip.livechat.data.repositories.ContactsRepository
import com.edufelip.livechat.data.repositories.ConversationParticipantsRepository
import com.edufelip.livechat.data.repositories.MessagesRepository
import com.edufelip.livechat.domain.repositories.IAccountRepository
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IConversationParticipantsRepository
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedDataModule: Module =
    module {
        single<IAccountRepository> { AccountRepository(get<IAccountRemoteData>(), get()) }
        single<IContactsLocalData> { ContactsLocalDataSource(get()) }
        single<IContactsRepository> { ContactsRepository(get(), get()) }
        single<IMessagesLocalData> { MessagesLocalDataSource(get()) }
        single<IConversationParticipantsRepository> { ConversationParticipantsRepository(get(), get()) }
        single<IMessagesRepository> { MessagesRepository(get(), get(), get(), get()) }
    }
