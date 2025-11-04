package com.project.livechat.composeapp.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.domain.models.ContactsUiState
import com.project.livechat.domain.models.ConversationListUiState
import com.project.livechat.domain.models.ConversationUiState
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ConversationPresenter
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect

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
    val presenter =
        remember {
            provideContactsPresenter()
        }
    DisposableEffect(presenter) {
        onDispose { presenter.close() }
    }
    return presenter
}

@Composable
internal fun ContactsPresenter.collectState(): State<ContactsUiState> = this.state.collectAsComposeState()

@Composable
internal fun rememberSessionProvider(): InMemoryUserSessionProvider {
    return remember {
        provideSessionProvider()
    }
}

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

internal expect fun provideSessionProvider(): InMemoryUserSessionProvider
