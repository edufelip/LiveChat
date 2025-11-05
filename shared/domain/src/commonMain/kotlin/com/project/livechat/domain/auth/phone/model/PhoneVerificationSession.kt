package com.project.livechat.domain.auth.phone.model

import com.project.livechat.domain.utils.currentEpochMillis

data class PhoneVerificationSession(
    val verificationId: String,
    val phoneNumber: PhoneNumber,
    val createdAtMillis: Long = currentEpochMillis(),
    val autoVerified: Boolean = false,
) {
    fun markAutoVerified(): PhoneVerificationSession = copy(autoVerified = true)
}
