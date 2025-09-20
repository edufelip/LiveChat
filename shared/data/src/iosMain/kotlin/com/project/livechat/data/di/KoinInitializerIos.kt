package com.project.livechat.data.di

import com.project.livechat.data.remote.FirebaseRestConfig
import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.shared.data.initSharedKoin
import io.ktor.client.HttpClient
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object IosKoinBridge : KoinComponent {
    fun sessionProvider(): InMemoryUserSessionProvider = get()
    fun conversationPresenter(): ConversationPresenter = get()
}

fun startKoinForiOS(config: FirebaseRestConfig): KoinApplication {
    return initSharedKoin(
        platformModules = listOf(iosPlatformModule(config))
    )
}

fun startKoinForiOS(config: FirebaseRestConfig, httpClient: HttpClient): KoinApplication {
    return initSharedKoin(
        platformModules = listOf(iosPlatformModule(config, httpClient))
    )
}
