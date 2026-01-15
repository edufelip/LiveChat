package com.edufelip.livechat.domain.useCases

import com.edufelip.livechat.domain.auth.phone.model.PhoneAuthResult
import com.edufelip.livechat.domain.auth.phone.model.PhoneVerificationSession
import com.edufelip.livechat.domain.models.ConversationPeer
import com.edufelip.livechat.domain.models.ConversationSummary
import com.edufelip.livechat.domain.models.Message
import com.edufelip.livechat.domain.models.MessageDraft
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IMessagesRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.useCases.phone.VerifyOtpUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EnsureUserInboxUseCaseTest {
    @Test
    fun ensuresInboxForCurrentUser() =
        runTest {
            val sessionProvider = FakeSessionProvider(userId = "user123")
            val repository = FakeMessagesRepository()
            val useCase = EnsureUserInboxUseCase(repository, sessionProvider)

            useCase()

            assertEquals("user123", repository.lastEnsureConversationId)
            assertNull(repository.lastEnsureConversationPeer)
        }

    @Test
    fun doesNothingWhenNoCurrentUser() =
        runTest {
            val sessionProvider = FakeSessionProvider(userId = null)
            val repository = FakeMessagesRepository()
            val useCase = EnsureUserInboxUseCase(repository, sessionProvider)

            useCase()

            assertNull(repository.lastEnsureConversationId)
        }

    @Test
    fun propagatesExceptionsFromRepository() =
        runTest {
            val sessionProvider = FakeSessionProvider(userId = "user123")
            val repository =
                FakeMessagesRepository().apply {
                    shouldThrowOnEnsure = true
                }
            val useCase = EnsureUserInboxUseCase(repository, sessionProvider)

            var caughtException: Throwable? = null
            try {
                useCase()
            } catch (e: Exception) {
                caughtException = e
            }

            assertEquals("Failed to ensure conversation", caughtException?.message)
        }

    @Test
    fun createsInboxAfterSuccessfulPhoneAuth() =
        runTest {
            val sessionProvider = FakeSessionProvider(userId = "user456")
            val messagesRepository = FakeMessagesRepository()
            val phoneAuthRepository = FakePhoneAuthRepository()
            val ensureUserInbox = EnsureUserInboxUseCase(messagesRepository, sessionProvider)
            val verifyOtp = VerifyOtpUseCase(phoneAuthRepository, ensureUserInbox)

            val session =
                PhoneVerificationSession(
                    verificationId = "verify123",
                    phoneNumber = "+1234567890",
                    resendToken = null,
                )

            val result = verifyOtp(session, "123456")

            assertTrue(result is PhoneAuthResult.Success)
            assertEquals("user456", messagesRepository.lastEnsureConversationId)
            assertNull(messagesRepository.lastEnsureConversationPeer)
        }

    @Test
    fun doesNotCreateInboxOnFailedPhoneAuth() =
        runTest {
            val sessionProvider = FakeSessionProvider(userId = "user789")
            val messagesRepository = FakeMessagesRepository()
            val phoneAuthRepository =
                FakePhoneAuthRepository().apply {
                    shouldFail = true
                }
            val ensureUserInbox = EnsureUserInboxUseCase(messagesRepository, sessionProvider)
            val verifyOtp = VerifyOtpUseCase(phoneAuthRepository, ensureUserInbox)

            val session =
                PhoneVerificationSession(
                    verificationId = "verify123",
                    phoneNumber = "+1234567890",
                    resendToken = null,
                )

            val result = verifyOtp(session, "123456")

            assertTrue(result is PhoneAuthResult.Failure)
            assertNull(messagesRepository.lastEnsureConversationId)
        }

    private class FakeSessionProvider(
        private val userId: String?,
        private val userPhone: String? = null,
    ) : UserSessionProvider {
        override fun currentUserId(): String? = userId

        override fun currentUserPhone(): String? = userPhone
    }

    private class FakeMessagesRepository : IMessagesRepository {
        var lastEnsureConversationId: String? = null
        var lastEnsureConversationPeer: ConversationPeer? = null
        var shouldThrowOnEnsure = false

        override fun observeConversation(
            conversationId: String,
            pageSize: Int,
        ): Flow<List<Message>> = emptyFlow()

        override suspend fun sendMessage(draft: MessageDraft): Message = throw NotImplementedError()

        override suspend fun deleteMessageLocal(messageId: String) = throw NotImplementedError()

        override suspend fun syncConversation(
            conversationId: String,
            sinceEpochMillis: Long?,
        ): List<Message> = throw NotImplementedError()

        override fun observeConversationSummaries(): Flow<List<ConversationSummary>> = emptyFlow()

        override suspend fun markConversationAsRead(
            conversationId: String,
            lastReadAt: Long,
            lastReadSeq: Long?,
        ) = throw NotImplementedError()

        override suspend fun setConversationPinned(
            conversationId: String,
            pinned: Boolean,
            pinnedAt: Long?,
        ) = throw NotImplementedError()

        override suspend fun purgeConversation(conversationId: String) = throw NotImplementedError()

        override suspend fun hideReadReceipts() = throw NotImplementedError()

        override suspend fun ensureConversation(
            conversationId: String,
            peer: ConversationPeer?,
        ) {
            if (shouldThrowOnEnsure) {
                throw Exception("Failed to ensure conversation")
            }
            lastEnsureConversationId = conversationId
            lastEnsureConversationPeer = peer
        }

        override fun observeAllIncomingMessages(): Flow<List<Message>> = emptyFlow()
    }

    private class FakePhoneAuthRepository : IPhoneAuthRepository {
        var shouldFail = false

        override suspend fun verifyCode(
            session: PhoneVerificationSession,
            code: String,
        ): PhoneAuthResult {
            return if (shouldFail) {
                PhoneAuthResult.Failure(com.edufelip.livechat.domain.auth.phone.model.PhoneAuthError.InvalidVerificationCode)
            } else {
                PhoneAuthResult.Success
            }
        }

        override fun requestVerification(
            phoneNumber: com.edufelip.livechat.domain.auth.phone.model.PhoneNumber,
            presentationContext: com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext,
        ): Flow<com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent> = emptyFlow()

        override fun resendVerification(
            session: PhoneVerificationSession,
            presentationContext: com.edufelip.livechat.domain.auth.phone.model.PhoneAuthPresentationContext,
        ): Flow<com.edufelip.livechat.domain.auth.phone.model.PhoneAuthEvent> = emptyFlow()

        override fun clearVerification() = Unit
    }
}
