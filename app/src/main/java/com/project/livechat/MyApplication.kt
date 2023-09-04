package com.project.livechat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

abstract class BaseApplication: Application()

@HiltAndroidApp
class MyApplication : BaseApplication()