package com.edufelip.livechat.data.bridge

import com.edufelip.livechat.data.remote.PhoneExistsBatchResult
import com.edufelip.livechat.data.remote.PhoneExistsSingleResult

object IosBridgeDefaults {
    fun empty(): IosBridgeBundle =
        IosBridgeBundle(
            messagesBridge = NoopMessagesBridge,
            contactsBridge = NoopContactsBridge,
            storageBridge = NoopStorageBridge,
            phoneAuthBridge = NoopPhoneAuthBridge,
            authBridge = NoopAuthBridge,
            remoteConfigBridge = NoopRemoteConfigBridge,
        )
}

private object NoopMessagesBridge : MessagesRemoteBridge {
    override fun startListening(
        recipientId: String,
        listener: MessagesRemoteListener,
    ): String = "noop"

    override fun stopListening(token: String) {
    }

    override suspend fun fetchMessages(recipientId: String): List<TransportMessagePayload> = emptyList()

    override suspend fun sendMessage(
        recipientId: String,
        documentId: String,
        payload: TransportMessagePayload,
    ): String = documentId

    override suspend fun deleteMessage(
        recipientId: String,
        documentId: String,
    ) {
    }

    override suspend fun ensureConversation(conversationId: String) {
    }
}

private object NoopContactsBridge : ContactsRemoteBridge {
    override suspend fun phoneExists(phoneE164: String): PhoneExistsSingleResult = PhoneExistsSingleResult(exists = false, uid = null)

    override suspend fun phoneExistsMany(phones: List<String>): PhoneExistsBatchResult =
        PhoneExistsBatchResult(registeredPhones = emptyList(), matches = emptyList())

    override suspend fun isUserRegistered(phoneE164: String): Boolean = false
}

private object NoopStorageBridge : MediaStorageBridge {
    override suspend fun uploadBytes(
        objectPath: String,
        bytes: ByteArray,
    ): String = ""

    override suspend fun downloadBytes(
        remoteUrl: String,
        maxBytes: Long,
    ): ByteArray = ByteArray(0)

    override suspend fun deleteRemote(remoteUrl: String) {
    }
}

private object NoopPhoneAuthBridge : PhoneAuthBridge {
    override suspend fun sendCode(phoneE164: String): PhoneAuthBridgeResult =
        PhoneAuthBridgeResult(
            verificationId = null,
            error = PhoneAuthBridgeError(message = "Phone auth not configured for iOS"),
        )

    override suspend fun verifyCode(
        verificationId: String,
        code: String,
    ): PhoneAuthBridgeError? = PhoneAuthBridgeError(message = "Phone auth not configured for iOS")
}

private object NoopAuthBridge : AuthBridge {
    override fun signOut() {
    }
}

private object NoopRemoteConfigBridge : RemoteConfigBridge {
    override suspend fun fetchAndActivate(): RemoteConfigBridgeResult =
        RemoteConfigBridgeResult(
            activated = false,
            error = RemoteConfigBridgeError(message = "Remote config not configured for iOS"),
        )

    override fun getString(key: String): String = ""
}
