package com.edufelip.livechat.domain.models

data class BlockedContactsUiState(
    val isLoading: Boolean = true,
    val contacts: List<BlockedContact> = emptyList(),
    val errorMessage: String? = null,
    val isUpdating: Boolean = false,
)
