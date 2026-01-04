package com.edufelip.livechat.domain.models

data class AccountUiState(
    val isLoading: Boolean = true,
    val profile: AccountProfile? = null,
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val requiresReauth: Boolean = false,
    val emailUpdateState: EmailUpdateState = EmailUpdateState.Idle,
)
