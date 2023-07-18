package com.project.livechat.data.di

import com.project.livechat.data.contracts.IContactsLocalData
import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.data.providers.ContactsLocalData
import com.project.livechat.data.repository.ContactsRepository
import com.project.livechat.domain.repositories.IContactsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun providesContactsRepository(
        contactsRemoteData: IContactsRemoteData,
        contactsLocalData: IContactsLocalData
    ): IContactsRepository = ContactsRepository(contactsRemoteData, contactsLocalData)
}
