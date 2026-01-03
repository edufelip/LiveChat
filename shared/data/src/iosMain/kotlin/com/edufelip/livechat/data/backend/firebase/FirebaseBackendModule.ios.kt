package com.edufelip.livechat.data.backend.firebase

import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.contracts.INotificationSettingsRemoteData
import com.edufelip.livechat.data.remote.FirebaseMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestAccountRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestContactsRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestNotificationSettingsRemoteData
import com.edufelip.livechat.domain.providers.UserSessionProvider
import org.koin.core.module.Module
import org.koin.dsl.module

actual val firebaseBackendModule: Module =
    module {
        single<IContactsRemoteData> {
            FirebaseRestContactsRemoteData(
                contactsBridge = get(),
                config = get(),
                httpClient = get(),
            )
        }
        single<IAccountRemoteData> {
            FirebaseRestAccountRemoteData(
                config = get(),
                httpClient = get(),
            )
        }
        single<INotificationSettingsRemoteData> {
            FirebaseRestNotificationSettingsRemoteData(
                config = get(),
                httpClient = get(),
            )
        }
        single<IMessagesRemoteData> {
            FirebaseMessagesRemoteData(
                messagesBridge = get(),
                storageBridge = get(),
                config = get(),
                sessionProvider = get<UserSessionProvider>(),
            )
        }
    }
