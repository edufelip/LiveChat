package com.edufelip.livechat.data.bridge

data class IosBridgeBundle(
    val messagesBridge: MessagesRemoteBridge,
    val contactsBridge: ContactsRemoteBridge,
    val storageBridge: MediaStorageBridge,
    val phoneAuthBridge: PhoneAuthBridge,
)
