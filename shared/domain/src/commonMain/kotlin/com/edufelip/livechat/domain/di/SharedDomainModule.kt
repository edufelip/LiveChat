package com.edufelip.livechat.domain.di

import com.edufelip.livechat.domain.repositories.IAccountRepository
import com.edufelip.livechat.domain.repositories.IAppearanceSettingsRepository
import com.edufelip.livechat.domain.repositories.IBlockedContactsRepository
import com.edufelip.livechat.domain.repositories.IContactsRepository
import com.edufelip.livechat.domain.repositories.IDeviceTokenRepository
import com.edufelip.livechat.domain.repositories.INotificationSettingsRepository
import com.edufelip.livechat.domain.repositories.IOnboardingStatusRepository
import com.edufelip.livechat.domain.repositories.IPhoneAuthRepository
import com.edufelip.livechat.domain.repositories.IPrivacySettingsRepository
import com.edufelip.livechat.domain.repositories.IRemoteConfigRepository
import com.edufelip.livechat.domain.useCases.ApplyContactSyncPlanUseCase
import com.edufelip.livechat.domain.useCases.BlockContactUseCase
import com.edufelip.livechat.domain.useCases.BuildContactSyncPlanUseCase
import com.edufelip.livechat.domain.useCases.CheckRegisteredContactsUseCase
import com.edufelip.livechat.domain.useCases.DeleteAccountUseCase
import com.edufelip.livechat.domain.useCases.DeleteMessageLocalUseCase
import com.edufelip.livechat.domain.useCases.EnsureConversationUseCase
import com.edufelip.livechat.domain.useCases.EnsureUserInboxUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetLocalContactsUseCase
import com.edufelip.livechat.domain.useCases.GetOnboardingStatusSnapshotUseCase
import com.edufelip.livechat.domain.useCases.GetWelcomeSeenSnapshotUseCase
import com.edufelip.livechat.domain.useCases.MarkConversationReadUseCase
import com.edufelip.livechat.domain.useCases.ObserveAccountProfileUseCase
import com.edufelip.livechat.domain.useCases.ObserveAppearanceSettingsUseCase
import com.edufelip.livechat.domain.useCases.ObserveBlockedContactsUseCase
import com.edufelip.livechat.domain.useCases.ObserveContactByPhoneUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationSummariesUseCase
import com.edufelip.livechat.domain.useCases.ObserveConversationUseCase
import com.edufelip.livechat.domain.useCases.ObserveNotificationSettingsUseCase
import com.edufelip.livechat.domain.useCases.ObserveOnboardingStatusUseCase
import com.edufelip.livechat.domain.useCases.ObserveParticipantUseCase
import com.edufelip.livechat.domain.useCases.ObservePresenceUseCase
import com.edufelip.livechat.domain.useCases.ObservePrivacyPolicyUrlUseCase
import com.edufelip.livechat.domain.useCases.ObservePrivacySettingsUseCase
import com.edufelip.livechat.domain.useCases.ObserveWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.RefreshRemoteConfigUseCase
import com.edufelip.livechat.domain.useCases.RegisterDeviceTokenUseCase
import com.edufelip.livechat.domain.useCases.ResetPrivacySettingsUseCase
import com.edufelip.livechat.domain.useCases.ResolveConversationIdForContactUseCase
import com.edufelip.livechat.domain.useCases.SendMessageUseCase
import com.edufelip.livechat.domain.useCases.SetConversationArchivedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationMutedUseCase
import com.edufelip.livechat.domain.useCases.SetConversationPinnedUseCase
import com.edufelip.livechat.domain.useCases.SetOnboardingCompleteUseCase
import com.edufelip.livechat.domain.useCases.SetWelcomeSeenUseCase
import com.edufelip.livechat.domain.useCases.SyncConversationUseCase
import com.edufelip.livechat.domain.useCases.UnblockContactUseCase
import com.edufelip.livechat.domain.useCases.UnregisterDeviceTokenUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountDisplayNameUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountEmailUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountPhotoUseCase
import com.edufelip.livechat.domain.useCases.UpdateAccountStatusMessageUseCase
import com.edufelip.livechat.domain.useCases.UpdateInvitePreferenceUseCase
import com.edufelip.livechat.domain.useCases.UpdateLastSeenAudienceUseCase
import com.edufelip.livechat.domain.useCases.UpdateMessagePreviewUseCase
import com.edufelip.livechat.domain.useCases.UpdatePushNotificationsUseCase
import com.edufelip.livechat.domain.useCases.UpdateQuietHoursEnabledUseCase
import com.edufelip.livechat.domain.useCases.UpdateQuietHoursWindowUseCase
import com.edufelip.livechat.domain.useCases.UpdateReadReceiptsUseCase
import com.edufelip.livechat.domain.useCases.UpdateSelfPresenceUseCase
import com.edufelip.livechat.domain.useCases.UpdateTextScaleUseCase
import com.edufelip.livechat.domain.useCases.UpdateThemeModeUseCase
import com.edufelip.livechat.domain.useCases.ValidateContactsUseCase
import com.edufelip.livechat.domain.useCases.phone.ClearPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.RequestPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.ResendPhoneVerificationUseCase
import com.edufelip.livechat.domain.useCases.phone.VerifyOtpUseCase
import com.edufelip.livechat.domain.utils.DefaultPhoneNumberFormatter
import com.edufelip.livechat.domain.utils.PhoneNumberFormatter
import com.edufelip.livechat.domain.validation.PhoneNumberValidator
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedDomainModule: Module =
    module {
        single { PhoneNumberValidator() }
        single<PhoneNumberFormatter> { DefaultPhoneNumberFormatter() }
        factory { GetLocalContactsUseCase(get<IContactsRepository>()) }
        factory { GetLocalContactsSnapshotUseCase(get<IContactsRepository>()) }
        factory { BuildContactSyncPlanUseCase(get()) }
        factory { ApplyContactSyncPlanUseCase(get<IContactsRepository>()) }
        factory { ValidateContactsUseCase(get<IContactsRepository>(), phoneNumberFormatter = get()) }
        factory { CheckRegisteredContactsUseCase(get(), get(), get()) }
        factory { ObserveContactByPhoneUseCase(get<IContactsRepository>()) }
        factory { ObserveConversationUseCase(get()) }
        factory { ObserveConversationSummariesUseCase(get()) }
        factory { ObserveParticipantUseCase(get()) }
        factory { ObserveAccountProfileUseCase(get<IAccountRepository>()) }
        factory { ObserveNotificationSettingsUseCase(get<INotificationSettingsRepository>()) }
        factory { ObserveAppearanceSettingsUseCase(get<IAppearanceSettingsRepository>()) }
        factory { ObservePrivacySettingsUseCase(get<IPrivacySettingsRepository>()) }
        factory { ObserveBlockedContactsUseCase(get<IBlockedContactsRepository>()) }
        factory { ObserveOnboardingStatusUseCase(get<IOnboardingStatusRepository>()) }
        factory { ObserveWelcomeSeenUseCase(get<IOnboardingStatusRepository>()) }
        factory { ObservePrivacyPolicyUrlUseCase(get<IRemoteConfigRepository>()) }
        factory { RefreshRemoteConfigUseCase(get<IRemoteConfigRepository>()) }
        factory { GetOnboardingStatusSnapshotUseCase(get<IOnboardingStatusRepository>()) }
        factory { GetWelcomeSeenSnapshotUseCase(get<IOnboardingStatusRepository>()) }
        factory { ResolveConversationIdForContactUseCase(get(), get()) }
        factory { EnsureConversationUseCase(get()) }
        factory { EnsureUserInboxUseCase(get(), get()) }
        factory { SendMessageUseCase(get()) }
        factory { DeleteMessageLocalUseCase(get()) }
        factory { SyncConversationUseCase(get()) }
        factory { MarkConversationReadUseCase(get()) }
        factory { SetConversationPinnedUseCase(get()) }
        factory { SetConversationMutedUseCase(get()) }
        factory { SetConversationArchivedUseCase(get()) }
        factory { SetOnboardingCompleteUseCase(get()) }
        factory { SetWelcomeSeenUseCase(get()) }
        factory { UpdateAccountDisplayNameUseCase(get<IAccountRepository>()) }
        factory { UpdateAccountStatusMessageUseCase(get<IAccountRepository>()) }
        factory { UpdateAccountEmailUseCase(get<IAccountRepository>()) }
        factory { UpdateAccountPhotoUseCase(get<IAccountRepository>()) }
        factory { DeleteAccountUseCase(get<IAccountRepository>(), get()) }
        factory { UpdateThemeModeUseCase(get<IAppearanceSettingsRepository>()) }
        factory { UpdateTextScaleUseCase(get<IAppearanceSettingsRepository>()) }
        factory { UpdateInvitePreferenceUseCase(get<IPrivacySettingsRepository>()) }
        factory { UpdateLastSeenAudienceUseCase(get<IPrivacySettingsRepository>()) }
        factory { UpdateReadReceiptsUseCase(get<IPrivacySettingsRepository>(), get()) }
        factory { ResetPrivacySettingsUseCase(get<IPrivacySettingsRepository>()) }
        factory { ObservePresenceUseCase(get()) }
        factory { UpdateSelfPresenceUseCase(get()) }
        factory { BlockContactUseCase(get<IBlockedContactsRepository>(), get()) }
        factory { UnblockContactUseCase(get<IBlockedContactsRepository>()) }
        factory { UpdatePushNotificationsUseCase(get<INotificationSettingsRepository>()) }
        factory { UpdateQuietHoursEnabledUseCase(get<INotificationSettingsRepository>()) }
        factory { UpdateQuietHoursWindowUseCase(get<INotificationSettingsRepository>()) }
        factory { UpdateMessagePreviewUseCase(get<INotificationSettingsRepository>()) }
        factory { RegisterDeviceTokenUseCase(get<IDeviceTokenRepository>()) }
        factory { UnregisterDeviceTokenUseCase(get<IDeviceTokenRepository>()) }
        factory { RequestPhoneVerificationUseCase(get<IPhoneAuthRepository>()) }
        factory { ResendPhoneVerificationUseCase(get<IPhoneAuthRepository>()) }
        factory { VerifyOtpUseCase(get<IPhoneAuthRepository>(), get<EnsureUserInboxUseCase>()) }
        factory { ClearPhoneVerificationUseCase(get<IPhoneAuthRepository>()) }
    }
