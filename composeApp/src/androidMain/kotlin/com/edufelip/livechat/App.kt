package com.edufelip.livechat

import android.app.Application
import com.edufelip.livechat.data.di.startKoinForAndroid
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        startKoinForAndroid(applicationContext)
    }
}
