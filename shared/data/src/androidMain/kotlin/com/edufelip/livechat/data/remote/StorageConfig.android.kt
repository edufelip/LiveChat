package com.edufelip.livechat.data.remote

actual val STORAGE_BUCKET_URL: String
    get() {
        val configured =
            runCatching {
                val clazz = Class.forName("com.edufelip.livechat.BuildConfig")
                val field = clazz.getField("STORAGE_BUCKET_URL")
                field.get(null) as? String
            }.getOrNull()
        return configured?.takeIf { it.isNotBlank() } ?: "gs://livechat-3ad1d.firebasestorage.app"
    }
