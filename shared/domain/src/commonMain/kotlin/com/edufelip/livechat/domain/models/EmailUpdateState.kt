package com.edufelip.livechat.domain.models

sealed interface EmailUpdateState {
    object Idle : EmailUpdateState

    data class Sending(val email: String) : EmailUpdateState

    data class Sent(val email: String) : EmailUpdateState

    data class Verifying(val email: String) : EmailUpdateState

    data class Verified(val email: String) : EmailUpdateState
}
