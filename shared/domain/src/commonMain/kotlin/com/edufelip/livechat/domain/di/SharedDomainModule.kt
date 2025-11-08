package com.edufelip.livechat.domain.di

import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.InviteContactUseCase
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveParticipantUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
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
        factory { GetLocalContactsUseCase(get<IContactsRepository>()) }
        factory { CheckRegisteredContactsUseCase(get<IContactsRepository>()) }
        factory { InviteContactUseCase(get<IContactsRepository>()) }
        factory { ObserveConversationUseCase(get()) }
        factory { ObserveConversationSummariesUseCase(get()) }
        factory { ObserveParticipantUseCase(get()) }
        factory { ObserveOnboardingStatusUseCase(get<IOnboardingStatusRepository>()) }
        factory { GetOnboardingStatusSnapshotUseCase(get<IOnboardingStatusRepository>()) }
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
        factory { ConversationPresenter(get(), get(), get(), get(), get(), get()) }
        factory { ConversationListPresenter(get(), get(), get(), get(), get()) }
        factory { ContactsPresenter(get(), get(), get(), get()) }
        factory { PhoneAuthPresenter(get(), get(), get(), get()) }
        factory { AppPresenter(get(), get(), get()) }
    }
