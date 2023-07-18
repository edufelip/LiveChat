package com.project.livechat.data.di

import com.google.firebase.firestore.FirebaseFirestore
import com.project.livechat.data.contracts.IContactsLocalData
import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.data.db.ContactDao
import com.project.livechat.data.providers.ContactsLocalData
import com.project.livechat.data.providers.ContactsRemoteData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun providesContactsRemoteData(
        fireStore: FirebaseFirestore
    ): IContactsRemoteData = ContactsRemoteData(fireStore)

    @Provides
    fun providesContactsLocalData(
        dao: ContactDao
    ): IContactsLocalData = ContactsLocalData(dao)
}
