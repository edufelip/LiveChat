package com.project.livechat.ui

import androidx.lifecycle.ViewModel
import com.project.livechat.domain.validators.ValidationResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

open class ValidationViewModel: ViewModel() {
    protected val validationEventChannel = Channel<ValidationResult>()
    val validationEvents = validationEventChannel.receiveAsFlow()
}