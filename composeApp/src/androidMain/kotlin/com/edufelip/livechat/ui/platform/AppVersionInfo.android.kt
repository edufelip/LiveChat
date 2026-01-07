package com.edufelip.livechat.ui.platform

import com.edufelip.livechat.BuildConfig

actual fun appVersionInfo(): AppVersionInfo =
    AppVersionInfo(
        versionName = BuildConfig.VERSION_NAME,
        buildNumber = BuildConfig.VERSION_CODE.toString(),
    )
