package com.edufelip.livechat.di

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
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.useCases.GetLocalContactsSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetWelcomeSeenSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.ObserveWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.edufelip.livechat.domain.useCases.SetWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.UpdateSelfPresenceUseCase
import kotlinx.coroutines.MainScope
import org.koin.core.module.Module
import org.koin.dsl.module

val presentationModule: Module =
    module {
        factory { ConversationPresenter(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { ConversationListPresenter(get(), get(), get(), get(), get(), get(), get()) }
        single {
            ContactsPresenter(
                getLocalContactsUseCase = get(),
                checkRegisteredContactsUseCase = get(),
                resolveConversationIdForContactUseCase = get(),
                ensureConversationUseCase = get(),
                phoneNumberFormatter = get(),
                scope = MainScope(),
            )
        }
        factory { PhoneAuthPresenter(get(), get(), get(), get()) }
        factory {
            AccountPresenter(
                observeAccountProfile = get(),
                updateDisplayName = get(),
                updateStatusMessage = get(),
                updateEmail = get(),
                sendEmailVerification = get(),
                checkEmailUpdated = get(),
                deleteAccount = get(),
                scope = MainScope(),
            )
        }
        factory {
            NotificationSettingsPresenter(
                observeSettings = get(),
                updatePushEnabled = get(),
                updateSound = get(),
                updateQuietHoursEnabled = get(),
                updateQuietHoursWindow = get(),
                updateInAppVibration = get(),
                updateShowMessagePreview = get(),
                resetSettings = get(),
                scope = MainScope(),
            )
        }
        factory {
            AppearanceSettingsPresenter(
                observeSettings = get(),
                updateThemeMode = get(),
                updateTextScale = get(),
                updateReduceMotion = get(),
                updateHighContrast = get(),
                resetSettings = get(),
                scope = MainScope(),
            )
        }
        factory {
            PrivacySettingsPresenter(
                observeSettings = get(),
                updateInvitePreference = get(),
                updateLastSeenAudience = get(),
                updateReadReceipts = get(),
                updateShareUsageData = get(),
                resetSettings = get(),
                scope = MainScope(),
            )
        }
        factory {
            BlockedContactsPresenter(
                observeBlockedContacts = get(),
                blockContact = get(),
                unblockContact = get(),
                scope = MainScope(),
            )
        }
        factory {
            AppPresenter(
                observeOnboardingStatus = get<ObserveOnboardingStatusUseCase>(),
                observeWelcomeSeen = get<ObserveWelcomeSeenUseCase>(),
                observeConversationUseCase = get<ObserveConversationUseCase>(),
                observeConversationSummaries = get<ObserveConversationSummariesUseCase>(),
                setOnboardingComplete = get<SetOnboardingCompleteUseCase>(),
                setWelcomeSeen = get<SetWelcomeSeenUseCase>(),
                getOnboardingStatusSnapshot = get<GetOnboardingStatusSnapshotUseCase>(),
                getWelcomeSeenSnapshot = get<GetWelcomeSeenSnapshotUseCase>(),
                getLocalContactsSnapshot = get<GetLocalContactsSnapshotUseCase>(),
                updateSelfPresence = get<UpdateSelfPresenceUseCase>(),
                sessionProvider = get<UserSessionProvider>(),
                scope = MainScope(),
            )
        }
    }
