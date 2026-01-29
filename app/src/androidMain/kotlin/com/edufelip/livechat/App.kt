package com.edufelip.livechat

import android.app.Application
import com.edufelip.livechat.data.di.startKoinForAndroid
import com.edufelip.livechat.di.presentationModule
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        instance = this
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        startKoinForAndroid(applicationContext, extraModules = listOf(presentationModule))
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
