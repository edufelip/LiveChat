package com.project.livechat.domain.presentation

import com.project.livechat.domain.models.ConversationUiState
import com.project.livechat.domain.models.MessageDraft
import com.project.livechat.domain.providers.UserSessionProvider
import com.project.livechat.domain.repositories.IMessagesRepository
import com.project.livechat.domain.useCases.ObserveConversationUseCase
import com.project.livechat.domain.useCases.SendMessageUseCase
import com.project.livechat.domain.useCases.SyncConversationUseCase
import com.project.livechat.domain.utils.CStateFlow
import com.project.livechat.domain.utils.asCStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlin.random.Random
import kotlinx.datetime.Clock

class ConversationPresenter(
    private val observeConversationUseCase: ObserveConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val syncConversationUseCase: SyncConversationUseCase,
    private val userSessionProvider: UserSessionProvider,
    private val scope: CoroutineScope = MainScope()
) {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val state = _uiState.asStateFlow()
    val uiState: CStateFlow<ConversationUiState> = state.asCStateFlow()

    private var observeJob: Job? = null

    fun start(conversationId: String, pageSize: Int = IMessagesRepository.DEFAULT_PAGE_SIZE) {
        if (conversationId.isBlank()) return
        val currentId = _uiState.value.conversationId
        if (currentId == conversationId) return

        _uiState.update {
            it.copy(
                conversationId = conversationId,
                isLoading = true,
                errorMessage = null
            )
        }

        observeJob?.cancel()
        observeJob = scope.launch {
            observeConversationUseCase(conversationId, pageSize)
                .catch { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Failed to observe conversation"
                        )
                    }
                }
                .collect { messages ->
                    _uiState.update { state ->
                        state.copy(
                            messages = messages,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }

        scope.launch {
            runCatching {
                syncConversationUseCase(conversationId, null)
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load conversation"
                    )
                }
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
            }
        }
    }

    fun refresh() {
        val conversationId = _uiState.value.conversationId
        if (conversationId.isBlank()) return

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                syncConversationUseCase(conversationId, null)
            }.onSuccess { messages ->
                _uiState.update {
                    it.copy(messages = messages, isLoading = false)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to refresh conversation"
                    )
                }
            }
        }
    }

    fun sendMessage(body: String) {
        if (body.isBlank()) return
        val conversationId = _uiState.value.conversationId
        if (conversationId.isBlank()) return

        scope.launch {
            val senderId = userSessionProvider.currentUserId()
            if (senderId.isNullOrBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "User not authenticated")
                }
                return@launch
            }

            val timestamp = Clock.System.now().toEpochMilliseconds()
            val localId = "ios-${timestamp}-${Random.nextInt()}"

            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            runCatching {
                sendMessageUseCase(
                    MessageDraft(
                        conversationId = conversationId,
                        senderId = senderId,
                        body = body,
                        localId = localId,
                        createdAt = timestamp
                    )
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.message ?: "Failed to send message"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
        _uiState.value = ConversationUiState()
    }

    fun close() {
        stop()
        scope.cancel()
    }
}
