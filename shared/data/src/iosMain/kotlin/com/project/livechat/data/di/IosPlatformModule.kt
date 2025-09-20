package com.project.livechat.data.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.project.livechat.data.remote.FirebaseRestConfig
import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.shared.data.database.LiveChatDatabase
import com.project.livechat.domain.providers.UserSessionProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosPlatformModule(
    config: FirebaseRestConfig,
    httpClient: HttpClient = defaultHttpClient()
): Module = module {
    single { config }
    single { httpClient }
    single<SqlDriver> { NativeSqliteDriver(LiveChatDatabase.Schema, "livechat.db") }
    single { InMemoryUserSessionProvider() }
    single<UserSessionProvider> { get<InMemoryUserSessionProvider>() }
}

private fun defaultHttpClient(): HttpClient = HttpClient(Darwin) {
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
