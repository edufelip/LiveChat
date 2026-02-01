package com.edufelip.livechat.data.repositories

import com.edufelip.livechat.data.contracts.IPresenceRemoteData
import com.edufelip.livechat.data.remote.FirebaseRestConfig
import com.edufelip.livechat.data.store.PrivacySettingsStore
import com.edufelip.livechat.domain.models.LastSeenAudience
import com.edufelip.livechat.domain.models.PresenceState
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IPresenceRepository
import com.edufelip.livechat.domain.utils.currentEpochMillis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PresenceRepository(
    private val remoteData: IPresenceRemoteData,
    private val sessionProvider: UserSessionProvider,
    private val config: FirebaseRestConfig,
    private val privacySettingsStore: PrivacySettingsStore,
    private val contactsRepository: IContactsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : IPresenceRepository {
    override fun observePresence(userIds: List<String>): Flow<Map<String, PresenceState>> {
        val sanitized = userIds.distinct().filter { it.isNotBlank() }
        return privacySettingsStore.settings
            .map { it.lastSeenAudience }
            .distinctUntilChanged()
            .flatMapLatest { audience ->
                when (audience) {
                    LastSeenAudience.Nobody -> flowOf(emptyMap())
                    LastSeenAudience.Contacts ->
                        contactsRepository
                            .getLocalContacts()
                            .map { contacts -> contacts.mapNotNull { it.firebaseUid }.toSet() }
                            .distinctUntilChanged()
                            .flatMapLatest { contactIds ->
                                observePresenceInternal(sanitized.filter { contactIds.contains(it) })
                            }

                    LastSeenAudience.Everyone -> observePresenceInternal(sanitized)
                }
            }.flowOn(dispatcher)
    }

    override suspend fun updateSelfPresence(isOnline: Boolean) {
        withContext(dispatcher) {
            if (privacySettingsStore.lastSeenAudience() == LastSeenAudience.Nobody) return@withContext
            val session = sessionProvider.refreshSession() ?: return@withContext
            val token = session.idToken.takeIf { it.isNotBlank() } ?: return@withContext
            remoteData.updatePresence(
                userId = session.userId,
                idToken = token,
                isOnline = isOnline,
                lastActiveAt = currentEpochMillis(),
            )
        }
    }

    private fun observePresenceInternal(userIds: List<String>): Flow<Map<String, PresenceState>> =
        flow {
            val targets = userIds.distinct().filter { it.isNotBlank() }
            if (targets.isEmpty()) {
                emit(emptyMap())
                return@flow
            }
            while (true) {
                val session = sessionProvider.refreshSession()
                val token = session?.idToken.orEmpty()
                if (token.isBlank()) {
                    emit(emptyMap())
                    delay(config.pollingIntervalMs)
                    continue
                }
                val snapshot = remoteData.fetchPresence(targets, token)
                emit(resolvePresence(snapshot))
                delay(config.pollingIntervalMs)
            }
        }

    private fun resolvePresence(snapshot: Map<String, PresenceState>): Map<String, PresenceState> {
        if (snapshot.isEmpty()) return snapshot
        val now = currentEpochMillis()
        return snapshot.mapValues { (_, state) ->
            if (now - state.lastActiveAt > ACTIVE_WINDOW_MS) {
                state.copy(isOnline = false)
            } else {
                state
            }
        }
    }

    private companion object {
        const val ACTIVE_WINDOW_MS = 2 * 60 * 1000L
    }
}
