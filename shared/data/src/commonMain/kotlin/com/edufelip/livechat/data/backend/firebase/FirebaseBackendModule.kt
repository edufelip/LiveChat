package com.edufelip.livechat.data.backend.firebase

import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestContactsRemoteData
import org.koin.dsl.module

val firebaseBackendModule =
    module {
        single<IContactsRemoteData> { FirebaseRestContactsRemoteData(get(), get()) }
        single<IMessagesRemoteData> { FirebaseMessagesRemoteData(get(), get(), get()) }
    }
