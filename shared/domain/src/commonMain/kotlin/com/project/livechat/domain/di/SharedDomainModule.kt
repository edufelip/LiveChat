package com.project.livechat.domain.di

import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.domain.repositories.IContactsRepository
import com.project.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.project.livechat.domain.useCases.GetLocalContactsUseCase
import com.project.livechat.domain.useCases.ObserveConversationUseCase
import com.project.livechat.domain.useCases.SendMessageUseCase
import com.project.livechat.domain.useCases.SyncConversationUseCase
import com.project.livechat.domain.validation.PhoneNumberValidator
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedDomainModule: Module = module {
    single { PhoneNumberValidator() }
    factory { GetLocalContactsUseCase(get<IContactsRepository>()) }
    factory { CheckRegisteredContactsUseCase(get<IContactsRepository>()) }
    factory { ObserveConversationUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { SyncConversationUseCase(get()) }
    factory { ConversationPresenter(get(), get(), get(), get()) }
}
