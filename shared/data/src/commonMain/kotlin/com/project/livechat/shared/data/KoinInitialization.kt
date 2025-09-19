package com.project.livechat.shared.data

import com.project.livechat.domain.di.sharedDomainModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initSharedKoin(
    platformModules: List<Module>,
    appDeclaration: KoinAppDeclaration = {}
): KoinApplication {
    val allModules = platformModules + sharedDataModule + sharedDomainModule
    return startKoin {
        appDeclaration()
        modules(allModules)
    }
}
