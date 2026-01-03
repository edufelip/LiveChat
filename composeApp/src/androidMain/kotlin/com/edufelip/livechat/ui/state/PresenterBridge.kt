package com.edufelip.livechat.ui.state

import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.di.AndroidKoinBridge
import com.edufelip.livechat.domain.presentation.AccountPresenter
import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter

internal actual fun provideConversationListPresenter(): ConversationListPresenter = AndroidKoinBridge.conversationListPresenter()

internal actual fun provideConversationPresenter(): ConversationPresenter = AndroidKoinBridge.conversationPresenter()

internal actual fun provideContactsPresenter(): ContactsPresenter = AndroidKoinBridge.contactsPresenter()

internal actual fun provideAccountPresenter(): AccountPresenter = AndroidKoinBridge.accountPresenter()

internal actual fun provideSessionProvider(): InMemoryUserSessionProvider = AndroidKoinBridge.sessionProvider()

internal actual fun provideAppPresenter(): AppPresenter = AndroidKoinBridge.appPresenter()

internal actual fun providePhoneAuthPresenter(): PhoneAuthPresenter = AndroidKoinBridge.phoneAuthPresenter()
