package com.project.livechat.ui.di

import com.google.firebase.auth.FirebaseAuth
import com.project.livechat.domain.providers.IPhoneAuthProvider
import com.project.livechat.ui.utils.auth.FirebasePhoneAuthProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideFirebasePhoneAuthProvider(firebaseAuth: FirebaseAuth): IPhoneAuthProvider =
        FirebasePhoneAuthProvider(firebaseAuth)
}