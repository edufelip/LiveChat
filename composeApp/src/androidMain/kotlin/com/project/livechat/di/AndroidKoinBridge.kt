package com.project.livechat.di

import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.domain.presentation.AppPresenter
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.domain.presentation.PhoneAuthPresenter
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object AndroidKoinBridge : KoinComponent {
    fun conversationListPresenter(): ConversationListPresenter = get()

    fun conversationPresenter(): ConversationPresenter = get()

    fun contactsPresenter(): ContactsPresenter = get()

    fun sessionProvider(): InMemoryUserSessionProvider = get()

    fun appPresenter(): AppPresenter = get()

    fun phoneAuthPresenter(): PhoneAuthPresenter = get()
}
