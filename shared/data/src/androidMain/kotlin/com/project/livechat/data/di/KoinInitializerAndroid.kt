package com.project.livechat.data.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.project.livechat.data.backend.firebase.firebaseBackendModule
import com.project.livechat.data.session.FirebaseUserSessionProvider
import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.shared.data.initSharedKoin
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
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
        single<Settings> {
            val prefs = context.getSharedPreferences(PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
            SharedPreferencesSettings(prefs)
        }
        single<SqlDriver> {
            AndroidSqliteDriver(
                schema = LiveChatDatabase.Schema,
                context = context,
                name = DEFAULT_DATABASE_NAME,
            )
        }
    }

private fun firebaseRestConfig(app: FirebaseApp): com.project.livechat.data.remote.FirebaseRestConfig =
    com.project.livechat.data.remote.FirebaseRestConfig(
        projectId = app.options.projectId
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

private const val DEFAULT_DATABASE_NAME = "livechat.db"
private const val PREFERENCES_FILE_NAME = "livechat_prefs"
