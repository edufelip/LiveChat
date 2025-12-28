package com.edufelip.livechat.domain.presentation

import com.edufelip.livechat.domain.models.ConversationUiState
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageContentType
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.models.MessageStatus
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.useCases.DeleteMessageLocalUseCase
import com.edufelip.livechat.domain.useCases.EnsureConversationUseCase
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.ObserveContactByPhoneUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveParticipantUseCase
import com.edufelip.livechat.domain.useCases.SendMessageUseCase
import com.edufelip.livechat.domain.useCases.SyncConversationUseCase
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ConversationPresenter(
    private val observeConversationUseCase: ObserveConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val deleteMessageLocalUseCase: DeleteMessageLocalUseCase,
    private val syncConversationUseCase: SyncConversationUseCase,
    private val observeParticipantUseCase: ObserveParticipantUseCase,
    private val markConversationReadUseCase: MarkConversationReadUseCase,
    private val observeContactByPhoneUseCase: ObserveContactByPhoneUseCase,
    private val ensureConversationUseCase: EnsureConversationUseCase,
    private val userSessionProvider: UserSessionProvider,
    private val scope: CoroutineScope = MainScope(),
) {
    private val _state = MutableStateFlow(ConversationUiState())
    val state = _state.asStateFlow()

    private var observeJob: Job? = null
    private var participantJob: Job? = null
    private var markReadJob: Job? = null
    private var contactJob: Job? = null
    private var lastMarkedReadSeq: Long? = null
    private var lastMarkedReadAt: Long = 0L

    fun start(
        conversationId: String,
        pageSize: Int = IMessagesRepository.DEFAULT_PAGE_SIZE,
    ) {
        if (conversationId.isBlank()) return
        val currentId = _state.value.conversationId
        if (currentId == conversationId) return

        _state.update { current ->
            val preservedName =
                current.contactName.takeIf {
                    current.conversationId == conversationId
                } ?: ""
            current.copy(
                conversationId = conversationId,
                contactName = preservedName,
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

        scope.launch {
            runCatching { ensureConversationUseCase(conversationId) }
                .onFailure { throwable ->
                    _state.update { state ->
                        state.copy(
                            errorMessage = throwable.message ?: state.errorMessage,
                            isLoading = false,
                        )
                    }
                }
        }

        contactJob?.cancel()
        contactJob =
            scope.launch {
                observeContactByPhoneUseCase(conversationId).collect { contact ->
                    _state.update { state ->
                        val fallback = state.contactName.takeIf { it.isNotBlank() }
                        state.copy(
                            contactName =
                                contact?.name?.takeIf { it.isNotBlank() }
                                    ?: contact?.phoneNo?.takeIf { it.isNotBlank() }
                                    ?: fallback
                                    ?: "",
                        )
                    }
                }
            }

        observeJob?.cancel()
        observeJob =
            scope.launch {
                observeConversationUseCase(conversationId, pageSize)
                    .catch { throwable ->
                        _state.update { state ->
                            state.copy(
                                isLoading = false,
                                errorMessage = throwable.message ?: "Failed to observe conversation",
                            )
                        }
                    }
                    .collect { messages ->
                        _state.update { state ->
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
                        _state.update { state ->
                            state.copy(errorMessage = state.errorMessage ?: throwable.message)
                        }
                    }
                    .collectLatest { participant ->
                        val mutedUntil = participant?.muteUntil
                        val isMuted =
                            mutedUntil?.let { it > currentEpochMillis() } ?: false
                        _state.update { state ->
                            state.copy(
                                participant = participant,
                                isMuted = isMuted,
                                muteUntil = mutedUntil,
                                isArchived = participant?.archived ?: false,
                            )
                        }
                        maybeMarkConversationRead(_state.value.messages)
                    }
            }

        scope.launch {
            runCatching {
                syncConversationUseCase(conversationId, null)
            }.onFailure { throwable ->
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to load conversation",
                    )
                }
            }.onSuccess {
                _state.update { state ->
                    state.copy(isLoading = false)
                }
            }
        }
    }

    private fun maybeMarkConversationRead(messages: List<Message>) {
        if (messages.isEmpty()) return
        val conversationId = _state.value.conversationId
        if (conversationId.isBlank()) return
        val participant = _state.value.participant ?: return
        val currentUserId = userSessionProvider.currentUserId() ?: return
        val latestIncoming =
            messages
                .asSequence()
                .filter { it.senderId != currentUserId }
                .maxByOrNull { it.createdAt }
                ?: return
        val latestSeq = latestIncoming.messageSeq
        val participantSeq = participant.lastReadSeq
        val participantReadAt = participant.lastReadAt ?: 0L
        val knownSeq = listOfNotNull(participantSeq, lastMarkedReadSeq).maxOrNull()
        val knownReadAt = maxOf(participantReadAt, lastMarkedReadAt)

        val alreadyAcknowledged =
            if (latestSeq != null && knownSeq != null) {
                latestSeq <= knownSeq
            } else {
                latestIncoming.createdAt <= knownReadAt
            }

        if (alreadyAcknowledged) return

        dispatchMarkConversationRead(conversationId, latestIncoming.createdAt, latestSeq)
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
                    _state.update { state ->
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
                    _state.update { state ->
                        state.copy(errorMessage = state.errorMessage ?: throwable.message)
                    }
                }
            }
    }

    fun sendMessage(body: String) {
        sendMessage(body = body, contentType = MessageContentType.Text)
    }

    fun sendImage(localPath: String) {
        sendMessage(body = localPath, contentType = MessageContentType.Image)
    }

    fun sendAudio(localPath: String) {
        sendMessage(body = localPath, contentType = MessageContentType.Audio)
    }

    fun deleteMessageLocal(message: Message) {
        val messageId = message.localTempId ?: message.id
        if (messageId.isBlank()) return
        scope.launch {
            runCatching {
                deleteMessageLocalUseCase(messageId)
            }.onFailure { throwable ->
                _state.update { state ->
                    state.copy(errorMessage = state.errorMessage ?: throwable.message)
                }
            }
        }
    }

    fun retryMessage(message: Message) {
        if (message.status != MessageStatus.ERROR) return
        if (_state.value.isSending) return

        scope.launch {
            val senderId = userSessionProvider.currentUserId()
            if (senderId.isNullOrBlank()) {
                _state.update {
                    it.copy(errorMessage = "User not authenticated")
                }
                return@launch
            }

            val localId = message.localTempId ?: message.id
            val timestamp = currentEpochMillis()

            _state.update { it.copy(isSending = true, errorMessage = null) }
            runCatching {
                sendMessageUseCase(
                    MessageDraft(
                        conversationId = message.conversationId,
                        senderId = senderId,
                        body = message.body,
                        localId = localId,
                        createdAt = timestamp,
                        contentType = message.contentType,
                        ciphertext = message.ciphertext,
                        attachments = message.attachments,
                        replyToMessageId = message.replyToMessageId,
                        threadRootId = message.threadRootId,
                        metadata = message.metadata,
                    ),
                )
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.message ?: "Failed to send message",
                    )
                }
                return@launch
            }

            _state.update { it.copy(isSending = false) }
        }
    }

    private fun sendMessage(
        body: String,
        contentType: MessageContentType,
    ) {
        if (body.isBlank()) return
        val conversationId = _state.value.conversationId
        if (conversationId.isBlank()) return
        if (_state.value.isSending) return

        scope.launch {
            val senderId = userSessionProvider.currentUserId()
            if (senderId.isNullOrBlank()) {
                _state.update {
                    it.copy(errorMessage = "User not authenticated")
                }
                return@launch
            }

            val timestamp = currentEpochMillis()
            val localId = "ios-$timestamp-${Random.nextInt()}"

            _state.update { it.copy(isSending = true, errorMessage = null) }
            runCatching {
                sendMessageUseCase(
                    MessageDraft(
                        conversationId = conversationId,
                        senderId = senderId,
                        body = body,
                        localId = localId,
                        createdAt = timestamp,
                        contentType = contentType,
                    ),
                )
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.message ?: "Failed to send message",
                    )
                }
                return@launch
            }

            _state.update { it.copy(isSending = false) }
        }
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
        participantJob?.cancel()
        participantJob = null
        markReadJob?.cancel()
        markReadJob = null
        contactJob?.cancel()
        contactJob = null
        lastMarkedReadSeq = null
        lastMarkedReadAt = 0L
        _state.value = ConversationUiState()
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun close() {
        stop()
        scope.cancel()
    }
}
