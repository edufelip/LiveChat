package com.project.livechat

import android.app.Application
import com.project.livechat.data.di.startKoinForAndroid

class LiveChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoinForAndroid(applicationContext)
    }
}
