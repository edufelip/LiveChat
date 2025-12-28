package com.edufelip.livechat.data.remote

import platform.Foundation.NSBundle

actual val STORAGE_BUCKET_URL: String
    get() {
        val fromInfoPlist =
            NSBundle.mainBundle.infoDictionary?.get("STORAGE_BUCKET_URL") as? String
        return fromInfoPlist?.takeIf { it.isNotBlank() } ?: "gs://livechat-3ad1d.firebasestorage.app"
    }
