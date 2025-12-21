package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.bridge.IosBridgeBundle
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.shared.data.initSharedKoin
import org.koin.core.KoinApplication

fun initKoinForIos(
    config: FirebaseRestConfig,
    bridgeBundle: IosBridgeBundle,
): KoinApplication =
    initSharedKoin(
        platformModules = listOf(iosPlatformModule(config, bridgeBundle)),
    )

fun initKoinForIos(bridgeBundle: IosBridgeBundle): KoinApplication = initKoinForIos(loadFirebaseRestConfigFromPlist(), bridgeBundle)
