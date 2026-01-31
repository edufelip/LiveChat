package com.edufelip.livechat.data.remote

import com.edufelip.livechat.data.bridge.RemoteConfigBridge
import com.edufelip.livechat.data.contracts.IRemoteConfigRemoteData

class IosRemoteConfigRemoteData(
    private val bridge: RemoteConfigBridge,
) : IRemoteConfigRemoteData {
    override suspend fun fetchAndActivate(): Boolean = bridge.fetchAndActivate().activated

    override fun getString(key: String): String = bridge.getString(key)
}
