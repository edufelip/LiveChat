package com.project.livechat.composeapp.ui.state

import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.di.AndroidKoinBridge
import com.project.livechat.domain.presentation.AppPresenter
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ConversationPresenter
import com.project.livechat.domain.presentation.PhoneAuthPresenter

internal actual fun provideConversationListPresenter(): ConversationListPresenter = AndroidKoinBridge.conversationListPresenter()

internal actual fun provideConversationPresenter(): ConversationPresenter = AndroidKoinBridge.conversationPresenter()

internal actual fun provideContactsPresenter(): ContactsPresenter = AndroidKoinBridge.contactsPresenter()

internal actual fun provideSessionProvider(): InMemoryUserSessionProvider = AndroidKoinBridge.sessionProvider()

internal actual fun provideAppPresenter(): AppPresenter = AndroidKoinBridge.appPresenter()

internal actual fun providePhoneAuthPresenter(): PhoneAuthPresenter = AndroidKoinBridge.phoneAuthPresenter()
