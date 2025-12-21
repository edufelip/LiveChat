package com.edufelip.livechat.data.bridge

import com.edufelip.livechat.data.remote.STORAGE_BUCKET_URL
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageBridge : MediaStorageBridge {
    override suspend fun uploadBytes(
        objectPath: String,
        bytes: ByteArray,
    ): String {
        val ref = FirebaseStorage.getInstance(STORAGE_BUCKET_URL).reference.child(objectPath)
        val upload = ref.putBytes(bytes).await()
        return upload.metadata?.reference?.downloadUrl?.await().toString()
    }

    override suspend fun downloadBytes(
        remoteUrl: String,
        maxBytes: Long,
    ): ByteArray {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(remoteUrl)
        return ref.getBytes(maxBytes).await()
    }

    override suspend fun deleteRemote(remoteUrl: String) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(remoteUrl)
        ref.delete().await()
    }
}
