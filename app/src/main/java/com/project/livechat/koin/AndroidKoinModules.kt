package com.project.livechat.koin

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.data.remote.FirebaseRestConfig
import com.project.livechat.data.remote.FirebaseRestContactsRemoteData
import com.project.livechat.data.session.FirebaseUserSessionProvider
import com.project.livechat.domain.providers.IPhoneAuthProvider
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.ui.components.connection.ConnectivityObserver
import com.project.livechat.ui.components.connection.NetworkConnectivityObserver
import com.project.livechat.ui.utils.Constants
import com.project.livechat.ui.utils.auth.FirebasePhoneAuthProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single { FirebaseAuth.getInstance() }
    single { provideFirebaseRestConfig(androidContext()) }
    single { provideHttpClient() }
    single<ConnectivityObserver> { NetworkConnectivityObserver(androidContext()) }
    single<IContactsRemoteData> { FirebaseRestContactsRemoteData(get(), get()) }
    single<UserSessionProvider> { FirebaseUserSessionProvider(get()) }
    single<IPhoneAuthProvider> { FirebasePhoneAuthProvider(get()) }
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = LiveChatDatabase.Schema,
                    context = androidContext(),
            name = Constants.DATABASE_NAME
        )
    }
}

private fun provideFirebaseRestConfig(context: Context): FirebaseRestConfig {
    val app = FirebaseApp.getApps(context).firstOrNull() ?: FirebaseApp.initializeApp(context)
        ?: error("FirebaseApp could not be initialized. Ensure google-services.json is present.")
    val options = app.options
    val projectId = options.projectId ?: error("Firebase projectId is missing. Check google-services.json.")
    val apiKey = options.apiKey ?: ""
    return FirebaseRestConfig(
        projectId = projectId,
        apiKey = apiKey
    )
}

private fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }
    install(Logging) {
        level = LogLevel.NONE
    }
    install(WebSockets) { }
}
