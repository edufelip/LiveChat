package com.edufelip.livechat

import android.app.Application
import com.edufelip.livechat.data.di.startKoinForAndroid
import com.edufelip.livechat.di.presentationModule

class LiveChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoinForAndroid(applicationContext, extraModules = listOf(presentationModule))
    }
}
