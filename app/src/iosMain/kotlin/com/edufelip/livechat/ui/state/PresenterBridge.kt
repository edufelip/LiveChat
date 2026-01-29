package com.edufelip.livechat.ui.state

import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
import com.edufelip.livechat.di.IosPresentationBridge
import com.edufelip.livechat.domain.presentation.AccountPresenter
import com.edufelip.livechat.domain.presentation.AppPresenter
import com.edufelip.livechat.domain.presentation.AppearanceSettingsPresenter
import com.edufelip.livechat.domain.presentation.BlockedContactsPresenter
import com.edufelip.livechat.domain.presentation.ContactsPresenter
import com.edufelip.livechat.domain.presentation.ConversationListPresenter
import com.edufelip.livechat.domain.presentation.ConversationPresenter
import com.edufelip.livechat.domain.presentation.NotificationSettingsPresenter
import com.edufelip.livechat.domain.presentation.PhoneAuthPresenter
import com.edufelip.livechat.domain.presentation.PrivacySettingsPresenter

internal actual fun provideConversationListPresenter(): ConversationListPresenter = IosPresentationBridge.conversationListPresenter()

internal actual fun provideConversationPresenter(): ConversationPresenter = IosPresentationBridge.conversationPresenter()

internal actual fun provideContactsPresenter(): ContactsPresenter = IosPresentationBridge.contactsPresenter()

internal actual fun provideAccountPresenter(): AccountPresenter = IosPresentationBridge.accountPresenter()

internal actual fun provideNotificationSettingsPresenter(): NotificationSettingsPresenter =
    IosPresentationBridge.notificationSettingsPresenter()

internal actual fun provideAppearanceSettingsPresenter(): AppearanceSettingsPresenter = IosPresentationBridge.appearanceSettingsPresenter()

internal actual fun providePrivacySettingsPresenter(): PrivacySettingsPresenter = IosPresentationBridge.privacySettingsPresenter()

internal actual fun provideBlockedContactsPresenter(): BlockedContactsPresenter = IosPresentationBridge.blockedContactsPresenter()

internal actual fun provideSessionProvider(): InMemoryUserSessionProvider = IosPresentationBridge.sessionProvider()

internal actual fun provideAppPresenter(): AppPresenter = IosPresentationBridge.appPresenter()

internal actual fun providePhoneAuthPresenter(): PhoneAuthPresenter = IosPresentationBridge.phoneAuthPresenter()
