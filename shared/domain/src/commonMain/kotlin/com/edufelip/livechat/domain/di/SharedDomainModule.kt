package com.edufelip.livechat.domain.di

import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.useCases.ApplyContactSyncPlanUseCase
import com.edufelip.livechat.domain.useCases.BuildContactSyncPlanUseCase
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveParticipantUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.ObserveContactByPhoneUseCase
import com.edufelip.livechat.domain.useCases.ResolveConversationIdForContactUseCase
import com.edufelip.livechat.domain.useCases.EnsureConversationUseCase
import com.edufelip.livechat.domain.useCases.ValidateContactsUseCase
import com.edufelip.livechat.domain.providers.UserSessionProvider
import com.edufelip.livechat.domain.utils.DefaultPhoneNumberFormatter
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter
import kotlinx.coroutines.MainScope
import com.edufelip.livechat.domain.useCases.SendMessageUseCase
import com.edufelip.livechat.domain.useCases.SetConversationArchivedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationMutedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationPinnedUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.edufelip.livechat.domain.useCases.SyncConversationUseCase
import com.edufelip.livechat.domain.useCases.phone.ClearPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.RequestPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.ResendPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.VerifyOtpUseCase
import com.edufelip.livechat.domain.validation.PhoneNumberValidator
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedDomainModule: Module =
    module {
        single { PhoneNumberValidator() }
        single<PhoneNumberFormatter> { DefaultPhoneNumberFormatter() }
        factory { GetLocalContactsUseCase(get<IContactsRepository>()) }
        factory { BuildContactSyncPlanUseCase(get()) }
        factory { ApplyContactSyncPlanUseCase(get<IContactsRepository>()) }
        factory { ValidateContactsUseCase(get<IContactsRepository>(), phoneNumberFormatter = get()) }
        factory { CheckRegisteredContactsUseCase(get(), get(), get()) }
        factory { ObserveContactByPhoneUseCase(get<IContactsRepository>()) }
        factory { ObserveConversationUseCase(get()) }
        factory { ObserveConversationSummariesUseCase(get()) }
        factory { ObserveParticipantUseCase(get()) }
        factory { ObserveOnboardingStatusUseCase(get<IOnboardingStatusRepository>()) }
        factory { GetOnboardingStatusSnapshotUseCase(get<IOnboardingStatusRepository>()) }
        factory { ResolveConversationIdForContactUseCase(get(), get()) }
        factory { EnsureConversationUseCase(get()) }
        factory { SendMessageUseCase(get()) }
        factory { SyncConversationUseCase(get()) }
        factory { MarkConversationReadUseCase(get()) }
        factory { SetConversationPinnedUseCase(get()) }
        factory { SetConversationMutedUseCase(get()) }
        factory { SetConversationArchivedUseCase(get()) }
        factory { SetOnboardingCompleteUseCase(get()) }
        factory { RequestPhoneVerificationUseCase(get<IPhoneAuthRepository>()) }
        factory { ResendPhoneVerificationUseCase(get<IPhoneAuthRepository>()) }
        factory { VerifyOtpUseCase(get<IPhoneAuthRepository>()) }
        factory { ClearPhoneVerificationUseCase(get<IPhoneAuthRepository>()) }
        factory { ConversationPresenter(get(), get(), get(), get(), get(), get(), get(), get()) }
        factory { ConversationListPresenter(get(), get(), get(), get(), get()) }
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
        factory { AppPresenter(get(), get(), get(), scope = MainScope()) }
    }
