package com.project.livechat.shared.data

import com.project.livechat.data.backend.firebase.firebaseBackendModule
import com.project.livechat.domain.di.sharedDomainModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initSharedKoin(
    platformModules: List<Module>,
    backendModules: List<Module> = listOf(firebaseBackendModule),
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    val allModules = platformModules + backendModules + sharedDataModule + sharedDomainModule
    return startKoin {
        appDeclaration()
        modules(allModules)
    }
}
