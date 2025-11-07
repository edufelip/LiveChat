package com.project.livechat.domain.di

import com.project.livechat.domain.presentation.AppPresenter
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.domain.presentation.PhoneAuthPresenter
import com.project.livechat.domain.repositories.IContactsRepository
import com.project.livechat.domain.repositories.IOnboardingStatusRepository
import com.project.livechat.domain.repositories.IPhoneAuthRepository
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.useCases.InviteContactUseCase
import com.project.livechat.domain.useCases.MarkConversationReadUseCase
import com.project.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.project.livechat.domain.useCases.ObserveConversationUseCase
import com.project.livechat.domain.useCases.ObserveParticipantUseCase
import com.project.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.project.livechat.domain.useCases.SendMessageUseCase
import com.project.livechat.domain.useCases.SetConversationArchivedUseCase
import com.project.livechat.domain.useCases.SetConversationMutedUseCase
import com.project.livechat.domain.useCases.SetConversationPinnedUseCase
import com.project.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.project.livechat.domain.useCases.SyncConversationUseCase
import com.project.livechat.domain.useCases.phone.ClearPhoneVerificationUseCase
import com.project.livechat.domain.useCases.phone.RequestPhoneVerificationUseCase
import com.project.livechat.domain.useCases.phone.ResendPhoneVerificationUseCase
import com.project.livechat.domain.useCases.phone.VerifyOtpUseCase
import com.project.livechat.domain.validation.PhoneNumberValidator
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
        factory { AppPresenter(get(), get()) }
    }
