package com.edufelip.livechat

import android.app.Application
import com.edufelip.livechat.data.di.startKoinForAndroid

class LiveChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoinForAndroid(applicationContext)
    }
}
