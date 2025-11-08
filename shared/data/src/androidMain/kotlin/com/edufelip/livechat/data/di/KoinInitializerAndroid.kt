package com.edufelip.livechat.data.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.edufelip.livechat.data.auth.phone.FirebasePhoneAuthRepository
import com.edufelip.livechat.data.backend.firebase.firebaseBackendModule
import com.edufelip.livechat.data.session.FirebaseUserSessionProvider
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.data.repositories.RoomOnboardingStatusRepository
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.buildLiveChatDatabase
import com.edufelip.livechat.shared.data.database.createAndroidDatabaseBuilder
import com.edufelip.livechat.shared.data.initSharedKoin
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module

fun startKoinForAndroid(
    context: Context,
    backendModules: List<Module> = listOf(firebaseBackendModule),
    extraModules: List<Module> = emptyList(),
    httpClient: HttpClient = defaultHttpClient(),
    firebaseAppProvider: () -> FirebaseApp = { ensureFirebaseApp(context) },
): KoinApplication {
    val firebaseApp = firebaseAppProvider()
    val platformModule = androidPlatformModule(context, firebaseApp, httpClient)
    val koinApp =
        initSharedKoin(
            platformModules = listOf(platformModule) + extraModules,
            backendModules = backendModules,
        )

    koinApp.koin.get<AndroidSessionBridge>()
    return koinApp
}

fun androidPlatformModule(
    context: Context,
    firebaseApp: FirebaseApp,
    httpClient: HttpClient = defaultHttpClient(),
): Module =
    module {
        single { context.applicationContext }
        single { firebaseApp }
        single { firebaseRestConfig(firebaseApp) }
        single { httpClient }
        single { FirebaseAuth.getInstance(firebaseApp) }
        single { InMemoryUserSessionProvider() }
        single { FirebaseUserSessionProvider(get()) }
        single<UserSessionProvider> { get<FirebaseUserSessionProvider>() }
        single { AndroidSessionBridge(get(), get(), Dispatchers.Default) }
        single<IPhoneAuthRepository> { FirebasePhoneAuthRepository(get()) }
        single<LiveChatDatabase> { buildLiveChatDatabase(createAndroidDatabaseBuilder(get())) }
        single<IOnboardingStatusRepository> { RoomOnboardingStatusRepository(get()) }
    }

private fun firebaseRestConfig(app: FirebaseApp): com.edufelip.livechat.data.remote.FirebaseRestConfig =
    com.edufelip.livechat.data.remote.FirebaseRestConfig(
        projectId =
            app.options.projectId
                ?: error("Firebase projectId is missing. Check google-services.json."),
        apiKey = app.options.apiKey ?: "",
    )

private fun ensureFirebaseApp(context: Context): FirebaseApp {
    return FirebaseApp.getApps(context).firstOrNull()
        ?: FirebaseApp.initializeApp(context)
        ?: error("FirebaseApp could not be initialized. Ensure google-services.json is present.")
}

fun defaultHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        install(Logging) {
            level = LogLevel.NONE
        }
        install(WebSockets) { }
    }
