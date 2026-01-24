package com.edufelip.livechat.di

import com.edufelip.livechat.data.session.InMemoryUserSessionProvider
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
import com.edufelip.livechat.domain.repositories.INotificationSettingsRepository
import com.edufelip.livechat.domain.useCases.RegisterDeviceTokenUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object AndroidKoinBridge : KoinComponent {
    fun conversationListPresenter(): ConversationListPresenter = get()

    fun conversationPresenter(): ConversationPresenter = get()

    fun contactsPresenter(): ContactsPresenter = get()

    fun accountPresenter(): AccountPresenter = get()

    fun sessionProvider(): InMemoryUserSessionProvider = get()

    fun appPresenter(): AppPresenter = get()

    fun phoneAuthPresenter(): PhoneAuthPresenter = get()

    fun notificationSettingsPresenter(): NotificationSettingsPresenter = get()

    fun notificationSettingsRepository(): INotificationSettingsRepository = get()

    fun appearanceSettingsPresenter(): AppearanceSettingsPresenter = get()

    fun privacySettingsPresenter(): PrivacySettingsPresenter = get()

    fun blockedContactsPresenter(): BlockedContactsPresenter = get()

    fun registerDeviceTokenUseCase(): RegisterDeviceTokenUseCase = get()
}
