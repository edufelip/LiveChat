package com.edufelip.livechat.ui.state

import com.edufelip.livechat.data.di.IosKoinBridge
import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.domain.presentation.AccountPresenter
import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter

internal actual fun provideConversationListPresenter(): ConversationListPresenter = IosKoinBridge.conversationListPresenter()

internal actual fun provideConversationPresenter(): ConversationPresenter = IosKoinBridge.conversationPresenter()

internal actual fun provideContactsPresenter(): ContactsPresenter = IosKoinBridge.contactsPresenter()

internal actual fun provideAccountPresenter(): AccountPresenter = IosKoinBridge.accountPresenter()

internal actual fun provideSessionProvider(): InMemoryUserSessionProvider = IosKoinBridge.sessionProvider()

internal actual fun provideAppPresenter(): AppPresenter = IosKoinBridge.appPresenter()

internal actual fun providePhoneAuthPresenter(): PhoneAuthPresenter = IosKoinBridge.phoneAuthPresenter()
