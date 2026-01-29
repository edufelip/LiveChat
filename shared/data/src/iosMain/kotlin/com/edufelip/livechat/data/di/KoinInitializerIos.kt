package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.backend.firebase.firebaseBackendModule
import com.edufelip.livechat.data.bridge.AuthBridge
import com.edufelip.livechat.data.bridge.IosBridgeBundle
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.shared.data.initSharedKoin
import io.ktor.client.HttpClient
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module

object IosKoinBridge : KoinComponent {
    fun sessionProvider(): InMemoryUserSessionProvider = get()

    fun authBridge(): AuthBridge = get()
}

fun startKoinForiOS(
    config: FirebaseRestConfig,
    bridgeBundle: IosBridgeBundle,
    httpClient: HttpClient = defaultHttpClient(),
    backendModules: List<Module>? = null,
    extraModules: List<Module> = emptyList(),
): KoinApplication {
    return initSharedKoin(
        platformModules = listOf(iosPlatformModule(config, bridgeBundle, httpClient)),
        backendModules = backendModules ?: listOf(firebaseBackendModule),
        extraModules = extraModules,
    )
}
