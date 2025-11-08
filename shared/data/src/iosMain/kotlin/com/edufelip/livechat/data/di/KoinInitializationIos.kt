package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.shared.data.initSharedKoin
import org.koin.core.KoinApplication

fun initKoinForIos(config: FirebaseRestConfig): KoinApplication =
    initSharedKoin(
        platformModules = listOf(iosPlatformModule(config)),
    )

fun initKoinForIos(): KoinApplication = initKoinForIos(loadFirebaseRestConfigFromPlist())
