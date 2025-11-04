package com.project.livechat.composeapp.ui.state

import com.project.livechat.data.di.IosKoinBridge
import com.project.livechat.data.session.InMemoryUserSessionProvider
import com.project.livechat.domain.presentation.ContactsPresenter
import com.project.livechat.domain.presentation.ConversationListPresenter
import com.project.livechat.domain.presentation.ConversationPresenter

internal actual fun provideConversationListPresenter(): ConversationListPresenter = IosKoinBridge.conversationListPresenter()

internal actual fun provideConversationPresenter(): ConversationPresenter = IosKoinBridge.conversationPresenter()

internal actual fun provideContactsPresenter(): ContactsPresenter = IosKoinBridge.contactsPresenter()

internal actual fun provideSessionProvider(): InMemoryUserSessionProvider = IosKoinBridge.sessionProvider()
