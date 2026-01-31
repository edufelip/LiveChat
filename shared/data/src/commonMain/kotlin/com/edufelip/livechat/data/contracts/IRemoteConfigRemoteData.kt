package com.edufelip.livechat.data.contracts

interface IRemoteConfigRemoteData {
    suspend fun fetchAndActivate(): Boolean

    fun getString(key: String): String
}
