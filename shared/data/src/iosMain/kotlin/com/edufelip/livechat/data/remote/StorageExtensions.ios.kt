package com.edufelip.livechat.data.remote

import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.reference
import dev.gitlive.firebase.storage.referenceFromUrl

/**
 * iOS implementation uses gitlive Firebase storage APIs.
 */
actual suspend fun FirebaseStorage.uploadBytes(objectPath: String, data: ByteArray): String {
    val ref = referenceFromUrl(STORAGE_BUCKET_URL).child(objectPath)
    ref.putBytes(data)
    return ref.toString()
}

actual suspend fun FirebaseStorage.downloadBytes(remoteUrl: String, maxSize: Long): ByteArray {
    val ref = referenceFromUrl(remoteUrl)
    return ref.getBytes(maxSize)
}

actual suspend fun FirebaseStorage.deleteRemote(remoteUrl: String) {
    val ref = referenceFromUrl(remoteUrl)
    ref.delete()
}
