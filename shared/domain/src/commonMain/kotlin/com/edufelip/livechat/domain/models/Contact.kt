package com.edufelip.livechat.domain.models

data class Contact(
    val id: Long = 0,
    val name: String,
    val phoneNo: String,
    val description: String? = null,
    val photo: String? = null,
    val isRegistered: Boolean = false,
    val firebaseUid: String? = null,
)
