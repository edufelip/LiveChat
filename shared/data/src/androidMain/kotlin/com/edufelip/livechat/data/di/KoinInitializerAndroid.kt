package com.edufelip.livechat.data.di

import android.content.Context
import android.telephony.TelephonyManager
import com.edufelip.livechat.data.auth.phone.FirebasePhoneAuthRepository
import com.edufelip.livechat.data.backend.firebase.firebaseBackendModule
import com.edufelip.livechat.data.files.MediaFileStore
import com.edufelip.livechat.data.remote.loadFirebaseEmulatorConfig
import com.edufelip.livechat.data.repositories.RoomOnboardingStatusRepository
import com.edufelip.livechat.data.session.FirebaseUserSessionProvider
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.shared.data.database.LiveChatDatabase
import com.edufelip.livechat.shared.data.database.buildLiveChatDatabase
import com.edufelip.livechat.shared.data.database.createAndroidDatabaseBuilder
import com.edufelip.livechat.shared.data.initSharedKoin
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
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
import java.io.File
import java.util.Locale

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
        val emulatorConfig = loadFirebaseEmulatorConfig()
        MediaFileStore.configure(File(context.filesDir, "media").absolutePath)
        single { context.applicationContext }
        single { firebaseApp }
        single { firebaseRestConfig(context.applicationContext, firebaseApp) }
        single { httpClient }
        single {
            FirebaseAuth.getInstance(firebaseApp).apply {
                emulatorConfig?.let { useEmulator(it.host, it.authPort) }
            }
        }
        single { InMemoryUserSessionProvider() }
        single { FirebaseUserSessionProvider(get()) }
        single<UserSessionProvider> { get<FirebaseUserSessionProvider>() }
        single { AndroidSessionBridge(get(), get(), Dispatchers.Default) }
        single<IPhoneAuthRepository> { FirebasePhoneAuthRepository(get()) }
        single<LiveChatDatabase> { buildLiveChatDatabase(createAndroidDatabaseBuilder(get())) }
        single<IOnboardingStatusRepository> { RoomOnboardingStatusRepository(get()) }
    }

private fun firebaseRestConfig(
    context: Context,
    app: FirebaseApp,
): com.edufelip.livechat.data.remote.FirebaseRestConfig =
    run {
        val emulatorConfig = loadFirebaseEmulatorConfig()
        com.edufelip.livechat.data.remote.FirebaseRestConfig(
            projectId =
                app.options.projectId
                    ?: error("Firebase projectId is missing. Check google-services.json."),
            apiKey = app.options.apiKey ?: "",
            emulatorHost = emulatorConfig?.host,
            emulatorPort = emulatorConfig?.firestorePort,
            defaultRegionIso = context.defaultRegionIso(),
        )
    }

private fun ensureFirebaseApp(context: Context): FirebaseApp {
    return FirebaseApp.getApps(context).firstOrNull()
        ?: FirebaseApp.initializeApp(context)
        ?: error("FirebaseApp could not be initialized. Ensure google-services.json is present.")
}

private fun Context.defaultRegionIso(): String? {
    val telephony = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val localeCountry =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            val locales = resources.configuration.locales
            if (locales.isEmpty) null else locales.get(0)?.country
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale?.country
        }
    val candidates =
        listOfNotNull(
            telephony?.simCountryIso,
            telephony?.networkCountryIso,
            localeCountry,
            Locale.getDefault().country,
        )
    return candidates
        .firstOrNull { !it.isNullOrBlank() }
        ?.uppercase(Locale.ROOT)
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
