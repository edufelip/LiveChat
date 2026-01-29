package com.edufelip.livechat.data.di

import com.edufelip.livechat.data.bridge.IosBridgeBundle
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.shared.data.initSharedKoin
import org.koin.core.KoinApplication
import org.koin.core.module.Module

fun initKoinForIos(
    config: FirebaseRestConfig,
    bridgeBundle: IosBridgeBundle,
    extraModules: List<Module> = emptyList(),
): KoinApplication =
    initSharedKoin(
        platformModules = listOf(iosPlatformModule(config, bridgeBundle)),
        extraModules = extraModules,
    )

fun initKoinForIos(
    bridgeBundle: IosBridgeBundle,
    extraModules: List<Module> = emptyList(),
): KoinApplication = initKoinForIos(loadFirebaseRestConfigFromPlist(), bridgeBundle, extraModules)
