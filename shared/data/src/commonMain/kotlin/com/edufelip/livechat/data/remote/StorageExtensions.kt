package com.edufelip.livechat.data.remote

import dev.gitlive.firebase.storage.FirebaseStorage

/**
 * Minimal storage helpers to keep upload/download calls platform-safe.
 */
expect suspend fun FirebaseStorage.uploadBytes(objectPath: String, data: ByteArray): String
expect suspend fun FirebaseStorage.downloadBytes(remoteUrl: String, maxSize: Long): ByteArray
expect suspend fun FirebaseStorage.deleteRemote(remoteUrl: String)
