package com.edufelip.livechat.data.backend.firebase

import com.edufelip.livechat.data.bridge.ContactsRemoteBridge
import com.edufelip.livechat.data.bridge.FirebaseContactsBridge
import com.edufelip.livechat.data.bridge.FirebaseMessagesBridge
import com.edufelip.livechat.data.bridge.FirebaseStorageBridge
import com.edufelip.livechat.data.bridge.MediaStorageBridge
import com.edufelip.livechat.data.bridge.MessagesRemoteBridge
import com.edufelip.livechat.data.contracts.IAccountRemoteData
import com.edufelip.livechat.data.contracts.IAppearanceSettingsRemoteData
import com.edufelip.livechat.data.contracts.IBlockedContactsRemoteData
import com.edufelip.livechat.data.contracts.IContactsRemoteData
import com.edufelip.livechat.data.contracts.IMessagesRemoteData
import com.edufelip.livechat.data.contracts.INotificationSettingsRemoteData
import com.edufelip.livechat.data.contracts.IPrivacySettingsRemoteData
import com.edufelip.livechat.data.remote.FirebaseMessagesRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestAccountRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestAppearanceSettingsRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestBlockedContactsRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestContactsRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestNotificationSettingsRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestPrivacySettingsRemoteData
import com.edufelip.livechat.data.remote.STORAGE_BUCKET_URL
import com.edufelip.livechat.data.remote.loadFirebaseEmulatorConfig
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val firebaseBackendModule: Module =
    module {
        val emulatorConfig = loadFirebaseEmulatorConfig()
        single {
            FirebaseFirestore.getInstance().apply {
                emulatorConfig?.let { useEmulator(it.host, it.firestorePort) }
            }
        }
        single {
            FirebaseFunctions.getInstance().apply {
                emulatorConfig?.let { useEmulator(it.host, it.functionsPort) }
            }
        }
        single {
            FirebaseStorage.getInstance(STORAGE_BUCKET_URL).apply {
                emulatorConfig?.let { useEmulator(it.host, it.storagePort) }
            }
        }
        single<ContactsRemoteBridge> { FirebaseContactsBridge(get(), get(), get()) }
        single<MessagesRemoteBridge> { FirebaseMessagesBridge(get(), get()) }
        single<MediaStorageBridge> { FirebaseStorageBridge(get()) }
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
        single<IAppearanceSettingsRemoteData> {
            FirebaseRestAppearanceSettingsRemoteData(
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
        single<IPrivacySettingsRemoteData> {
            FirebaseRestPrivacySettingsRemoteData(
                config = get(),
                httpClient = get(),
            )
        }
        single<IBlockedContactsRemoteData> {
            FirebaseRestBlockedContactsRemoteData(
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
