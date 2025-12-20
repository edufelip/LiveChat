package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.repositories.RoomOnboardingStatusRepository
import com.edufelip.livechat.data.auth.phone.FirebasePhoneAuthRepository
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.buildLiveChatDatabase
import com.edufelip.livechat.shared.data.database.createIosDatabaseBuilder
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
    httpClient: HttpClient = defaultHttpClient(),
): Module =
    module {
        single { config }
        single { httpClient }
        single<LiveChatDatabase> { buildLiveChatDatabase(createIosDatabaseBuilder()) }
        single { InMemoryUserSessionProvider() }
        single<UserSessionProvider> { get<InMemoryUserSessionProvider>() }
        single<IPhoneAuthRepository> { FirebasePhoneAuthRepository() }
        single<IOnboardingStatusRepository> { RoomOnboardingStatusRepository(get()) }
    }

fun defaultHttpClient(): HttpClient =
    HttpClient(Darwin) {
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
