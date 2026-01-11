package com.edufelip.livechat.domain.utils

import android.os.Looper

internal actual fun isMainThread(): Boolean {
    val mainLooper = Looper.getMainLooper() ?: return true
    return Looper.myLooper() == mainLooper
}
