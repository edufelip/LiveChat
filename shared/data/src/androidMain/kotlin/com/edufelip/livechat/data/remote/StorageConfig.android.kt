package com.edufelip.livechat.data.remote

import com.edufelip.livechat.shared.data.BuildConfig

actual val STORAGE_BUCKET_URL: String
    get() = BuildConfig.STORAGE_BUCKET_URL.ifBlank { "gs://livechat-3ad1d.firebasestorage.app" }

