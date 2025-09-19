package com.project.livechat

import android.app.Application
import com.project.livechat.koin.androidPlatformModule
import com.project.livechat.koin.androidViewModelModule
import com.project.livechat.shared.data.initSharedKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initSharedKoin(
            platformModules = listOf(androidPlatformModule, androidViewModelModule)
        ) {
            androidLogger()
            androidContext(this@MyApplication)
        }
    }
}
