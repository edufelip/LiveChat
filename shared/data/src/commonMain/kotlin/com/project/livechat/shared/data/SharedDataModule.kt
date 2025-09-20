package com.project.livechat.shared.data

import com.project.livechat.data.contracts.IContactsLocalData
import com.project.livechat.data.contracts.IMessagesLocalData
import com.project.livechat.data.local.ContactsLocalDataSource
import com.project.livechat.data.local.MessagesLocalDataSource
import com.project.livechat.data.repositories.ContactsRepository
import com.project.livechat.data.repositories.MessagesRepository
import com.project.livechat.domain.repositories.IContactsRepository
import com.project.livechat.domain.repositories.IMessagesRepository
import com.project.livechat.shared.data.database.LiveChatDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedDataModule: Module = module {
    single { LiveChatDatabase(get()) }
    single<IContactsLocalData> { ContactsLocalDataSource(get()) }
    single<IContactsRepository> { ContactsRepository(get(), get()) }
    single<IMessagesLocalData> { MessagesLocalDataSource(get()) }
    single<IMessagesRepository> { MessagesRepository(get(), get(), get()) }
}
