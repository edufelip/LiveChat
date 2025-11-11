package com.edufelip.livechat.data.backend.firebase

import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestContactsRemoteData
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.functions.functions
import org.koin.dsl.module

val firebaseBackendModule =
    module {
        single { Firebase.firestore }
        single { Firebase.functions }
        single<IContactsRemoteData> {
            FirebaseRestContactsRemoteData(
                firestore = get(),
                functions = get(),
                config = get(),
                httpClient = get(),
            )
        }
        single<IMessagesRemoteData> { FirebaseMessagesRemoteData(get(), get()) }
    }
