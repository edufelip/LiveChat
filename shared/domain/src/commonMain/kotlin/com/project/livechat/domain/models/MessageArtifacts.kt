package com.project.livechat.domain.models

enum class MessageContentType {
    Text,
    Encrypted,
    AttachmentOnly,
}

data class CipherInfo(
    val algorithm: String,
    val keyId: String,
    val nonce: String,
    val associatedData: String? = null,
)

data class AttachmentRef(
    val objectKey: String,
    val mimeType: String,
    val sizeBytes: Long,
    val thumbnailKey: String? = null,
    val cipherInfo: CipherInfo? = null,
)
