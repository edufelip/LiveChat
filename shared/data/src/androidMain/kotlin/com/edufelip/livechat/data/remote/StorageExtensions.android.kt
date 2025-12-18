package com.edufelip.livechat.data.remote

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.edufelip.livechat.data.remote.STORAGE_BUCKET_URL

/**
 * Android implementation relies on platform storage KTX.
 */
actual suspend fun FirebaseStorage.uploadBytes(objectPath: String, data: ByteArray): String {
    val ref = Firebase.storage(STORAGE_BUCKET_URL).reference.child(objectPath)
    ref.putBytes(data).await()
    return ref.toString()
}

actual suspend fun FirebaseStorage.downloadBytes(remoteUrl: String, maxSize: Long): ByteArray {
    val ref = Firebase.storage(STORAGE_BUCKET_URL).getReferenceFromUrl(remoteUrl)
    return ref.getBytes(maxSize).await()
}

actual suspend fun FirebaseStorage.deleteRemote(remoteUrl: String) {
    val ref = Firebase.storage(STORAGE_BUCKET_URL).getReferenceFromUrl(remoteUrl)
    ref.delete().await()
}
