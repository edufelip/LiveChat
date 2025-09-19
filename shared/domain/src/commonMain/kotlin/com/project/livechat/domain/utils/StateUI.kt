package com.project.livechat.domain.utils

sealed class StateUI<out T> {
    data class Success<T>(val data: T) : StateUI<T>()
    data class Error(
        val type: Enum<*>? = null,
        val cause: Throwable? = null
    ) : StateUI<Nothing>()

    data object Loading : StateUI<Nothing>()
    data object Idle : StateUI<Nothing>()

    fun isData(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    fun isLoading(): Boolean = this is Loading
    fun isIdle(): Boolean = this is Idle
}
