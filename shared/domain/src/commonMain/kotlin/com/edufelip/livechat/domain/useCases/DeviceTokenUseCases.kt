package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.models.DeviceTokenRegistration
import com.edufelip.livechat.domain.repositories.IDeviceTokenRepository

class RegisterDeviceTokenUseCase(
    private val repository: IDeviceTokenRepository,
) {
    suspend operator fun invoke(registration: DeviceTokenRegistration) {
        repository.registerDeviceToken(registration)
    }
}

class UnregisterDeviceTokenUseCase(
    private val repository: IDeviceTokenRepository,
) {
    suspend operator fun invoke(deviceId: String) {
        repository.unregisterDeviceToken(deviceId)
    }
}
