package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.ConversationUiState
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.SendMessageUseCase
import com.edufelip.livechat.domain.useCases.SyncConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveParticipantUseCase
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.utils.CStateFlow
import com.edufelip.livechat.domain.utils.asCStateFlow
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ConversationPresenter(
    private val observeConversationUseCase: ObserveConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val syncConversationUseCase: SyncConversationUseCase,
    private val observeParticipantUseCase: ObserveParticipantUseCase,
    private val markConversationReadUseCase: MarkConversationReadUseCase,
    private val userSessionProvider: UserSessionProvider,
    private val scope: CoroutineScope = MainScope(),
) {
    private val _uiState = MutableStateFlow(ConversationUiState())
    val state = _uiState.asStateFlow()
    val uiState: CStateFlow<ConversationUiState> = state.asCStateFlow()

    private var observeJob: Job? = null
    private var participantJob: Job? = null
    private var markReadJob: Job? = null
    private var lastMarkedReadSeq: Long? = null
    private var lastMarkedReadAt: Long = 0L

    fun start(
        conversationId: String,
        pageSize: Int = IMessagesRepository.DEFAULT_PAGE_SIZE,
    ) {
        if (conversationId.isBlank()) return
        val currentId = _uiState.value.conversationId
        if (currentId == conversationId) return

        _uiState.update {
            it.copy(
                conversationId = conversationId,
                isLoading = true,
                errorMessage = null,
                participant = null,
                isMuted = false,
                muteUntil = null,
                isArchived = false,
            )
        }
        lastMarkedReadSeq = null
        lastMarkedReadAt = 0L
        markReadJob?.cancel()
        markReadJob = null

        observeJob?.cancel()
        observeJob =
            scope.launch {
                observeConversationUseCase(conversationId, pageSize)
                    .catch { throwable ->
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Failed to observe conversation",
                            )
                        }
                    }
                    .collect { messages ->
                        _uiState.update { state ->
                            state.copy(
                                messages = messages,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                        maybeMarkConversationRead(messages)
                    }
            }

        participantJob?.cancel()
        participantJob =
            scope.launch {
                observeParticipantUseCase(conversationId)
                    .catch { throwable ->
                        _uiState.update { state ->
                            state.copy(errorMessage = state.errorMessage ?: throwable.message)
                        }
                    }
                    .collectLatest { participant ->
                        val mutedUntil = participant?.muteUntil
                        val isMuted =
                            mutedUntil?.let { it > currentEpochMillis() } ?: false
                        _uiState.update { state ->
                            state.copy(
                                participant = participant,
                                isMuted = isMuted,
                                muteUntil = mutedUntil,
                                isArchived = participant?.archived ?: false,
                            )
                        }
                        maybeMarkConversationRead(_uiState.value.messages)
                    }
            }

        scope.launch {
            runCatching {
                syncConversationUseCase(conversationId, null)
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load conversation",
                    )
                }
            }.onSuccess {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
            }
        }
    }

    private fun maybeMarkConversationRead(messages: List<Message>) {
        if (messages.isEmpty()) return
        val conversationId = _uiState.value.conversationId
        if (conversationId.isBlank()) return
        val latest = messages.maxByOrNull { it.createdAt } ?: return
        val latestSeq = latest.messageSeq
        val participant = _uiState.value.participant
        val participantSeq = participant?.lastReadSeq
        val participantReadAt = participant?.lastReadAt ?: 0L
        val knownSeq = listOfNotNull(participantSeq, lastMarkedReadSeq).maxOrNull()

        val alreadyAcknowledgedBySeq =
            latestSeq != null && knownSeq != null && latestSeq <= knownSeq
        val alreadyAcknowledgedByTime =
            latestSeq == null && latest.createdAt <= maxOf(participantReadAt, lastMarkedReadAt)

        if (alreadyAcknowledgedBySeq || alreadyAcknowledgedByTime) return

        dispatchMarkConversationRead(conversationId, latest.createdAt, latestSeq)
    }

    private fun dispatchMarkConversationRead(
        conversationId: String,
        lastReadAt: Long,
        lastReadSeq: Long?,
    ) {
        markReadJob?.cancel()
        markReadJob =
            scope.launch {
                runCatching {
                    markConversationReadUseCase(conversationId, lastReadAt, lastReadSeq)
                }.onSuccess {
                    lastReadSeq?.let { seq ->
                        val baseline = lastMarkedReadSeq ?: Long.MIN_VALUE
                        lastMarkedReadSeq = maxOf(baseline, seq)
                    }
                    lastMarkedReadAt = maxOf(lastMarkedReadAt, lastReadAt)
                    _uiState.update { state ->
                        val participant = state.participant
                        if (participant == null) {
                            state
                        } else {
                            state.copy(
                                participant =
                                    participant.copy(
                                        lastReadSeq = lastReadSeq ?: participant.lastReadSeq,
                                        lastReadAt = maxOf(participant.lastReadAt ?: 0L, lastReadAt),
                                    ),
                            )
                        }
                    }
                }.onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(errorMessage = state.errorMessage ?: throwable.message)
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
                        errorMessage = throwable.message ?: "Failed to refresh conversation",
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

            val timestamp = currentEpochMillis()
            val localId = "ios-$timestamp-${Random.nextInt()}"

            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            runCatching {
                sendMessageUseCase(
                    MessageDraft(
                        conversationId = conversationId,
                        senderId = senderId,
                        body = body,
                        localId = localId,
                        createdAt = timestamp,
                    ),
                )
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.message ?: "Failed to send message",
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
        participantJob?.cancel()
        participantJob = null
        markReadJob?.cancel()
        markReadJob = null
        lastMarkedReadSeq = null
        lastMarkedReadAt = 0L
        _uiState.value = ConversationUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun close() {
        stop()
        scope.cancel()
    }
}
