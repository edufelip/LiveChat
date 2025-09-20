package com.project.livechat.data.di

import com.project.livechat.data.backend.firebase.firebaseBackendModule
import com.project.livechat.data.remote.FirebaseRestConfig
import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.shared.data.initSharedKoin
import io.ktor.client.HttpClient
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module

object IosKoinBridge : KoinComponent {
    fun sessionProvider(): InMemoryUserSessionProvider = get()
    fun conversationPresenter(): ConversationPresenter = get()
    fun conversationListPresenter(): ConversationListPresenter = get()
    fun contactsPresenter(): ContactsPresenter = get()
}

fun startKoinForiOS(
    config: FirebaseRestConfig,
    httpClient: HttpClient = defaultHttpClient(),
    backendModules: List<Module>? = null
): KoinApplication {
    return initSharedKoin(
        platformModules = listOf(iosPlatformModule(config, httpClient)),
        backendModules = backendModules ?: listOf(firebaseBackendModule)
    )
}
