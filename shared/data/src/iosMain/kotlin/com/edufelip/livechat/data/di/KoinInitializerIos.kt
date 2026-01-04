package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.backend.firebase.firebaseBackendModule
import com.edufelip.livechat.data.bridge.AuthBridge
import com.edufelip.livechat.data.bridge.IosBridgeBundle
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.AccountPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.NotificationSettingsPresenter
import com.edufelip.livechat.domain.presentation.AppearanceSettingsPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter
import com.edufelip.livechat.shared.data.initSharedKoin
import io.ktor.client.HttpClient
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module

object IosKoinBridge : KoinComponent {
    fun sessionProvider(): InMemoryUserSessionProvider = get()

    fun conversationPresenter(): ConversationPresenter = get()

    fun conversationListPresenter(): ConversationListPresenter = get()

    fun contactsPresenter(): ContactsPresenter = get()

    fun accountPresenter(): AccountPresenter = get()

    fun appPresenter(): AppPresenter = get()

    fun phoneAuthPresenter(): PhoneAuthPresenter = get()

    fun notificationSettingsPresenter(): NotificationSettingsPresenter = get()

    fun appearanceSettingsPresenter(): AppearanceSettingsPresenter = get()

    fun authBridge(): AuthBridge = get()
}

fun startKoinForiOS(
    config: FirebaseRestConfig,
    bridgeBundle: IosBridgeBundle,
    httpClient: HttpClient = defaultHttpClient(),
    backendModules: List<Module>? = null,
): KoinApplication {
    return initSharedKoin(
        platformModules = listOf(iosPlatformModule(config, bridgeBundle, httpClient)),
        backendModules = backendModules ?: listOf(firebaseBackendModule),
    )
}
