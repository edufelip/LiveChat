package com.edufelip.livechat.data.bridge

import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageBridge(
    private val storage: FirebaseStorage,
) : MediaStorageBridge {
    override suspend fun uploadBytes(
        objectPath: String,
        bytes: ByteArray,
    ): String {
        val ref = storage.reference.child(objectPath)
        val upload = ref.putBytes(bytes).await()
        return upload.metadata
            ?.reference
            ?.downloadUrl
            ?.await()
            .toString()
    }

    override suspend fun downloadBytes(
        remoteUrl: String,
        maxBytes: Long,
    ): ByteArray {
        val ref = storage.getReferenceFromUrl(remoteUrl)
        return ref.getBytes(maxBytes).await()
    }

    override suspend fun deleteRemote(remoteUrl: String) {
        val ref = storage.getReferenceFromUrl(remoteUrl)
        ref.delete().await()
    }
}
