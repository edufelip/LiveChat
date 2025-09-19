package com.project.livechat.data.di

import com.project.livechat.data.remote.FirebaseRestConfig
import com.project.livechat.shared.data.initSharedKoin
import org.koin.core.KoinApplication

fun initKoinForIos(
    config: FirebaseRestConfig
): KoinApplication = initSharedKoin(
    platformModules = listOf(iosPlatformModule(config))
)

fun initKoinForIos(): KoinApplication = initKoinForIos(loadFirebaseRestConfigFromPlist())
