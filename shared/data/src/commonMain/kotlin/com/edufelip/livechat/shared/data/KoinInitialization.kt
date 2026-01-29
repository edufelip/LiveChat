package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.backend.firebase.firebaseBackendModule
import com.edufelip.livechat.domain.di.sharedDomainModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initSharedKoin(
    platformModules: List<Module>,
    backendModules: List<Module> = listOf(firebaseBackendModule),
    extraModules: List<Module> = emptyList(),
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication {
    val allModules = platformModules + backendModules + sharedDataModule + sharedDomainModule + extraModules
    return startKoin {
        appDeclaration()
        modules(allModules)
    }
}
