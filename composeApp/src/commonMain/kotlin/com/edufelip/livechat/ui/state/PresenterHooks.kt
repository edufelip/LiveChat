package com.edufelip.livechat.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.models.AccountUiState
import com.edufelip.livechat.domain.models.AppUiState
import com.edufelip.livechat.domain.models.AppearanceSettingsUiState
import com.edufelip.livechat.domain.models.BlockedContactsUiState
import com.edufelip.livechat.domain.models.ContactsUiState
import com.edufelip.livechat.domain.models.ConversationListUiState
import com.edufelip.livechat.domain.models.ConversationUiState
import com.edufelip.livechat.domain.models.NotificationSettingsUiState
import com.edufelip.livechat.domain.models.PrivacySettingsUiState
import com.edufelip.livechat.domain.presentation.AccountPresenter
import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.AppearanceSettingsPresenter
import com.edufelip.livechat.domain.presentation.BlockedContactsPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.NotificationSettingsPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter
import com.edufelip.livechat.domain.presentation.PrivacySettingsPresenter
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

@Composable
internal fun rememberAppPresenter(): AppPresenter {
    val presenter =
        remember {
            provideAppPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun AppPresenter.collectState(): State<AppUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberConversationListPresenter(): ConversationListPresenter {
    val presenter =
        remember {
            provideConversationListPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun ConversationListPresenter.collectState(): State<ConversationListUiState> = this.uiState.collectAsComposeState()

@Composable
internal fun rememberConversationPresenter(conversationId: String): ConversationPresenter {
    val presenter =
        remember(conversationId) {
            provideConversationPresenter()
        }
    val latestConversationId by rememberUpdatedState(conversationId)

    DisposableEffect(presenter, latestConversationId) {
        presenter.start(latestConversationId)
        onDispose { presenter.close() }
    }

    return presenter
}

@Composable
internal fun ConversationPresenter.collectState(): State<ConversationUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberContactsPresenter(): ContactsPresenter {
    return remember {
        provideContactsPresenter()
    }
}

@Composable
internal fun ContactsPresenter.collectState(): State<ContactsUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberAccountPresenter(): AccountPresenter {
    val presenter =
        remember {
            provideAccountPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun AccountPresenter.collectState(): State<AccountUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberNotificationSettingsPresenter(): NotificationSettingsPresenter {
    val presenter =
        remember {
            provideNotificationSettingsPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun NotificationSettingsPresenter.collectState(): State<NotificationSettingsUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberAppearanceSettingsPresenter(): AppearanceSettingsPresenter {
    val presenter =
        remember {
            provideAppearanceSettingsPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun AppearanceSettingsPresenter.collectState(): State<AppearanceSettingsUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberPrivacySettingsPresenter(): PrivacySettingsPresenter {
    val presenter =
        remember {
            providePrivacySettingsPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun PrivacySettingsPresenter.collectState(): State<PrivacySettingsUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberBlockedContactsPresenter(): BlockedContactsPresenter {
    val presenter =
        remember {
            provideBlockedContactsPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun BlockedContactsPresenter.collectState(): State<BlockedContactsUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberSessionProvider(): InMemoryUserSessionProvider {
    return remember {
        provideSessionProvider()
    }
}

@Composable
internal fun rememberPhoneAuthPresenter(): PhoneAuthPresenter {
    val presenter =
        remember {
            providePhoneAuthPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun PhoneAuthPresenter.collectState(): State<com.edufelip.livechat.domain.models.PhoneAuthUiState> =
    this.uiState.collectAsComposeState()

@Composable
private fun <T> StateFlow<T>.collectAsComposeState(): State<T> {
    val flow = this
    val state = remember { mutableStateOf(flow.value) }
    LaunchedEffect(flow) {
        flow.collect { state.value = it }
    }
    return state
}

internal expect fun provideConversationListPresenter(): ConversationListPresenter

internal expect fun provideConversationPresenter(): ConversationPresenter

internal expect fun provideContactsPresenter(): ContactsPresenter

internal expect fun provideAccountPresenter(): AccountPresenter

internal expect fun provideNotificationSettingsPresenter(): NotificationSettingsPresenter

internal expect fun provideAppearanceSettingsPresenter(): AppearanceSettingsPresenter

internal expect fun providePrivacySettingsPresenter(): PrivacySettingsPresenter

internal expect fun provideBlockedContactsPresenter(): BlockedContactsPresenter

internal expect fun provideSessionProvider(): InMemoryUserSessionProvider

internal expect fun provideAppPresenter(): AppPresenter

internal expect fun providePhoneAuthPresenter(): PhoneAuthPresenter
