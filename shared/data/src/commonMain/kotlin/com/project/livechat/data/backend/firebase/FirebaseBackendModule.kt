package com.project.livechat.data.backend.firebase

import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.data.contracts.IMessagesRemoteData
import com.project.livechat.data.remote.FirebaseMessagesRemoteData
import com.project.livechat.data.remote.FirebaseRestContactsRemoteData
import org.koin.dsl.module

val firebaseBackendModule = module {
    single<IContactsRemoteData> { FirebaseRestContactsRemoteData(get(), get()) }
    single<IMessagesRemoteData> { FirebaseMessagesRemoteData(get(), get(), get()) }
}
