package com.edufelip.livechat.data.remote

import platform.Foundation.NSBundle
import platform.Foundation.NSDictionary
import platform.Foundation.dictionaryWithContentsOfFile

private const val STORAGE_BUCKET_KEY = "STORAGE_BUCKET"
private const val DEFAULT_PLIST_NAME = "GoogleService-Info"

actual val STORAGE_BUCKET_URL: String by lazy {
    val path =
        NSBundle.mainBundle.pathForResource(DEFAULT_PLIST_NAME, ofType = "plist")
            ?: return@lazy ""
    val dictionary = NSDictionary.dictionaryWithContentsOfFile(path) ?: return@lazy ""
    val bucket = dictionary[STORAGE_BUCKET_KEY] as? String ?: return@lazy ""
    if (bucket.startsWith("gs://") || bucket.startsWith("https://") || bucket.startsWith("http://")) {
        bucket
    } else {
        "gs://$bucket"
    }
}
