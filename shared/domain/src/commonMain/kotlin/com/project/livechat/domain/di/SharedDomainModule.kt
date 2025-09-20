package com.project.livechat.domain.di

import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.repositories.IContactsRepository
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.useCases.ObserveConversationUseCase
import com.project.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.project.livechat.domain.useCases.SendMessageUseCase
import com.project.livechat.domain.useCases.SyncConversationUseCase
import com.project.livechat.domain.useCases.MarkConversationReadUseCase
import com.project.livechat.domain.useCases.SetConversationPinnedUseCase
import com.project.livechat.domain.useCases.InviteContactUseCase
import com.project.livechat.domain.validation.PhoneNumberValidator
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedDomainModule: Module = module {
    single { PhoneNumberValidator() }
    factory { GetLocalContactsUseCase(get<IContactsRepository>()) }
    factory { CheckRegisteredContactsUseCase(get<IContactsRepository>()) }
    factory { InviteContactUseCase(get<IContactsRepository>()) }
    factory { ObserveConversationUseCase(get()) }
    factory { ObserveConversationSummariesUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { SyncConversationUseCase(get()) }
    factory { MarkConversationReadUseCase(get()) }
    factory { SetConversationPinnedUseCase(get()) }
    factory { ConversationPresenter(get(), get(), get(), get()) }
    factory { ConversationListPresenter(get(), get(), get()) }
    factory { ContactsPresenter(get(), get(), get()) }
}
